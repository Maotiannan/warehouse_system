package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StartupRestorePlan {
    private final List<WarehouseEntry> rows;
    private final QueryMode mode;
    private final QueryUiState uiState;
    private final Map<String, String> filters;
    private final boolean shouldQueryNetwork;
    private final List<String> entryNumbers;
    private final int statusIndex;
    private final LocalDate rangeStart;
    private final LocalDate rangeEnd;

    private StartupRestorePlan(
        List<WarehouseEntry> rows,
        QueryMode mode,
        QueryUiState uiState,
        Map<String, String> filters,
        boolean shouldQueryNetwork,
        List<String> entryNumbers,
        int statusIndex,
        LocalDate rangeStart,
        LocalDate rangeEnd) {
        this.rows = Collections.unmodifiableList(new ArrayList<WarehouseEntry>(rows));
        this.mode = mode;
        this.uiState = uiState;
        this.filters = Collections.unmodifiableMap(filters);
        this.shouldQueryNetwork = shouldQueryNetwork;
        this.entryNumbers = Collections.unmodifiableList(new ArrayList<String>(entryNumbers));
        this.statusIndex = statusIndex;
        this.rangeStart = Objects.requireNonNull(rangeStart, "rangeStart");
        this.rangeEnd = Objects.requireNonNull(rangeEnd, "rangeEnd");
    }

    public static StartupRestorePlan from(QuerySnapshot snapshot, LocalDate today) {
        QueryMode mode = snapshot == null ? QueryMode.NORMAL : snapshot.mode();
        QueryUiState state = QueryUiState.forMode(mode, today);
        if (snapshot == null) {
            return new StartupRestorePlan(
                Collections.<WarehouseEntry>emptyList(),
                mode,
                state,
                Collections.<String, String>emptyMap(),
                false,
                Collections.<String>emptyList(),
                1,
                state.start(),
                state.end());
        }
        LocalDate restoredEnd = snapshot.queryEndDate() != null
            ? snapshot.queryEndDate()
            : state.end();
        LocalDate restoredStart = earliestSegmentStart(snapshot, restoredEnd, state.start());
        return new StartupRestorePlan(
            snapshot.rows(),
            mode,
            state,
            Collections.<String, String>emptyMap(),
            false,
            snapshot.entryNumbers(),
            snapshot.statusIndex(),
            restoredStart,
            restoredEnd);
    }

    private static LocalDate earliestSegmentStart(
        QuerySnapshot snapshot,
        LocalDate fallbackEnd,
        LocalDate fallbackStart) {
        List<QuerySegment> segments = snapshot.segments();
        if (segments != null && !segments.isEmpty()) {
            LocalDate earliest = null;
            for (QuerySegment segment : segments) {
                if (segment == null || segment.start() == null) {
                    continue;
                }
                if (earliest == null || segment.start().isBefore(earliest)) {
                    earliest = segment.start();
                }
            }
            if (earliest != null) {
                return earliest;
            }
        }
        LocalDate byMode = QueryUiState.forMode(snapshot.mode(), fallbackEnd).start();
        return byMode != null ? byMode : fallbackStart;
    }

    public List<WarehouseEntry> rows() {
        return this.rows;
    }

    public QueryMode mode() {
        return this.mode;
    }

    public QueryUiState uiState() {
        return this.uiState;
    }

    public LocalDate start() {
        return this.rangeStart;
    }

    public LocalDate end() {
        return this.rangeEnd;
    }

    public Map<String, String> filters() {
        return this.filters;
    }

    public boolean shouldQueryNetwork() {
        return this.shouldQueryNetwork;
    }

    public List<String> entryNumbers() {
        return this.entryNumbers;
    }

    public int statusIndex() {
        return this.statusIndex;
    }
}
