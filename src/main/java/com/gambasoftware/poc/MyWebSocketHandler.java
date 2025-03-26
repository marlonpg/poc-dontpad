package com.gambasoftware.poc;

import com.gambasoftware.poc.services.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static final ConcurrentHashMap<String, List<WebSocketSession>> sessionsByPath = new ConcurrentHashMap<>();
    private final NoteService noteService;

    public MyWebSocketHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("Connection established session {}", session);

        String path = extractPath(session);
        logger.info("Connection established with path {}", path);
        sessionsByPath.putIfAbsent(path, Collections.synchronizedList(new ArrayList<WebSocketSession>()));
        sessionsByPath.get(path).add(session);

        String content = noteService.getNote(path);

        session.sendMessage(new TextMessage(content));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Processing message {}", message.getPayload());
        try {
            String path = extractPath(session);
            String text = message.getPayload();

            sessionsByPath.putIfAbsent(path, Collections.synchronizedList(new ArrayList<WebSocketSession>()));
            sessionsByPath.get(path).add(session);

            noteService.saveNote(path, text);
            List<WebSocketSession> list = sessionsByPath.get(path);
            synchronized(list) {
                for (WebSocketSession s : list) {
                    if (s.isOpen() && s != session) {
                        s.sendMessage(message);
                    }
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

    private String extractPath(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.split("/ws")[1];
    }
}