package com.agencia.chatbot.config.advisors;

import com.agencia.chatbot.utils.ToolCallEvent;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Dentro del loop de ToolCallingAdvisor. Un instance por request.
 */
public class ToolCallObservingAdvisor implements CallAdvisor, StreamAdvisor {

    public static final int ORDER = ToolCallingAdvisor.DEFAULT_ORDER + 100;

    private final Consumer<ToolCallEvent> listener;
    private final Set<String> started = ConcurrentHashMap.newKeySet();
    private final Set<String> completed = ConcurrentHashMap.newKeySet();

    public ToolCallObservingAdvisor(Consumer<ToolCallEvent> listener) {
        this.listener = listener;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest request,
            StreamAdvisorChain chain) {

        emitCompletions(request);

        return chain.nextStream(request)
                .doOnNext(this::emitStarts);
    }

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        emitCompletions(request);

        ChatClientResponse response = chain.nextCall(request);

        emitStarts(response);

        return response;
    }

    private void emitStarts(ChatClientResponse response) {

        ChatResponse chatResponse = response.chatResponse();

        if (chatResponse == null) {
            return;
        }

        chatResponse.getResults().stream()
                .flatMap(g -> g.getOutput().getToolCalls().stream())
                .filter(tc -> StringUtils.hasText(tc.name()))
                .forEach(tc -> {

                    String key = resolveToolKey(
                            tc.id(),
                            tc.name(),
                            tc.arguments()
                    );

                    if (started.add(key)) {
                        listener.accept(
                                new ToolCallEvent(
                                        tc.id(),
                                        tc.name(),
                                        ToolCallEvent.Phase.STARTED
                                )
                        );
                    }
                });
    }

    private void emitCompletions(ChatClientRequest request) {
        request.prompt().getInstructions().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(msg -> msg.getResponses().stream())
                .filter(r -> StringUtils.hasText(r.name()))
                .forEach(r -> {

                    String key = resolveToolKey(
                            r.id(),
                            r.name(),
                            null
                    );

                    if (started.contains(key) && completed.add(key)) {
                        listener.accept(
                                new ToolCallEvent(
                                        r.id(),
                                        r.name(),
                                        ToolCallEvent.Phase.COMPLETED
                                )
                        );
                    }
                });
    }

    private String resolveToolKey(
            String id,
            String name,
            String arguments) {

        if (StringUtils.hasText(id)) {
            return id;
        }

        return name + ":" + (arguments != null ? arguments : "");
    }

    @Override
    public String getName() {
        return "toolCallObservingAdvisor";
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}