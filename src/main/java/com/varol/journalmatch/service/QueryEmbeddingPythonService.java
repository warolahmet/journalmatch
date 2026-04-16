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
            System.out.println("QUERY EMBEDDING: starting python process");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/opt/venv/bin/python",
                    "scripts/query_embedding.py",
                    text
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            System.out.println("QUERY EMBEDDING: python process started");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;

            System.out.println("QUERY EMBEDDING: reading python output");

            while ((line = reader.readLine()) != null) {
                System.out.println("PYTHON OUT: " + line);
                output.append(line);
            }

            System.out.println("QUERY EMBEDDING: waiting for process to finish");
            boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Query embedding process timed out");
            }

            int exitCode = process.exitValue();
            System.out.println("QUERY EMBEDDING: process finished with exitCode=" + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("Python embedding script failed: " + output);
            }

            return objectMapper.readValue(output.toString(), double[].class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate query embedding", e);
        }
    }
}