package org.example.langchain4jdemo.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 * 提供基本的数学计算功能
 */
@Slf4j
@Component
public class CalculatorTool {

    /**
     * 加法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    @Tool("执行加法运算，计算两个数的和")
    public double add(double a, double b) {
        log.info("计算: {} + {}", a, b);
        double result = a + b;
        log.info("结果: {}", result);
        return result;
    }

    /**
     * 减法运算
     *
     * @param a 被减数
     * @param b 减数
     * @return 差
     */
    @Tool("执行减法运算，计算两个数的差")
    public double subtract(double a, double b) {
        log.info("计算: {} - {}", a, b);
        double result = a - b;
        log.info("结果: {}", result);
        return result;
    }

    /**
     * 乘法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之积
     */
    @Tool("执行乘法运算，计算两个数的积")
    public double multiply(double a, double b) {
        log.info("计算: {} * {}", a, b);
        double result = a * b;
        log.info("结果: {}", result);
        return result;
    }

    /**
     * 除法运算
     *
     * @param a 被除数
     * @param b 除数
     * @return 商
     */
    @Tool("执行除法运算，计算两个数相除的结果")
    public double divide(double a, double b) {
        log.info("计算: {} / {}", a, b);

        if (b == 0) {
            log.error("除数不能为0");
            throw new IllegalArgumentException("除数不能为0");
        }

        double result = a / b;
        log.info("结果: {}", result);
        return result;
    }

    /**
     * 幂运算
     *
     * @param base 底数
     * @param exponent 指数
     * @return base 的 exponent 次方
     */
    @Tool("执行幂运算，计算一个数的指定次方")
    public double power(double base, double exponent) {
        log.info("计算: {} ^ {}", base, exponent);
        double result = Math.pow(base, exponent);
        log.info("结果: {}", result);
        return result;
    }

    /**
     * 平方根运算
     *
     * @param number 被开方数
     * @return 平方根
     */
    @Tool("执行平方根运算，计算一个数的平方根")
    public double sqrt(double number) {
        log.info("计算: √{}", number);

        if (number < 0) {
            log.error("不能对负数开平方根");
            throw new IllegalArgumentException("不能对负数开平方根");
        }

        double result = Math.sqrt(number);
        log.info("结果: {}", result);
        return result;
    }
}
