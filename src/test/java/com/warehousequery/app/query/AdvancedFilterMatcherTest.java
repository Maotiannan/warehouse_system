package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdvancedFilterMatcherTest {
    @Test
    void supportsEnglishAndChineseCommaAsOrWithinEveryField() {
        WarehouseEntry entry = fixtureEntry();
        Map<String, String> filters = new LinkedHashMap<String, String>();
        filters.put(AdvancedFilterMatcher.OWNER, "其他货主,货主甲");
        filters.put(AdvancedFilterMatcher.ENTRY_NUMBER, "X，L26MH001");
        filters.put(AdvancedFilterMatcher.JOB_NUMBER, "X,JOB-001");
        filters.put(AdvancedFilterMatcher.DRIVER, "李四，张三");
        filters.put(AdvancedFilterMatcher.PLATE, "粤B,沪A");
        filters.put(AdvancedFilterMatcher.CARGO, "塑料，棉布");
        filters.put(AdvancedFilterMatcher.MARK, "OTHER，MARK-01");
        filters.put(AdvancedFilterMatcher.PACKAGE, "托盘,纸箱");
        filters.put(AdvancedFilterMatcher.DRIVER_PHONE, "0000，1380013");

        assertTrue(AdvancedFilterMatcher.match(entry, filters));
    }

    @Test
    void keepsDifferentFieldsAsAnd() {
        WarehouseEntry entry = fixtureEntry();

        assertFalse(AdvancedFilterMatcher.match(entry, Map.of(
            AdvancedFilterMatcher.OWNER, "货主甲,货主乙",
            AdvancedFilterMatcher.PLATE, "粤B,苏C")));
    }

    @Test
    void markIsExactWhileOtherFieldsUseSubstringMatching() {
        WarehouseEntry entry = fixtureEntry();

        assertTrue(AdvancedFilterMatcher.match(entry, Map.of(
            AdvancedFilterMatcher.MARK, "MARK-01,OTHER")));
        assertFalse(AdvancedFilterMatcher.match(entry, Map.of(
            AdvancedFilterMatcher.MARK, "MARK")));
        assertTrue(AdvancedFilterMatcher.match(entry, Map.of(
            AdvancedFilterMatcher.CARGO, "棉,塑料")));
    }

    @Test
    void matchesOwnerAndDriverPhoneFromEitherMappedSourceField() {
        WarehouseEntry entry = new WarehouseEntry();
        entry.setShdw("备用货主");
        entry.setDriverdh("13900000000");

        assertTrue(AdvancedFilterMatcher.match(entry, Map.of(
            AdvancedFilterMatcher.OWNER, "备用货主",
            AdvancedFilterMatcher.DRIVER_PHONE, "1390")));
    }

    private WarehouseEntry fixtureEntry() {
        WarehouseEntry entry = new WarehouseEntry();
        entry.setHz("货主甲");
        entry.setJcbh("L26MH001");
        entry.setJczyh("JOB-001");
        entry.setZyh("WORK-001");
        entry.setJsy("张三");
        entry.setCh("沪A12345");
        entry.setHwmc("棉布");
        entry.setHwmc1("棉织物");
        entry.setMt("MARK-01");
        entry.setBzgg("纸箱");
        entry.setJsydh("13800138000");
        return entry;
    }
}
