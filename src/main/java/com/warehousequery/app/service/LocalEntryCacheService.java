package com.warehousequery.app.service;

import com.warehousequery.app.model.WarehouseEntry;
import com.warehousequery.app.util.ExceptionHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public final class LocalEntryCacheService {
    private static final LocalEntryCacheService INSTANCE = new LocalEntryCacheService();
    private static final Path CACHE_FILE = Paths.get(System.getProperty("user.home"), ".warehouse-query-system", "entry-cache.json");
    private static final String ROOT_ENTRIES = "entries";
    private JSONObject cacheRoot;
    private boolean loaded;

    private LocalEntryCacheService() {
    }

    public static LocalEntryCacheService getInstance() {
        return INSTANCE;
    }

    public synchronized boolean hasPersistentKey(WarehouseEntry entry) {
        return !this.buildCacheKey(entry).isEmpty();
    }

    public synchronized void mergeNetworkEntries(List<WarehouseEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        try {
            JSONObject entriesObject = this.getEntriesObject();
            boolean dirty = false;
            for (WarehouseEntry entry : entries) {
                if (!this.isCacheableEntry(entry)) {
                    continue;
                }
                String cacheKey = this.buildCacheKey(entry);
                if (cacheKey.isEmpty()) {
                    continue;
                }
                String networkMt = this.sanitize(entry.getMt());
                JSONObject existing = entriesObject.optJSONObject(cacheKey);
                if (existing != null) {
                    String cachedInguid = existing.optString("inguid", "");
                    if (!cachedInguid.isEmpty() && this.sanitize(entry.getInguid()).isEmpty()) {
                        entry.setInguid(cachedInguid);
                    }
                    if (existing.optBoolean("mtEdited", false)) {
                        entry.setMt(existing.optString("mt", ""));
                    }
                }
                JSONObject merged = this.toJson(entry);
                merged.put("cacheKey", cacheKey);
                merged.put("lastNetworkMt", networkMt);
                if (existing != null && existing.optBoolean("mtEdited", false)) {
                    merged.put("mtEdited", true);
                    merged.put("mt", existing.optString("mt", ""));
                } else {
                    merged.put("mtEdited", false);
                }
                merged.put("updatedAt", Instant.now().toString());
                entriesObject.put(cacheKey, merged);
                dirty = true;
            }
            if (dirty) {
                this.writeCache();
            }
        }
        catch (Exception e) {
            ExceptionHandler.handleException("合并本地查询缓存", e, false);
        }
    }

    public synchronized void saveEditedMark(WarehouseEntry entry) {
        if (entry == null) {
            return;
        }
        String cacheKey = this.buildCacheKey(entry);
        if (cacheKey.isEmpty()) {
            return;
        }
        try {
            JSONObject entriesObject = this.getEntriesObject();
            JSONObject record = this.toJson(entry);
            record.put("cacheKey", cacheKey);
            record.put("mtEdited", true);
            record.put("updatedAt", Instant.now().toString());
            entriesObject.put(cacheKey, record);
            this.writeCache();
        }
        catch (Exception e) {
            ExceptionHandler.handleException("保存本地唛头缓存", e, false);
        }
    }

    private JSONObject getEntriesObject() throws IOException {
        this.ensureLoaded();
        JSONObject entriesObject = this.cacheRoot.optJSONObject(ROOT_ENTRIES);
        if (entriesObject == null) {
            entriesObject = new JSONObject();
            this.cacheRoot.put(ROOT_ENTRIES, entriesObject);
        }
        return entriesObject;
    }

    private void ensureLoaded() throws IOException {
        if (this.loaded) {
            return;
        }
        this.loaded = true;
        Files.createDirectories(CACHE_FILE.getParent());
        if (!Files.exists(CACHE_FILE)) {
            this.cacheRoot = new JSONObject();
            this.cacheRoot.put(ROOT_ENTRIES, new JSONObject());
            return;
        }
        String content = Files.readString(CACHE_FILE, StandardCharsets.UTF_8).trim();
        if (content.isEmpty()) {
            this.cacheRoot = new JSONObject();
            this.cacheRoot.put(ROOT_ENTRIES, new JSONObject());
            return;
        }
        try {
            this.cacheRoot = new JSONObject(content);
        }
        catch (JSONException e) {
            throw new IOException("无法解析本地缓存文件: " + CACHE_FILE, e);
        }
        if (!this.cacheRoot.has(ROOT_ENTRIES) || !(this.cacheRoot.opt(ROOT_ENTRIES) instanceof JSONObject)) {
            this.cacheRoot.put(ROOT_ENTRIES, new JSONObject());
        }
    }

    private void writeCache() throws IOException {
        Files.createDirectories(CACHE_FILE.getParent());
        Files.writeString(
            CACHE_FILE,
            this.cacheRoot.toString(2),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private String buildCacheKey(WarehouseEntry entry) {
        return this.firstNonBlank(entry.getZyh(), entry.getJczyh(), entry.getJcbh());
    }

    private boolean isCacheableEntry(WarehouseEntry entry) {
        if (entry == null) {
            return false;
        }
        if (this.buildCacheKey(entry).isEmpty()) {
            return false;
        }
        String bz = this.sanitize(entry.getBz());
        return !(bz.contains("未找到记录")
            || bz.contains("没有找到")
            || bz.contains("查询结果为空")
            || bz.contains("未能解析")
            || bz.contains("抱歉")
            || bz.contains("请检查")
            || bz.contains("错误")
            || bz.contains("API")
            || bz.contains("日期范围"));
    }

    private JSONObject toJson(WarehouseEntry entry) {
        JSONObject record = new JSONObject();
        record.put("jcbh", this.sanitize(entry.getJcbh()));
        record.put("jczyh", this.sanitize(entry.getJczyh()));
        record.put("zyh", this.sanitize(entry.getZyh()));
        record.put("yjrq", this.sanitize(entry.getYjrq()));
        record.put("jcrq", this.sanitize(entry.getJcrq()));
        record.put("lf", this.sanitize(entry.getLf()));
        record.put("bzgg", this.sanitize(entry.getBzgg()));
        record.put("hwmc", this.sanitize(entry.getHwmc()));
        record.put("mt", this.sanitize(entry.getMt()));
        record.put("hh", this.sanitize(entry.getHh()));
        record.put("js", entry.getJs());
        record.put("tj", entry.getTj());
        record.put("kctj", entry.getKctj());
        record.put("mz", entry.getMz());
        record.put("ts", entry.getTs());
        record.put("shdw", this.sanitize(entry.getShdw()));
        record.put("kcjs", entry.getKcjs());
        record.put("bgzt", this.sanitize(entry.getBgzt()));
        record.put("bz", this.sanitize(entry.getBz()));
        record.put("jcid", this.sanitize(entry.getJcid()));
        record.put("inguid", this.sanitize(entry.getInguid()));
        record.put("yyh", this.sanitize(entry.getYyh()));
        record.put("srrq", this.sanitize(entry.getSrrq()));
        record.put("hd", this.sanitize(entry.getHd()));
        record.put("ch", this.sanitize(entry.getCh()));
        record.put("jsy", this.sanitize(entry.getJsy()));
        record.put("jsydh", this.sanitize(entry.getJsydh()));
        record.put("fgsmc", this.sanitize(entry.getFgsmc()));
        record.put("yqqdsj", this.sanitize(entry.getYqqdsj()));
        record.put("hz", this.sanitize(entry.getHz()));
        record.put("yjjs", entry.getYjjs());
        record.put("yjtj", entry.getYjtj());
        record.put("yjmz", entry.getYjmz());
        record.put("kcmz", entry.getKcmz());
        record.put("xhrq", this.sanitize(entry.getXhrq()));
        record.put("xhrq2", this.sanitize(entry.getXhrq2()));
        record.put("hwmc1", this.sanitize(entry.getHwmc1()));
        record.put("driverdh", this.sanitize(entry.getDriverdh()));
        record.put("cleng", this.sanitize(entry.getCleng()));
        record.put("chengzhong", this.sanitize(entry.getChengzhong()));
        record.put("bz2", this.sanitize(entry.getBz2()));
        record.put("hdmc", this.sanitize(entry.getHdmc()));
        return record;
    }

    private String firstNonBlank(String ... values) {
        for (String value : values) {
            String sanitized = this.sanitize(value);
            if (!sanitized.isEmpty()) {
                return sanitized;
            }
        }
        return "";
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
