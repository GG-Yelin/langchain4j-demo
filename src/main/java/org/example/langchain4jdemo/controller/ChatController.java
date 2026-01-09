package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.langchain4jdemo.dto.ChatRequest;
import org.example.langchain4jdemo.dto.ChatResponse;
import org.example.langchain4jdemo.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 简单聊天
     * POST /api/chat/simple
     */
    @PostMapping("/simple")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    /**
     * 带记忆的多轮对话
     * POST /api/chat/memory
     */
    @PostMapping("/memory")
    public ChatResponse chatWithMemory(@RequestBody ChatRequest request) {
        return chatService.chatWithMemory(request);
    }

    /**
     * 流式聊天 (Server-Sent Events)
     * GET /api/chat/stream
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        chatService.streamChat(request, new ChatService.StreamCallback() {
            @Override
            public void onToken(String token) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("token")
                            .data(token));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(ChatResponse response) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(response));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }
}
