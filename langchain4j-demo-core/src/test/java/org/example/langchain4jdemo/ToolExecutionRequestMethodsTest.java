package org.example.langchain4jdemo;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * 测试 ToolExecutionRequest 类有哪些方法
 */
public class ToolExecutionRequestMethodsTest {

    @Test
    public void printToolExecutionRequestMethods() {
        System.out.println("\n=== ToolExecutionRequest 类的所有方法 ===");

        Class<?> clazz = ToolExecutionRequest.class;

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
