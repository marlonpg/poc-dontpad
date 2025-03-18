package com.gambasoftware.poc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NoteService {
    private final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final ConcurrentHashMap<String, String> notes = new ConcurrentHashMap<>();

    public String getNote(String path) {
        logger.info("get note by path {}", path);
        return notes.getOrDefault(path, "");
    }

    public void saveNote(String path, String content) {
        logger.info("save note by path {}", path);
        notes.put(path, content);
    }
}
