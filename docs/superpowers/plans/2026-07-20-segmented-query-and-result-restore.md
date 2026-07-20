# Segmented Query And Result Restore Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task with review checkpoints. This repository instruction forbids automatic subagent use, so execution is inline in the current session unless the user explicitly requests otherwise.

**Goal:** Add mutually exclusive 180/360/720-day warehouse querying with durable last-result restore, complete per-segment logs, reliable mark editing, comma-based advanced filters, and no idle layout gap.

**Architecture:** Keep the existing JavaFX shell and remote HTTP endpoint, but move date planning, batch orchestration, filtering, and snapshot persistence into pure/testable Java services. MainController becomes an adapter that translates controls into a batch request, consumes progress/results, and updates the view. A versioned JSON snapshot is atomically replaced only after every requested segment succeeds; the existing per-entry mark cache remains separate and is synchronized with the snapshot.

**Tech Stack:** Java 11, JavaFX 11, Maven, JUnit 5/Surefire, existing org.json dependency, Apache HttpClient, Java NIO atomic file replacement.

## Global Constraints

- A normal query is one inclusive 180-day segment; “查一年” is 360 days split into two segments; “查两年” is 720 days split into four segments.
- Segment end is today for long-range modes; each segment is inclusive, contiguous, non-overlapping, and starts at its end minus 179 days.
- Multiple entry numbers are processed outer-first; each number runs newest segment first, then older segments. Total requests equal entry-number count multiplied by segment count.
- Each successful request is appended immediately; a failed segment does not stop later work and does not replace the previous complete snapshot.
- No de-duplication is performed.
- A zero-row fully successful batch replaces the previous snapshot.
- Startup restores the last complete snapshot locally, never makes a network request, never shows a modal dialog, clears advanced filters, and recomputes date controls from today and the persisted mode.
- English and Chinese commas mean OR within one filter; different filter fields remain AND; mark matching is exact and other fields remain substring matching.
- Mark edits commit on Enter, focus loss/clicking elsewhere, row change, and window close; they are not written on every keystroke.
- Existing remote endpoints, column configuration, export behavior, photo links, and entry-cache.json compatibility remain intact.
- Automated tests use fake clients and temporary directories; tests do not contact the real warehouse service or require a visible desktop window.
- Every code/doc change updates README where relevant, passes Maven verification, is committed, and is pushed to origin/main after the latest-main check.

---

## File Map

### New production files

- src/main/java/com/warehousequery/app/query/QueryMode.java — normal, 360-day, and 720-day modes plus segment metadata.
- src/main/java/com/warehousequery/app/query/QuerySegment.java — immutable inclusive date segment.
- src/main/java/com/warehousequery/app/query/QueryRangePlanner.java — pure date calculation.
- src/main/java/com/warehousequery/app/query/SingleQueryClient.java — one-number/one-segment network boundary.
- src/main/java/com/warehousequery/app/query/QueryLogSink.java — structured request/response/error log boundary.
- src/main/java/com/warehousequery/app/query/QueryBatchRequest.java — normalized batch inputs.
- src/main/java/com/warehousequery/app/query/QueryProgress.java — immutable per-attempt progress event.
- src/main/java/com/warehousequery/app/query/QueryBatchResult.java — rows, segment outcomes, failures, logs, and completion state.
- src/main/java/com/warehousequery/app/query/QueryBatchService.java — sequential number/segment orchestration.
- src/main/java/com/warehousequery/app/query/AdvancedFilterMatcher.java — comma parser and field matching.
- src/main/java/com/warehousequery/app/query/QuerySnapshot.java — versioned complete-result snapshot model.
- src/main/java/com/warehousequery/app/query/QuerySnapshotStore.java — atomic JSON snapshot read/write and mark update.
- src/main/java/com/warehousequery/app/query/QuerySnapshotCodec.java — explicit org.json serialization.
- src/main/java/com/warehousequery/app/query/QueryUiState.java — pure mode/date control state.
- src/main/java/com/warehousequery/app/query/StartupRestorePlan.java — pure startup restoration plan.

### Modified production files

- src/main/java/com/warehousequery/app/service/WarehouseService.java — adapt the existing HTTP implementation to SingleQueryClient and append to a supplied log sink.
- src/main/java/com/warehousequery/app/service/LocalEntryCacheService.java — synchronize mark changes with the query snapshot.
- src/main/java/com/warehousequery/app/controller/MainController.java — mode/date controls, batch lifecycle, restore, progress, filter delegation, mark commit, and compact status layout.
- src/main/resources/fxml/MainView.fxml — two mutually exclusive checkboxes, status-strip IDs, and compact status hooks.
- src/main/resources/styles/app-theme.css — managed/visible behavior and compact status styling.
- README.md — implementation status, test commands, and snapshot location.

