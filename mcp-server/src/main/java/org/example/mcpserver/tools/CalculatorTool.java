package org.example.mcpserver.tools;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 * 使用 Spring AI MCP @McpTool 注解定义工具方法
 */
@Slf4j
@Component
public class CalculatorTool {

    @McpTool(
        name = "calculator_add",
        description = """
        A tool for adding two numbers together.
        This tool performs basic addition operation and returns the sum of two numbers.
        """
    )
    public double add(double a, double b) {
        log.info("Calculator Tool - Add: {} + {}", a, b);
        double result = a + b;
        log.info("Result: {}", result);
        return result;
    }

    @McpTool(
        name = "calculator_subtract",
        description = """
        A tool for subtracting second number from first number.
        This tool performs basic subtraction operation.
        """
    )
    public double subtract(double a, double b) {
        log.info("Calculator Tool - Subtract: {} - {}", a, b);
        double result = a - b;
        log.info("Result: {}", result);
        return result;
    }

    @McpTool(
        name = "calculator_multiply",
        description = """
        A tool for multiplying two numbers together.
        This tool performs basic multiplication operation and returns the product.
        """
    )
    public double multiply(double a, double b) {
        log.info("Calculator Tool - Multiply: {} * {}", a, b);
        double result = a * b;
        log.info("Result: {}", result);
        return result;
    }

    @McpTool(
        name = "calculator_divide",
        description = """
        A tool for dividing first number by second number.
        This tool performs basic division operation.
        The divisor cannot be zero, otherwise an error will be thrown.
        """
    )
    public double divide(double a, double b) {
        log.info("Calculator Tool - Divide: {} / {}", a, b);

        if (b == 0) {
            log.error("Division by zero error: divisor = {}", b);
            throw new IllegalArgumentException("Divisor cannot be zero. Please provide a non-zero divisor.");
        }

        double result = a / b;
        log.info("Result: {}", result);
        return result;
    }

    @McpTool(
        name = "calculator_power",
        description = """
        A tool for calculating power: base raised to the exponent.
        This tool performs exponentiation operation using Math.pow().
        For example: power(2, 3) returns 8 (2^3 = 8).
        """
    )
    public double power(double base, double exponent) {
        log.info("Calculator Tool - Power: {} ^ {}", base, exponent);
        double result = Math.pow(base, exponent);
        log.info("Result: {}", result);
        return result;
    }

    @McpTool(
        name = "calculator_sqrt",
        description = """
        A tool for calculating square root of a number.
        This tool computes the square root using Math.sqrt().
        The input number must be non-negative, otherwise an error will be thrown.
        """
    )
    public double sqrt(double number) {
        log.info("Calculator Tool - Square Root: √{}", number);

        if (number < 0) {
            log.error("Square root of negative number error: number = {}", number);
            throw new IllegalArgumentException("Cannot calculate square root of negative number: " + number);
        }

        double result = Math.sqrt(number);
        log.info("Result: {}", result);
        return result;
    }
}
