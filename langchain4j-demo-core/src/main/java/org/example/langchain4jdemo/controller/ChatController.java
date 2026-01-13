package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ChatRequestVO;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
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
    public ChatResponseVO chat(@RequestBody ChatRequestVO request) {
        return chatService.chat(request);
    }

    /**
     * 带记忆的多轮对话
     * POST /api/chat/memory
     */
    @PostMapping("/memory")
    public ChatResponseVO chatWithMemory(@RequestBody ChatRequestVO request) {
        return chatService.chatWithMemory(request);
    }

    /**
     * 流式聊天 (Server-Sent Events)
     * GET /api/chat/stream
     */
    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChat(@RequestBody ChatRequestVO request) {
        log.info("Stream chat request received: {}", request.getMessage());
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        // 在异步线程中执行流式响应，避免阻塞主线程
        new Thread(() -> {
            chatService.streamChat(request, new ChatService.StreamCallback() {
                @Override
                public void onToken(String token) {
                    try {
                        log.debug("Sending token to client: [{}]", token);
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(token, MediaType.TEXT_PLAIN));
                    } catch (Exception e) {
                        log.error("Error sending token", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(ChatResponseVO response) {
                    try {
                        log.info("Stream chat complete, sending final response");
                        emitter.send(SseEmitter.event()
                                .name("complete")
                                .data(response));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Error completing stream", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("Stream chat error", error);
                    emitter.completeWithError(error);
                }
            });
        }).start();

        return emitter;
    }
}