### New test files

- src/test/java/com/warehousequery/app/query/QueryRangePlannerTest.java
- src/test/java/com/warehousequery/app/query/AdvancedFilterMatcherTest.java
- src/test/java/com/warehousequery/app/query/QuerySnapshotStoreTest.java
- src/test/java/com/warehousequery/app/query/QueryBatchServiceTest.java
- src/test/java/com/warehousequery/app/query/MarkSnapshotSyncTest.java
- src/test/java/com/warehousequery/app/query/ControllerStateContractTest.java

---

## Task 1: Add Pure Query Modes And Date Segmentation

**Files:**
- Create: src/main/java/com/warehousequery/app/query/QueryMode.java
- Create: src/main/java/com/warehousequery/app/query/QuerySegment.java
- Create: src/main/java/com/warehousequery/app/query/QueryRangePlanner.java
- Test: src/test/java/com/warehousequery/app/query/QueryRangePlannerTest.java

**Interfaces:**
- QueryMode exposes NORMAL, ONE_YEAR, TWO_YEARS, segmentCount(), and totalDays().
- QuerySegment exposes ordinal(), totalSegments(), start(), and end().
- QueryRangePlanner.plan(QueryMode mode, LocalDate endDate) returns an immutable newest-to-oldest list and rejects a null end date.

- [ ] Step 1: Write the failing tests.

~~~java
@Test
void plansTwoNonOverlappingInclusiveSegmentsFromToday() {
    List<QuerySegment> segments = QueryRangePlanner.plan(
        QueryMode.ONE_YEAR, LocalDate.of(2026, 7, 20));

    assertEquals(2, segments.size());
    assertEquals(LocalDate.of(2026, 1, 22), segments.get(0).start());
    assertEquals(LocalDate.of(2026, 7, 20), segments.get(0).end());
    assertEquals(LocalDate.of(2025, 7, 26), segments.get(1).start());
    assertEquals(LocalDate.of(2026, 1, 21), segments.get(1).end());
    assertEquals(segments.get(0).start().minusDays(1), segments.get(1).end());
}

@Test
void plansFourSegmentsAcrossLeapDayWithoutOverlap() {
    List<QuerySegment> segments = QueryRangePlanner.plan(
        QueryMode.TWO_YEARS, LocalDate.of(2024, 2, 29));

    assertEquals(4, segments.size());
    for (int i = 1; i < segments.size(); i++) {
        assertEquals(segments.get(i - 1).start().minusDays(1), segments.get(i).end());
    }
    assertTrue(segments.stream().allMatch(s ->
        ChronoUnit.DAYS.between(s.start(), s.end()) == 179));
}
~~~

- [ ] Step 2: Run the focused test and verify it fails.

Run:

~~~powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-11.0.31.11-hotspot'
$env:Path="$env:JAVA_HOME\bin;C:\Users\10238\AppData\Local\Programs\Apache\apache-maven-3.9.16\bin;$env:Path"
mvn --batch-mode -Dtest=QueryRangePlannerTest test
~~~

Expected: FAIL because the query package classes do not yet exist.

- [ ] Step 3: Implement the smallest pure planner.

Use segmentEnd = endDate.minusDays(180L * ordinal) and segmentStart = segmentEnd.minusDays(179). Return exactly one, two, or four segments and wrap the result with Collections.unmodifiableList.

- [ ] Step 4: Run the focused test and verify it passes.

Run the same Maven command. Expected: all QueryRangePlannerTest cases pass with zero failures.

- [ ] Step 5: Commit and push.

~~~powershell
git add src/main/java/com/warehousequery/app/query src/test/java/com/warehousequery/app/query/QueryRangePlannerTest.java
git commit -m "feat: add warehouse query range planner"
git push origin main
~~~

## Task 2: Extract Advanced Filter Matching As A Tested API

**Files:**
- Create: src/main/java/com/warehousequery/app/query/AdvancedFilterMatcher.java
- Test: src/test/java/com/warehousequery/app/query/AdvancedFilterMatcherTest.java
- Modify: src/main/java/com/warehousequery/app/controller/MainController.java:1379-1474
- Modify: src/main/resources/fxml/MainView.fxml:91-104
- Modify: README.md

