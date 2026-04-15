package com.varol.journalmatch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class QueryEmbeddingPythonService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getQueryEmbedding(String text) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/opt/venv/bin/python",
                    "scripts/query_embedding.py",
                    text
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Python embedding script failed: " + output);
            }

            return objectMapper.readValue(output.toString(), double[].class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate query embedding", e);
        }
    }
}