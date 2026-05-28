package com.xunqin.service;

import java.util.List;
import java.util.Map;

public interface AIService {

    List<Map<String, Object>> searchByRegion(String region);

    String askQuestion(Long userId, String sessionId, String question);
}
