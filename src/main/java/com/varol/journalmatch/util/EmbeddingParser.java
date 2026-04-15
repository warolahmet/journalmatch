package com.varol.journalmatch.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EmbeddingParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static double[] parse(String json) {
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}