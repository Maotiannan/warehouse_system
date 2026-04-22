/*
 * Decompiled with CFR 0.152.
 */
package com.warehousequery.app.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern JCBH_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final int MAX_JCBH_LENGTH = 50;
    private static final long MAX_DATE_RANGE_DAYS = 180L;

    public static ValidationResult validateJcbh(String jcbh) {
        if (jcbh == null || jcbh.trim().isEmpty()) {
            return ValidationResult.error("\u8fdb\u4ed3\u7f16\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        String trimmedJcbh = jcbh.trim();
        if (trimmedJcbh.length() > 50) {
            return ValidationResult.error("\u8fdb\u4ed3\u7f16\u53f7\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc750\u4e2a\u5b57\u7b26");
        }
        if (!JCBH_PATTERN.matcher(trimmedJcbh).matches()) {
            return ValidationResult.error("\u8fdb\u4ed3\u7f16\u53f7\u53ea\u80fd\u5305\u542b\u5b57\u6bcd\u548c\u6570\u5b57");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateJcbhList(List<String> jcbhList) {
        if (jcbhList == null || jcbhList.isEmpty()) {
            return ValidationResult.error("\u8bf7\u8f93\u5165\u81f3\u5c11\u4e00\u4e2a\u8fdb\u4ed3\u7f16\u53f7");
        }
        if (jcbhList.size() > 100) {
            return ValidationResult.error("\u4e00\u6b21\u6700\u591a\u53ea\u80fd\u67e5\u8be2100\u4e2a\u8fdb\u4ed3\u7f16\u53f7");
        }
        for (int i = 0; i < jcbhList.size(); ++i) {
            String jcbh = jcbhList.get(i);
            ValidationResult result = ValidationUtils.validateJcbh(jcbh);
            if (result.isValid()) continue;
            return ValidationResult.error("\u7b2c" + (i + 1) + "\u4e2a\u8fdb\u4ed3\u7f16\u53f7\u65e0\u6548: " + result.getErrorMessage());
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            return ValidationResult.error("\u5f00\u59cb\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (endDate == null) {
            return ValidationResult.error("\u7ed3\u675f\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (startDate.isAfter(endDate)) {
            return ValidationResult.error("\u5f00\u59cb\u65e5\u671f\u4e0d\u80fd\u665a\u4e8e\u7ed3\u675f\u65e5\u671f");
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 180L) {
            return ValidationResult.error("\u67e5\u8be2\u65e5\u671f\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929");
        }
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2L);
        if (endDate.isBefore(twoYearsAgo)) {
            return ValidationResult.warning("\u67e5\u8be2\u7684\u65e5\u671f\u8303\u56f4\u8fc7\u4e8e\u4e45\u8fdc\uff0c\u53ef\u80fd\u6ca1\u6709\u6570\u636e");
        }
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            return ValidationResult.error("\u5f00\u59cb\u65e5\u671f\u4e0d\u80fd\u662f\u672a\u6765\u65e5\u671f");
        }
        if (endDate.isAfter(today.plusDays(1L))) {
            return ValidationResult.error("\u7ed3\u675f\u65e5\u671f\u4e0d\u80fd\u8d85\u8fc7\u660e\u5929");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateStatusIndex(int statusIndex) {
        if (statusIndex < 0 || statusIndex >= 4) {
            return ValidationResult.error("\u65e0\u6548\u7684\u72b6\u6001\u9009\u62e9");
        }
        return ValidationResult.success();
    }

    public static String sanitizeJcbh(String jcbh) {
        if (jcbh == null) {
            return "";
        }
        String cleaned = jcbh.trim();
        cleaned = cleaned.replaceAll("\\s+", "");
        return cleaned;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final ValidationLevel level;

        private ValidationResult(boolean valid, String errorMessage, ValidationLevel level) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.level = level;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null, ValidationLevel.SUCCESS);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, ValidationLevel.ERROR);
        }

        public static ValidationResult warning(String message) {
            return new ValidationResult(true, message, ValidationLevel.WARNING);
        }

        public boolean isValid() {
            return this.valid;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public ValidationLevel getLevel() {
            return this.level;
        }

        public boolean hasWarning() {
            return this.level == ValidationLevel.WARNING;
        }
    }

    public static enum ValidationLevel {
        SUCCESS,
        WARNING,
        ERROR;

    }
}
