package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class QuerySnapshotStore {
    private static final Path DEFAULT_PATH = Paths.get(
        System.getProperty("user.home"),
        ".warehouse-query-system",
        "query-snapshot.json");

    private final Path path;
    private final Function<Path, Path> temporaryPathFactory;
    private final QuerySnapshotCodec codec = new QuerySnapshotCodec();

    public QuerySnapshotStore() {
        this(DEFAULT_PATH);
    }

    public QuerySnapshotStore(Path path) {
        this(path, target -> target.resolveSibling(
            target.getFileName().toString() + ".tmp-" + UUID.randomUUID()));
    }

    QuerySnapshotStore(Path path, Function<Path, Path> temporaryPathFactory) {
        this.path = path.toAbsolutePath().normalize();
        this.temporaryPathFactory = java.util.Objects.requireNonNull(
            temporaryPathFactory,
            "temporaryPathFactory");
    }

    public Path path() {
        return this.path;
    }

    public Optional<QuerySnapshot> load() {
        if (!Files.exists(this.path)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(this.path, StandardCharsets.UTF_8);
            return Optional.of(this.codec.decode(content));
        }
        catch (Exception exception) {
            System.err.println("读取上次查询快照失败: " + exception.getMessage());
            return Optional.empty();
        }
    }

    public void replace(QuerySnapshot snapshot) throws IOException {
        if (snapshot == null) {
            throw new NullPointerException("snapshot");
        }
        Path parent = this.path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = java.util.Objects.requireNonNull(
            this.temporaryPathFactory.apply(this.path),
            "temporaryPath").toAbsolutePath().normalize();
        if (temporary.equals(this.path)) {
            throw new IOException("Temporary snapshot path must differ from destination");
        }
        byte[] content = this.codec.encode(snapshot).getBytes(StandardCharsets.UTF_8);
        try {
            try (FileChannel channel = FileChannel.open(
                temporary,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(content);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(
                    temporary,
                    this.path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, this.path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }

    public boolean updateMark(String stableKey, String mark) throws IOException {
        String normalizedKey = sanitize(stableKey);
        if (normalizedKey.isEmpty()) {
            return false;
        }
        Optional<QuerySnapshot> current = this.load();
        if (current.isEmpty()) {
            return false;
        }
        boolean updated = false;
        for (WarehouseEntry row : current.get().rows()) {
            if (!normalizedKey.equals(stableKey(row))) {
                continue;
            }
            row.setMt(mark == null ? "" : mark.trim());
            updated = true;
            break;
        }
        if (updated) {
            this.replace(current.get());
        }
        return updated;
    }

    public static String stableKey(WarehouseEntry entry) {
        if (entry == null) {
            return "";
        }
        return firstNonBlank(entry.getZyh(), entry.getJczyh(), entry.getJcbh());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = sanitize(value);
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "";
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
