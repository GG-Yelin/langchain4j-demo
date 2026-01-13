package org.example.langchain4jdemo;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.example.langchain4jdemo.service.BuiltInToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * 内置工具测试
 * 演示各种工具的使用场景
 */
public class BuiltInToolsTest {

    private BuiltInToolService service;

    @BeforeEach
    public void setUp() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        service = new BuiltInToolService(chatModel);
    }

    @Test
    public void testCalculator() {
        System.out.println("\n=== 测试计算器工具 ===\n");

        // 测试简单计算
        String result1 = service.chatWithCalculator("计算 25 + 17 的结果");
        System.out.println("Q: 计算 25 + 17 的结果");
        System.out.println("A: " + result1);

        // 测试复杂表达式
        String result2 = service.chatWithCalculator("帮我算一下 (100 - 25) * 3 + 50");
        System.out.println("\nQ: 帮我算一下 (100 - 25) * 3 + 50");
        System.out.println("A: " + result2);

        // 测试平方根
        String result3 = service.chatWithCalculator("144 的平方根是多少？");
        System.out.println("\nQ: 144 的平方根是多少？");
        System.out.println("A: " + result3);
    }

    @Test
    public void testDateTime() {
        System.out.println("\n=== 测试日期时间工具 ===\n");

        // 获取当前时间
        String result1 = service.chatWithDateTime("现在几点了？");
        System.out.println("Q: 现在几点了？");
        System.out.println("A: " + result1);

        // 计算日期差
        String result2 = service.chatWithDateTime("从 2026-01-01 到 2026-12-31 有多少天？");
        System.out.println("\nQ: 从 2026-01-01 到 2026-12-31 有多少天？");
        System.out.println("A: " + result2);

        // 判断星期几
        String result3 = service.chatWithDateTime("2026-01-13 是星期几？");
        System.out.println("\nQ: 2026-01-13 是星期几？");
        System.out.println("A: " + result3);
    }

    @Test
    public void testTextProcessor() {
        System.out.println("\n=== 测试文本处理工具 ===\n");

        // 统计字数
        String result1 = service.chatWithTextProcessor("'Hello World from LangChain4j' 有几个单词？");
        System.out.println("Q: 'Hello World from LangChain4j' 有几个单词？");
        System.out.println("A: " + result1);

        // 反转文本
        String result2 = service.chatWithTextProcessor("把 'LangChain4j' 反过来写是什么？");
        System.out.println("\nQ: 把 'LangChain4j' 反过来写是什么？");
        System.out.println("A: " + result2);

        // 大小写转换
        String result3 = service.chatWithTextProcessor("把 'hello world' 转换成大写");
        System.out.println("\nQ: 把 'hello world' 转换成大写");
        System.out.println("A: " + result3);
    }

    @Test
    public void testAllTools() {
        System.out.println("\n=== 测试所有工具组合 ===\n");

        // 复杂查询，可能需要多个工具
        String result1 = service.chatWithAllTools(
                "今天是2026年1月13日，计算到春节（2026年1月29日）还有多少天，" +
                "然后用这个天数乘以24得出总小时数"
        );
        System.out.println("Q: 复杂计算（日期 + 数学）");
        System.out.println("A: " + result1);

        // 数据验证 + 文本处理
        String result2 = service.chatWithAllTools(
                "检查 'test@example.com' 是否是有效的邮箱，" +
                "如果是，把它转换成大写"
        );
        System.out.println("\nQ: 邮箱验证 + 文本处理");
        System.out.println("A: " + result2);

        // 单位转换 + 计算
        String result3 = service.chatWithAllTools(
                "把 25 摄氏度转换成华氏度，然后加上 10"
        );
        System.out.println("\nQ: 温度转换 + 计算");
        System.out.println("A: " + result3);
    }

    @Test
    public void testCustomTools() {
        System.out.println("\n=== 测试自定义工具组合 ===\n");

        // 只使用计算器和单位转换
        String result1 = service.chatWithCustomTools(
                "100 千米等于多少英里？如果速度是每小时 100 千米，等于多少英里每小时？",
                new String[]{"calculator", "converter"}
        );
        System.out.println("Q: 距离和速度单位转换");
        System.out.println("A: " + result1);

        // 只使用文本和验证工具
        String result2 = service.chatWithCustomTools(
                "检查 'invalid-email' 是否是有效邮箱，并统计它有多少个字符",
                new String[]{"text", "validator"}
        );
        System.out.println("\nQ: 邮箱验证 + 字符统计");
        System.out.println("A: " + result2);
    }
}