**Interfaces:**
- AdvancedFilterMatcher.match(WarehouseEntry entry, Map<String,String> filters) returns boolean.
- AdvancedFilterMatcher.parseTerms(String input, boolean exact) returns immutable normalized terms.
- Any term matches within one field; every non-empty field must match across fields.

- [ ] Step 1: Write failing tests.

~~~java
@Test
void supportsEnglishAndChineseCommaAsOrWithinEveryField() {
    WarehouseEntry entry = entry("owner-a", "沪A123", "红色箱");
    Map<String, String> filters = Map.of(
        "owner", "owner-b,owner-a",
        "plate", "沪B，沪A",
        "cargo", "蓝色,红色");

    assertTrue(AdvancedFilterMatcher.match(entry, filters));
}

@Test
void keepsDifferentFieldsAsAnd() {
    WarehouseEntry entry = entry("owner-a", "沪A123", "红色箱");
    assertFalse(AdvancedFilterMatcher.match(entry, Map.of(
        "owner", "owner-a,owner-b", "plate", "粤B")));
}

@Test
void markIsExactWhileOtherFieldsAreSubstring() {
    WarehouseEntry entry = entryWithMark("ABC-01");
    assertTrue(AdvancedFilterMatcher.match(entry, Map.of("mark", "ABC-01,XYZ")));
    assertFalse(AdvancedFilterMatcher.match(entry, Map.of("mark", "ABC")));
    assertTrue(AdvancedFilterMatcher.match(entry, Map.of("cargo", "BC-0")));
}
~~~

- [ ] Step 2: Run the focused test and verify it fails.

Run mvn --batch-mode -Dtest=AdvancedFilterMatcherTest test. Expected: FAIL because the matcher API is absent.

- [ ] Step 3: Implement the matcher and delegate from MainController.

Normalize English comma and Chinese comma while retaining current whitespace/semicolon compatibility. Keep exact matching for mark and current substring field mapping for owner, entry number, job number, driver, plate, cargo, package, and driver phone. Replace only the controller-local parser/matcher calls; retain existing status messages.

- [ ] Step 4: Run focused and regression tests.

~~~powershell
mvn --batch-mode -Dtest=AdvancedFilterMatcherTest test
mvn --batch-mode test
~~~

Expected: focused tests pass and the existing build remains green.

- [ ] Step 5: Commit and push.

~~~powershell
git add src/main/java/com/warehousequery/app/query/AdvancedFilterMatcher.java src/test/java/com/warehousequery/app/query/AdvancedFilterMatcherTest.java src/main/java/com/warehousequery/app/controller/MainController.java src/main/resources/fxml/MainView.fxml README.md
git commit -m "feat: support comma OR filters across warehouse fields"
git push origin main
~~~

## Task 3: Add Versioned Atomic Query Snapshots

**Files:**
- Create: src/main/java/com/warehousequery/app/query/QuerySnapshot.java
- Create: src/main/java/com/warehousequery/app/query/QuerySnapshotCodec.java
- Create: src/main/java/com/warehousequery/app/query/QuerySnapshotStore.java
- Test: src/test/java/com/warehousequery/app/query/QuerySnapshotStoreTest.java
- Test: src/test/java/com/warehousequery/app/query/MarkSnapshotSyncTest.java
- Modify: src/main/java/com/warehousequery/app/service/LocalEntryCacheService.java
- Modify: README.md

**Interfaces:**
- QuerySnapshotStore.load() returns Optional<QuerySnapshot>.
- QuerySnapshotStore.path() returns the configured snapshot Path for test fixtures and diagnostics.
- QuerySnapshotStore.replace(QuerySnapshot snapshot) writes only complete snapshots using temp-file plus atomic move fallback.
- QuerySnapshotStore.updateMark(String stableKey, String mark) updates the current snapshot without changing query metadata.
- QuerySnapshot contains schema version, saved time, QueryMode, requested IDs, status index, actual segments, ordered rows, request log, and response log.

- [ ] Step 1: Write failing persistence tests.

~~~java
@Test
void roundTripsAllRowsAndRawLogs() throws Exception {
    QuerySnapshotStore store = new QuerySnapshotStore(
        tempDir.resolve("query-snapshot.json"));
    QuerySnapshot expected = fixtureSnapshot(QueryMode.ONE_YEAR, 2);

    store.replace(expected);

    assertEquals(expected, store.load().orElseThrow());
    assertTrue(Files.readString(store.path()).contains("raw response segment 2"));
}

