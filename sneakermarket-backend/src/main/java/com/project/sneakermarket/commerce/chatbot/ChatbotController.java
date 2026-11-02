package com.project.sneakermarket.commerce.chatbot;

import com.project.sneakermarket.common.security.InMemoryRequestRateLimiter;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final InMemoryRequestRateLimiter rateLimiter;


    @PostMapping("/api/chatbot/ask")
    public ChatResponse ask(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        rateLimiter.check(
                "chatbot",
                httpRequest.getRemoteAddr(),
                10,
                Duration.ofMinutes(1)
        );
        String answer = chatbotService.ask(request.getQuestion());
        return new ChatResponse(answer);
    }
}
