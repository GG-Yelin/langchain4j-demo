package org.example.langchain4jdemo;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.example.langchain4jdemo.dto.ToolCallRequestVo;
import org.example.langchain4jdemo.dto.ToolCallResponse;
import org.example.langchain4jdemo.service.impl.ToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

/**
 * 工具调用服务测试
 * 演示 ToolSpecification 动态工具调用
 */
public class ToolServiceTest {

    private ToolServiceImpl toolService;

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

        toolService = new ToolServiceImpl(chatModel);
    }

    @Test
    public void testCalculatorTool() {
        System.out.println("\n=== 测试计算器工具 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("计算 25 + 17 的结果");
        request.setEnabledTools(List.of("calculator"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());

        if (response.getToolExecutions() != null && !response.getToolExecutions().isEmpty()) {
            System.out.println("\n工具调用详情:");
            for (ToolCallResponse.ToolExecution execution : response.getToolExecutions()) {
                System.out.println("  工具名称: " + execution.getToolName());
                System.out.println("  参数: " + execution.getArguments());
                System.out.println("  结果: " + execution.getResult());
                System.out.println();
            }
        } else {
            System.out.println("\n未调用工具");
        }
    }

    @Test
    public void testComplexCalculation() {
        System.out.println("\n=== 测试复杂计算 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("先计算 100 减 25，然后把结果乘以 3，最后加上 50");
        request.setEnabledTools(List.of("calculator"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());

        if (response.getToolExecutions() != null && !response.getToolExecutions().isEmpty()) {
            System.out.println("\n工具调用序列 (共 " + response.getToolExecutions().size() + " 次):");
            int index = 1;
            for (ToolCallResponse.ToolExecution execution : response.getToolExecutions()) {
                System.out.println(index++ + ". " + execution.getToolName());
                System.out.println("   参数: " + execution.getArguments());
                System.out.println("   结果: " + execution.getResult());
            }
        }
    }

    @Test
    public void testDateTimeTool() {
        System.out.println("\n=== 测试日期时间工具 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("现在几点了？从今天到2026年12月31日还有多少天？");
        request.setEnabledTools(List.of("datetime"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }

    @Test
    public void testTextProcessorTool() {
        System.out.println("\n=== 测试文本处理工具 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("'Hello World from LangChain4j' 有几个单词？把它转换成大写");
        request.setEnabledTools(List.of("text"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }

    @Test
    public void testValidatorTool() {
        System.out.println("\n=== 测试数据验证工具 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("检查 test@example.com 是否是有效邮箱，检查 13800138000 是否是有效手机号");
        request.setEnabledTools(List.of("validator"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }

    @Test
    public void testUnitConverterTool() {
        System.out.println("\n=== 测试单位转换工具 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("把 25 摄氏度转换成华氏度，把 100 千米转换成英里");
        request.setEnabledTools(List.of("converter"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }

    @Test
    public void testMultipleToolTypes() {
        System.out.println("\n=== 测试多种工具组合 ===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("计算 25 + 17 的结果，然后检查 test@example.com 是否是有效邮箱");
        request.setEnabledTools(List.of("calculator", "validator"));

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }

    @Test
    public void testAllTools() {
        System.out.println("\n=== 测试所有工具（默认）===\n");

        ToolCallRequestVo request = new ToolCallRequestVo();
        request.setMessage("计算 100 除以 4，然后把结果转换成英尺");
        // 不设置 enabledTools，使用所有默认工具

        ToolCallResponse response = toolService.chatWithTools(request);

        System.out.println("用户: " + request.getMessage());
        System.out.println("成功: " + response.isSuccess());
        System.out.println("回复: " + response.getContent());
    }
}
