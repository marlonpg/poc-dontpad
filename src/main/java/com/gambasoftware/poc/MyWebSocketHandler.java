package com.gambasoftware.poc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gambasoftware.poc.services.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> sessionsByPath = new ConcurrentHashMap<>();
    private final NoteService noteService;

    public MyWebSocketHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Connection established session {}", session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Processing message {}", message.getPayload());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String path = jsonNode.get("path").asText();
            String text = jsonNode.get("text").asText();

            sessionsByPath.putIfAbsent(path, new CopyOnWriteArrayList<>());
            sessionsByPath.get(path).add(session);

            noteService.saveNote(path, text);

            for (WebSocketSession s : sessionsByPath.getOrDefault(path, new CopyOnWriteArrayList<>())) {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing message");
            session.sendMessage(new TextMessage("Error processing message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connection closed session {}, status {}", session, status);
    }
}