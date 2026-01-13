package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ToolCallRequest;
import org.example.langchain4jdemo.dto.ToolCallResponse;
import org.example.langchain4jdemo.service.ToolService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final OpenAiChatModel chatModel;

    @Override
    public ToolCallResponse chatWithTools(ToolCallRequest request) {
        try {
            // TODO: 在这里实现带工具调用的聊天
            // 方式1: 使用 @Tool 注解定义工具
            // 示例:
            // public class Calculator {
            //     @Tool("计算两个数的和")
            //     public int add(int a, int b) {
            //         return a + b;
            //     }
            // }
            //
            // interface Assistant {
            //     String chat(String message);
            // }
            //
            // Assistant assistant = AiServices.builder(Assistant.class)
            //     .chatLanguageModel(chatModel)
            //     .tools(new Calculator())
            //     .build();
            // String response = assistant.chat(request.getMessage());

            // 方式2: 动态定义工具
            // ToolSpecification toolSpec = ToolSpecification.builder()
            //     .name("get_weather")
            //     .description("获取指定城市的天气")
            //     .addParameter("city", JsonSchemaProperty.STRING, JsonSchemaProperty.description("城市名称"))
            //     .build();

            throw new UnsupportedOperationException("请实现 chatWithTools 方法");

        } catch (Exception e) {
            log.error("Tool call error", e);
            return ToolCallResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