@Test
void malformedFileLoadsAsEmptyWithoutThrowing() throws Exception {
    QuerySnapshotStore store = new QuerySnapshotStore(
        tempDir.resolve("query-snapshot.json"));
    Files.writeString(store.path(), "{not-json");

    assertTrue(store.load().isEmpty());
}

@Test
void markUpdateChangesOnlyTheMatchingRow() throws Exception {
    QuerySnapshotStore store = storeWithCompleteFixture();
    store.updateMark("zyh-1", "NEW MARK");

    assertEquals("NEW MARK", store.load().orElseThrow().rows().get(0).mark());
}
~~~

- [ ] Step 2: Run focused tests and verify they fail.

Run mvn --batch-mode -Dtest=QuerySnapshotStoreTest,MarkSnapshotSyncTest test. Expected: FAIL because snapshot types and store methods do not exist.

- [ ] Step 3: Implement typed JSON serialization and atomic replacement.

Use explicit JSON keys and schemaVersion 1. Serialize every WarehouseEntry field already copied by LocalEntryCacheService, not only visible columns. Write UTF-8 to a sibling temporary file, close it, then move with ATOMIC_MOVE and REPLACE_EXISTING; fall back to REPLACE_EXISTING if atomic moves are unsupported. Never delete the current file before the replacement is ready.

- [ ] Step 4: Verify old snapshot protection.

Add a test that forces a write failure with a non-writable temporary target and asserts the prior snapshot can still be loaded unchanged. Run the focused tests; expected: all persistence tests pass.

- [ ] Step 5: Commit and push.

~~~powershell
git add src/main/java/com/warehousequery/app/query/QuerySnapshot*.java src/main/java/com/warehousequery/app/service/LocalEntryCacheService.java src/test/java/com/warehousequery/app/query/QuerySnapshotStoreTest.java src/test/java/com/warehousequery/app/query/MarkSnapshotSyncTest.java README.md
git commit -m "feat: persist complete warehouse query snapshots"
git push origin main
~~~

## Task 4: Build Sequential Batch Execution And Aggregated Logs

**Files:**
- Create: src/main/java/com/warehousequery/app/query/SingleQueryClient.java
- Create: src/main/java/com/warehousequery/app/query/QueryLogSink.java
- Create: src/main/java/com/warehousequery/app/query/QueryBatchRequest.java
- Create: src/main/java/com/warehousequery/app/query/QueryBatchResult.java
- Create: src/main/java/com/warehousequery/app/query/QueryBatchService.java
- Test: src/test/java/com/warehousequery/app/query/QueryBatchServiceTest.java
- Modify: src/main/java/com/warehousequery/app/service/WarehouseService.java:161-315

**Interfaces:**
- SingleQueryClient.query(String jcbh, int status, QuerySegment segment, QueryLogSink logs) returns CompletableFuture<List<WarehouseEntry>>.
- QueryBatchRequest contains normalized IDs, status index, QueryMode, and end date.
- QueryBatchService.execute(QueryBatchRequest, Consumer<QueryProgress>) returns CompletableFuture<QueryBatchResult>.
- QueryLogSink.request(...), response(...), and failure(...) append batch ID, ID, segment ordinal, date range, timestamp, and raw content.

- [ ] Step 1: Write failing batch tests with a fake client.

~~~java
@Test
void executesEachIdNewestSegmentFirstAndReportsEveryAttempt() throws Exception {
    FakeClient client = new FakeClient();
    QueryBatchResult result = new QueryBatchService(client).execute(
        new QueryBatchRequest(List.of("A", "B"), 1, QueryMode.ONE_YEAR,
            LocalDate.of(2026, 7, 20)), progress::add).get();

    assertEquals(List.of("A/1", "A/2", "B/1", "B/2"), client.calls);
    assertEquals(4, progress.size());
    assertTrue(result.complete());
    assertEquals(4, result.rows().size());
}

@Test
void continuesAfterOneSegmentFailureAndMarksBatchIncomplete() throws Exception {
    FakeClient client = new FakeClient(Set.of("A/2"));
    QueryBatchResult result = new QueryBatchService(client).execute(
        requestFor("A", QueryMode.ONE_YEAR), progress::add).get();

    assertEquals(List.of("A/1", "A/2"), client.calls);
    assertEquals(1, result.rows().size());
    assertFalse(result.complete());
    assertEquals("A/2", result.failures().get(0).identity());
}
~~~

- [ ] Step 2: Run focused tests and verify they fail.

