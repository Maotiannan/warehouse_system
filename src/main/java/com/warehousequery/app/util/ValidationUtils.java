package com.warehousequery.app.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 数据验证工具类
 * 提供各种输入数据的验证功能
 */
public class ValidationUtils {
    
    // 进仓编号格式验证正则表达式
    private static final Pattern JCBH_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    
    // 最大进仓编号长度
    private static final int MAX_JCBH_LENGTH = 50;
    
    // 最大日期范围（天）
    private static final long MAX_DATE_RANGE_DAYS = 180;
    
    /**
     * 验证进仓编号格式
     * @param jcbh 进仓编号
     * @return 验证结果
     */
    public static ValidationResult validateJcbh(String jcbh) {
        if (jcbh == null || jcbh.trim().isEmpty()) {
            return ValidationResult.error("进仓编号不能为空");
        }
        
        String trimmedJcbh = jcbh.trim();
        
        if (trimmedJcbh.length() > MAX_JCBH_LENGTH) {
            return ValidationResult.error("进仓编号长度不能超过" + MAX_JCBH_LENGTH + "个字符");
        }
        
        if (!JCBH_PATTERN.matcher(trimmedJcbh).matches()) {
            return ValidationResult.error("进仓编号只能包含字母和数字");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证进仓编号列表
     * @param jcbhList 进仓编号列表
     * @return 验证结果
     */
    public static ValidationResult validateJcbhList(List<String> jcbhList) {
        if (jcbhList == null || jcbhList.isEmpty()) {
            return ValidationResult.error("请输入至少一个进仓编号");
        }
        
        if (jcbhList.size() > 100) {
            return ValidationResult.error("一次最多只能查询100个进仓编号");
        }
        
        for (int i = 0; i < jcbhList.size(); i++) {
            String jcbh = jcbhList.get(i);
            ValidationResult result = validateJcbh(jcbh);
            if (!result.isValid()) {
                return ValidationResult.error("第" + (i + 1) + "个进仓编号无效: " + result.getErrorMessage());
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证日期范围
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 验证结果
     */
    public static ValidationResult validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            return ValidationResult.error("开始日期不能为空");
        }
        
        if (endDate == null) {
            return ValidationResult.error("结束日期不能为空");
        }
        
        if (startDate.isAfter(endDate)) {
            return ValidationResult.error("开始日期不能晚于结束日期");
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_DATE_RANGE_DAYS) {
            return ValidationResult.error("查询日期范围不能超过" + MAX_DATE_RANGE_DAYS + "天");
        }
        
        // 检查日期是否过于久远
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        if (endDate.isBefore(twoYearsAgo)) {
            return ValidationResult.warning("查询的日期范围过于久远，可能没有数据");
        }
        
        // 检查日期是否在未来
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            return ValidationResult.error("开始日期不能是未来日期");
        }
        
        if (endDate.isAfter(today.plusDays(1))) {
            return ValidationResult.error("结束日期不能超过明天");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证状态索引
     * @param statusIndex 状态索引
     * @return 验证结果
     */
    public static ValidationResult validateStatusIndex(int statusIndex) {
        if (statusIndex < 0 || statusIndex >= 4) {
            return ValidationResult.error("无效的状态选择");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 清理和标准化进仓编号
     * @param jcbh 原始进仓编号
     * @return 清理后的进仓编号
     */
    public static String sanitizeJcbh(String jcbh) {
        if (jcbh == null) {
            return "";
        }
        
        // 去除首尾空白字符
        String cleaned = jcbh.trim();
        
        // 去除中间的空白字符
        cleaned = cleaned.replaceAll("\\s+", "");
        
        // 转换为大写（如果需要）
        // cleaned = cleaned.toUpperCase();
        
        return cleaned;
    }
    
    /**
     * 验证结果类
     */
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
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public ValidationLevel getLevel() {
            return level;
        }
        
        public boolean hasWarning() {
            return level == ValidationLevel.WARNING;
        }
    }
    
    /**
     * 验证级别枚举
     */
    public enum ValidationLevel {
        SUCCESS,
        WARNING,
        ERROR
    }
} 