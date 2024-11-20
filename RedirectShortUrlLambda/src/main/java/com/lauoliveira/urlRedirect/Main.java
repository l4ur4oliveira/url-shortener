package com.lauoliveira.urlRedirect;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParams = (String) input.get("rawPath");
        String shortUrlCode = pathParams.replace("/", "");

        if (shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket("shorturl-lambda-storage")
                .key(shortUrlCode + ".json")
                .build();

        InputStream objectStream;

        try {
            objectStream = s3Client.getObject(request);
        } catch (Exception exception) {
            throw new RuntimeException("Error fetching data from S3: " + exception.getMessage(), exception);
        }

        UrlData urlData;
        try {
            urlData = objectMapper.readValue(objectStream, UrlData.class);
        } catch (Exception exception) {
            throw new RuntimeException("Error deserializing data: " + exception.getMessage(), exception);
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        Map<String, Object> response = new HashMap<>();

        if (urlData.getExpirationTime() < currentTimeInSeconds) {
            response.put("statusCode", 410);
            response.put("body", "This URL has expired.");

            return response;
        }

        response.put("statusCode", 302);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());
        response.put("headers", headers);

        return response;
    }
}