Run mvn --batch-mode -Dtest=QueryBatchServiceTest test. Expected: FAIL because the batch interfaces and service do not exist.

- [ ] Step 3: Extract one-number/one-segment execution from WarehouseService.

Keep existing response parsing and field mapping. Replace per-call clearLogFiles behavior with injected QueryLogSink; retain queryWarehouse(List<String>,...) as a compatibility facade that uses a normal one-segment batch request. Do not clear persisted logs in the service constructor.

- [ ] Step 4: Implement sequential orchestration.

Create segments once, loop IDs first and segments second, await each SingleQueryClient future before starting the next request, append rows exactly as returned, catch per-request exceptions into QueryFailure, and always continue. Mark complete only when the failure list is empty. Do not write a snapshot in this service; controller/store integration does that only on completion.

- [ ] Step 5: Run focused and full tests.

~~~powershell
mvn --batch-mode -Dtest=QueryBatchServiceTest test
mvn --batch-mode test
~~~

Expected: all tests pass and no real network request is made by the test suite.

- [ ] Step 6: Commit and push.

~~~powershell
git add src/main/java/com/warehousequery/app/query src/main/java/com/warehousequery/app/service/WarehouseService.java src/test/java/com/warehousequery/app/query/QueryBatchServiceTest.java
git commit -m "feat: add sequential warehouse query batches"
git push origin main
~~~

## Task 5: Integrate JavaFX Controls, Restore, Mark Commit, And Layout

**Files:**
- Modify: src/main/resources/fxml/MainView.fxml:53-124
- Modify: src/main/resources/styles/app-theme.css:64-80
- Modify: src/main/java/com/warehousequery/app/controller/MainController.java:178-415,1197-1283,1322-1369,1476-1561,2093-2127
- Test: src/test/java/com/warehousequery/app/query/ControllerStateContractTest.java
- Modify: README.md

**Interfaces:**
- Add CheckBox fields oneYearCheckBox and twoYearsCheckBox with labels 查一年 and 查两年.
- QueryUiState.forMode(QueryMode mode, LocalDate today) returns dates, selected flags, and date-picker disabled state.
- StartupRestorePlan.from(QuerySnapshot snapshot, LocalDate today) returns rows, mode, recomputed dates, empty filters, and shouldQueryNetwork() == false.
- MainController.commitActiveMarkEdit() is called by row change and window-close paths.

- [ ] Step 1: Write failing controller-state tests.

~~~java
@Test
void modeStateMapsToMutuallyExclusiveDateRanges() {
    QueryUiState state = QueryUiState.forMode(
        QueryMode.ONE_YEAR, LocalDate.of(2026, 7, 20));

    assertEquals(LocalDate.of(2025, 7, 26), state.start());
    assertEquals(LocalDate.of(2026, 7, 20), state.end());
    assertTrue(state.oneYearSelected());
    assertFalse(state.twoYearsSelected());
    assertTrue(state.datePickersDisabled());
}

@Test
void startupRestoreClearsFiltersAndDoesNotRequestNetworkData() {
    StartupRestorePlan plan = StartupRestorePlan.from(
        snapshotFixture(), LocalDate.of(2026, 7, 20));

    assertTrue(plan.rows().size() > 0);
    assertTrue(plan.filters().isEmpty());
    assertFalse(plan.shouldQueryNetwork());
}
~~~

- [ ] Step 2: Run focused test and verify it fails.

Run mvn --batch-mode -Dtest=ControllerStateContractTest test. Expected: FAIL because the pure UI-state contract types do not yet exist.

- [ ] Step 3: Add controls and mode state.

Place both checkboxes immediately after the end-date picker in the existing FlowPane. Attach one listener that enforces mutual exclusion, persists the selected mode, recomputes dates from LocalDate.now(), disables/enables both date pickers, and never displays the old date-range warning for programmatic mode changes. Correct normal-range validation to inclusive 180-day semantics, so ChronoUnit.DAYS.between must be at most 179.

- [ ] Step 4: Replace one-shot query handling with batch integration.

Normalize IDs, create QueryBatchRequest, clear only the in-memory current view, subscribe to progress, append each successful segment on the JavaFX thread, collect failures, and update the compact status line. On complete, construct and atomically save QuerySnapshot; on partial failure, leave the previous snapshot untouched and show each failed ID/segment. Save query mode and metadata needed for startup restoration.

- [ ] Step 5: Implement startup restore without network or modal alerts.

