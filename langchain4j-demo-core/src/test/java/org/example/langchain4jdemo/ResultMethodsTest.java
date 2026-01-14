package org.example.langchain4jdemo;

import dev.langchain4j.service.Result;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * 测试 Result 类有哪些方法
 */
public class ResultMethodsTest {

    @Test
    public void printResultMethods() {
        System.out.println("\n=== Result 类的所有方法 ===");

        Class<?> clazz = Result.class;

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
