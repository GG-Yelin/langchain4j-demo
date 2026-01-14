package org.example.langchain4jdemo;

import dev.langchain4j.service.tool.ToolExecution;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * 测试 ToolExecution 类有哪些方法
 */
public class ToolExecutionMethodsTest {

    @Test
    public void printToolExecutionMethods() {
        System.out.println("\n=== ToolExecution 类的所有方法 ===");

        Class<?> clazz = ToolExecution.class;

        System.out.println("声明的方法:");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  - " + method.getName() + "(): " + method.getReturnType().getSimpleName());
        }

        System.out.println("\n所有public方法:");
        for (Method method : clazz.getMethods()) {
            if (method.getDeclaringClass() == clazz) {
                System.out.println("  - " + method.getName() + "(): " + method.getReturnType().getSimpleName());
            }
        }
    }
}