Load the snapshot after controls are initialized, restore raw rows and status, clear all advanced filter fields, recompute today-based dates from persisted mode, and set a non-modal one-line restore message. Remove startup calls that validate persisted dates through the alert path. A missing/corrupt snapshot yields an empty table and a quiet diagnostic log.

- [ ] Step 6: Fix mark edit commit paths.

Use a custom editable mark cell that commits its current text on focus loss and keeps normal Enter commit. Before changing selected row, filtering, closing, or replacing the table list, call commitActiveMarkEdit. On commit, update LocalEntryCacheService and QuerySnapshotStore.updateMark using the stable key; do not write while each character is typed.

- [ ] Step 7: Remove idle layout occupancy.

Give progress/status containers stable IDs and bind managed to the same condition as visible, or centralize this in setStatusMessage. The table must move up when there is no progress or result text; the progress overlay remains managed only while a batch is active.

- [ ] Step 8: Run pure contract tests and compile.

~~~powershell
mvn --batch-mode -Dtest=ControllerStateContractTest,QueryRangePlannerTest,AdvancedFilterMatcherTest,QuerySnapshotStoreTest,QueryBatchServiceTest,MarkSnapshotSyncTest test
mvn --batch-mode clean package
~~~

Expected: all focused tests pass, the shaded JAR is rebuilt, and no test contacts the remote service.

- [ ] Step 9: Commit and push.

~~~powershell
git add src/main/resources/fxml/MainView.fxml src/main/resources/styles/app-theme.css src/main/java/com/warehousequery/app/controller/MainController.java src/main/java/com/warehousequery/app/query/QueryUiState.java src/main/java/com/warehousequery/app/query/StartupRestorePlan.java src/test/java/com/warehousequery/app/query/ControllerStateContractTest.java README.md
git commit -m "feat: integrate segmented query restore and mark editing"
git push origin main
~~~

## Task 6: Final Verification And Delivery

**Files:**
- Modify: README.md (final usage and verification notes only)
- Verify: all production and test files from Tasks 1–5

- [ ] Step 1: Sync latest main before final verification.

~~~powershell
git status --short --branch
git fetch origin main
git pull --ff-only origin main
~~~

Expected: no unrelated changes and a clean fast-forward/no-op.

- [ ] Step 2: Run the full automated test/build gate.

~~~powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-11.0.31.11-hotspot'
$env:Path="$env:JAVA_HOME\bin;C:\Users\10238\AppData\Local\Programs\Apache\apache-maven-3.9.16\bin;$env:Path"
mvn --batch-mode clean test package
git diff --check
~~~

Expected: Maven exits 0, all tests pass, and only known shade warnings remain.

- [ ] Step 3: Run an API-style end-to-end smoke test with a fake client.

Invoke a test-only facade using IDs A,B, mode ONE_YEAR, and fake responses for four calls. Assert exact call order A/1,A/2,B/1,B/2, four segment markers in each log, four result groups, and a persisted complete snapshot. Run a second smoke with B/2 failing and assert later calls still execute while the old snapshot hash remains unchanged.

- [ ] Step 4: Verify packaged application startup without manual data entry.

Start the rebuilt shaded JAR using the existing Java 11 runtime, confirm the JavaFX window title is 仓库查询系统, and inspect accessibility text for 查一年, 查两年, 查询, 高级筛选, and date controls. Do not submit a real warehouse query as part of automated verification.

- [ ] Step 5: Update README and commit delivery metadata.

Document the new controls, snapshot path, restore behavior, comma rules, and exact test command. Commit and push:

~~~powershell
git add README.md
git commit -m "docs: document segmented query workflow"
git push origin main
~~~

- [ ] Step 6: Final clean-state check.

~~~powershell
git status --short --branch
git rev-parse HEAD
git rev-parse origin/main
~~~

Expected: clean worktree and identical local/remote commit IDs.

---

## Plan Self-Review

- Date rules are covered by Tasks 1 and 5, including inclusive boundaries and current-day recalculation.
- Per-ID sequential requests, immediate append, partial failure, no de-duplication, and complete-only snapshot replacement are covered by Tasks 4 and 5.
- Raw request/response persistence and startup restoration are covered by Tasks 3–5.
- Mark save triggers and snapshot synchronization are covered by Tasks 3 and 5.
- Comma OR/field AND matching is covered by Task 2.
- Idle blank-space removal is covered by Task 5.
- Automated tests are pure service/API contracts and fake-client smoke tests; no real endpoint is contacted.
- No placeholders or unassigned interfaces remain; QueryBatchService and QuerySnapshotStore signatures are defined before their consumers.
