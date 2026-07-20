package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdvancedFilterMatcher {
    public static final String OWNER = "owner";
    public static final String ENTRY_NUMBER = "entryNumber";
    public static final String JOB_NUMBER = "jobNumber";
    public static final String DRIVER = "driver";
    public static final String PLATE = "plate";
    public static final String CARGO = "cargo";
    public static final String MARK = "mark";
    public static final String PACKAGE = "package";
    public static final String DRIVER_PHONE = "driverPhone";

    private AdvancedFilterMatcher() {
    }

    public static boolean match(WarehouseEntry entry, Map<String, String> filters) {
        if (entry == null) {
            return false;
        }
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            boolean exact = MARK.equals(filter.getKey());
            List<String> terms = parseTerms(filter.getValue(), exact);
            if (!terms.isEmpty() && !matchesField(entry, filter.getKey(), terms)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> parseTerms(String input, boolean exact) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = input.trim().split("[\\s,，、;；]+");
        List<String> terms = new ArrayList<String>();
        for (String part : parts) {
            String value = part.trim();
            if (!value.isEmpty()) {
                terms.add(exact ? value : value.toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(terms);
    }

    private static boolean matchesField(WarehouseEntry entry, String key, List<String> terms) {
        switch (key) {
            case OWNER:
                return matchesAnySubstring(entry.getHz(), terms);
            case ENTRY_NUMBER:
                return matchesAnySubstring(entry.getJcbh(), terms);
            case JOB_NUMBER:
                return matchesAnySubstring(entry.getJczyh(), terms)
                    || matchesAnySubstring(entry.getZyh(), terms);
            case DRIVER:
                return matchesAnySubstring(entry.getJsy(), terms);
            case PLATE:
                return matchesAnySubstring(entry.getCh(), terms);
            case CARGO:
                return matchesAnySubstring(entry.getHwmc(), terms)
                    || matchesAnySubstring(entry.getHwmc1(), terms);
            case PACKAGE:
                return matchesAnySubstring(entry.getBzgg(), terms);
            case DRIVER_PHONE:
                return matchesAnySubstring(entry.getJsydh(), terms);
            case MARK:
                return matchesExact(entry.getMt(), terms);
            default:
                return true;
        }
    }

    private static boolean matchesAnySubstring(String source, List<String> terms) {
        if (source == null) {
            return false;
        }
        String normalized = source.toLowerCase(Locale.ROOT);
        return terms.stream().anyMatch(normalized::contains);
    }

    private static boolean matchesExact(String source, List<String> terms) {
        return source != null && terms.stream().anyMatch(source::equals);
    }
}
