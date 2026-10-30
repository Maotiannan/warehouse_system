package com.warehousequery.app.query;

import com.warehousequery.app.config.AppConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 新查询系统（螭吻WMS）状态映射测试：
 * 旧四档状态（预约/进仓/库存/出仓）映射为新系统覆盖状态码。
 */
public class AppConfigNewSystemMappingTest {

    @Test
    public void mapsLegacyStatusToNewSystemCoverStatus() {
        assertEquals("0", AppConfig.newSystemCoverStatus(0), "预约应映射为新系统0(Booking)");
        assertEquals("150", AppConfig.newSystemCoverStatus(1), "进仓应映射为新系统150(Entry)");
        assertEquals("200", AppConfig.newSystemCoverStatus(2), "库存应映射为新系统200(Inventory)");
        assertEquals("300", AppConfig.newSystemCoverStatus(3), "出仓应映射为新系统300(Exit)");
    }

    @Test
    public void outOfRangeStatusMapsToNull() {
        assertNull(AppConfig.newSystemCoverStatus(-1), "越界状态不应映射");
        assertNull(AppConfig.newSystemCoverStatus(4), "越界状态不应映射");
        assertNull(AppConfig.newSystemCoverStatus(99), "越界状态不应映射");
    }

    @Test
    public void intervalConstantsKeepLegacyBehavior() {
        assertEquals(300L, AppConfig.LEGACY_SYSTEM_MIN_INTERVAL_MILLIS, "老系统间隔应保持300ms");
        assertEquals(120L, AppConfig.NEW_SYSTEM_MIN_INTERVAL_MILLIS, "新系统间隔应放宽到120ms");
    }
}
