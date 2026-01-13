package org.example.langchain4jdemo.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * 内置工具类
 * 演示使用 @Tool 注解定义工具
 */
public class BuiltInTools {

    /**
     * 计算器工具
     */
    public static class Calculator {

        @Tool("执行数学计算表达式")
        public String calculate(
                @P("数学表达式，例如: 2+2, 10*5, (25+17)*3") String expression
        ) {
            try {
                ScriptEngine engine = new ScriptEngineManager()
                        .getEngineByName("JavaScript");
                Object result = engine.eval(expression);
                return String.format("计算结果：%s = %s", expression, result);
            } catch (Exception e) {
                return "计算错误：" + e.getMessage();
            }
        }

        @Tool("计算两个数字的和")
        public double add(
                @P("第一个数字") double a,
                @P("第二个数字") double b
        ) {
            return a + b;
        }

        @Tool("计算两个数字的差")
        public double subtract(
                @P("被减数") double a,
                @P("减数") double b
        ) {
            return a - b;
        }

        @Tool("计算两个数字的乘积")
        public double multiply(
                @P("第一个数字") double a,
                @P("第二个数字") double b
        ) {
            return a * b;
        }

        @Tool("计算两个数字的商")
        public double divide(
                @P("被除数") double a,
                @P("除数") double b
        ) {
            if (b == 0) {
                throw new IllegalArgumentException("除数不能为0");
            }
            return a / b;
        }

        @Tool("计算平方根")
        public double sqrt(@P("要计算平方根的数字") double number) {
            if (number < 0) {
                throw new IllegalArgumentException("不能计算负数的平方根");
            }
            return Math.sqrt(number);
        }

        @Tool("计算幂运算")
        public double power(
                @P("底数") double base,
                @P("指数") double exponent
        ) {
            return Math.pow(base, exponent);
        }
    }

    /**
     * 日期时间工具
     */
    public static class DateTime {

        @Tool("获取当前日期时间")
        public String getCurrentDateTime() {
            LocalDateTime now = LocalDateTime.now();
            return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Tool("获取当前日期")
        public String getCurrentDate() {
            LocalDate today = LocalDate.now();
            return today.format(DateTimeFormatter.ISO_DATE);
        }

        @Tool("获取星期几")
        public String getDayOfWeek(@P("日期，格式：yyyy-MM-dd") String dateStr) {
            LocalDate date = LocalDate.parse(dateStr);
            String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            return weekdays[date.getDayOfWeek().getValue() - 1];
        }

        @Tool("计算两个日期之间的天数")
        public long daysBetween(
                @P("开始日期，格式：yyyy-MM-dd") String startDate,
                @P("结束日期，格式：yyyy-MM-dd") String endDate
        ) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return ChronoUnit.DAYS.between(start, end);
        }

        @Tool("在指定日期上增加或减少天数")
        public String addDays(
                @P("起始日期，格式：yyyy-MM-dd") String dateStr,
                @P("要增加的天数，负数表示减少") int days
        ) {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDate result = date.plusDays(days);
            return result.format(DateTimeFormatter.ISO_DATE);
        }

        @Tool("判断是否是闰年")
        public boolean isLeapYear(@P("年份") int year) {
            return LocalDate.of(year, 1, 1).isLeapYear();
        }
    }

    /**
     * 文本处理工具
     */
    public static class TextProcessor {

        @Tool("统计文本中的字数")
        public int countWords(@P("要统计的文本") String text) {
            if (text == null || text.trim().isEmpty()) {
                return 0;
            }
            String[] words = text.trim().split("\\s+");
            return words.length;
        }

        @Tool("统计文本中的字符数")
        public int countCharacters(@P("要统计的文本") String text) {
            return text == null ? 0 : text.length();
        }

        @Tool("将文本转换为大写")
        public String toUpperCase(@P("要转换的文本") String text) {
            return text == null ? "" : text.toUpperCase();
        }

        @Tool("将文本转换为小写")
        public String toLowerCase(@P("要转换的文本") String text) {
            return text == null ? "" : text.toLowerCase();
        }

        @Tool("反转文本")
        public String reverseText(@P("要反转的文本") String text) {
            return text == null ? "" : new StringBuilder(text).reverse().toString();
        }

        @Tool("检查文本是否包含指定子串")
        public boolean contains(
                @P("源文本") String text,
                @P("要查找的子串") String substring
        ) {
            return text != null && text.contains(substring);
        }

        @Tool("替换文本中的内容")
        public String replace(
                @P("源文本") String text,
                @P("要替换的内容") String oldText,
                @P("新内容") String newText
        ) {
            return text == null ? "" : text.replace(oldText, newText);
        }
    }

    /**
     * 随机数工具
     */
    public static class RandomGenerator {

        private final Random random = new Random();

        @Tool("生成指定范围内的随机整数")
        public int randomInt(
                @P("最小值（包含）") int min,
                @P("最大值（包含）") int max
        ) {
            if (min > max) {
                throw new IllegalArgumentException("最小值不能大于最大值");
            }
            return random.nextInt(max - min + 1) + min;
        }

        @Tool("生成随机小数（0到1之间）")
        public double randomDouble() {
            return random.nextDouble();
        }

        @Tool("生成随机布尔值")
        public boolean randomBoolean() {
            return random.nextBoolean();
        }

        @Tool("从数组中随机选择一个元素")
        public String randomChoice(@P("选项列表，用逗号分隔") String options) {
            String[] items = options.split(",");
            if (items.length == 0) {
                throw new IllegalArgumentException("选项列表不能为空");
            }
            int index = random.nextInt(items.length);
            return items[index].trim();
        }
    }

    /**
     * 单位转换工具
     */
    public static class UnitConverter {

        @Tool("摄氏度转华氏度")
        public double celsiusToFahrenheit(@P("摄氏温度") double celsius) {
            return celsius * 9.0 / 5.0 + 32;
        }

        @Tool("华氏度转摄氏度")
        public double fahrenheitToCelsius(@P("华氏温度") double fahrenheit) {
            return (fahrenheit - 32) * 5.0 / 9.0;
        }

        @Tool("千米转英里")
        public double kmToMiles(@P("千米数") double km) {
            return km * 0.621371;
        }

        @Tool("英里转千米")
        public double milesToKm(@P("英里数") double miles) {
            return miles / 0.621371;
        }

        @Tool("千克转磅")
        public double kgToPounds(@P("千克数") double kg) {
            return kg * 2.20462;
        }

        @Tool("磅转千克")
        public double poundsToKg(@P("磅数") double pounds) {
            return pounds / 2.20462;
        }
    }

    /**
     * 数据验证工具
     */
    public static class Validator {

        @Tool("验证邮箱地址格式是否正确")
        public boolean isValidEmail(@P("邮箱地址") String email) {
            if (email == null || email.isEmpty()) {
                return false;
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            return email.matches(emailRegex);
        }

        @Tool("验证手机号码格式是否正确（中国大陆）")
        public boolean isValidPhoneNumber(@P("手机号码") String phone) {
            if (phone == null || phone.isEmpty()) {
                return false;
            }
            String phoneRegex = "^1[3-9]\\d{9}$";
            return phone.matches(phoneRegex);
        }

        @Tool("验证是否是有效的URL")
        public boolean isValidUrl(@P("URL地址") String url) {
            if (url == null || url.isEmpty()) {
                return false;
            }
            String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
            return url.matches(urlRegex);
        }

        @Tool("检查字符串是否为纯数字")
        public boolean isNumeric(@P("要检查的字符串") String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            return str.matches("-?\\d+(\\.\\d+)?");
        }
    }
}
