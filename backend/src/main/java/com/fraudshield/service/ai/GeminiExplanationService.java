package com.fraudshield.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.entity.RiskFactor;
import com.fraudshield.entity.Transaction;
import com.fraudshield.service.fraud.FraudScoreResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

@Service
@Primary
public class GeminiExplanationService implements AiExplanationService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiExplanationService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    private final RuleBasedExplanationService fallbackService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiExplanationService(RuleBasedExplanationService fallbackService, ObjectMapper objectMapper) {
        this.fallbackService = fallbackService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String generateExplanation(Transaction transaction, FraudScoreResult scoreResult) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.debug("GEMINI_API_KEY is not configured. Utilizing local heuristic explanation engine.");
            return fallbackService.generateExplanation(transaction, scoreResult);
        }

        try {
            String prompt = buildPrompt(transaction, scoreResult);
            String requestBody = objectMapper.writeValueAsString(new GeminiPayload(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey.trim()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidateText = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");

                if (!candidateText.isMissingNode() && !candidateText.asText().isBlank()) {
                    return candidateText.asText().trim();
                }
            } else {
                logger.warn("Gemini API call returned status {}: {}. Falling back to rule-based explanation.",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.warn("Failed to communicate with Gemini API ({}: {}). Executing resilient local fallback.",
                    e.getClass().getSimpleName(), e.getMessage());
        }

        return fallbackService.generateExplanation(transaction, scoreResult);
    }

    private String buildPrompt(Transaction transaction, FraudScoreResult scoreResult) {
        String factors = scoreResult.getRiskFactors().stream()
                .map(RiskFactor::getDescription)
                .collect(Collectors.joining("; "));

        return String.format(
                "You are an expert fraud risk analyst at a tier-1 fintech institution. " +
                "Summarize in exactly 2-3 concise, professional, clear sentences why the following transaction was assessed as %s risk (Risk Score: %d/100). " +
                "Transaction Reference: %s, Amount: %s %s, Merchant Category: %s, Location: %s, Usual Location: %s, New Device: %s, Failed Attempts: %d. " +
                "Identified Risk Factors: [%s]. " +
                "Do NOT use bullet points or markdown headings. Write in fluent, objective natural language.",
                scoreResult.getRiskLevel(),
                scoreResult.getRiskScore(),
                transaction.getTransactionReference(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantCategory(),
                transaction.getLocation(),
                transaction.getUsualLocation(),
                transaction.isNewDevice() ? "Yes" : "No",
                transaction.getFailedAttempts(),
                factors
        );
    }

    // Helper structure for Gemini JSON payload
    private static class GeminiPayload {
        public Content[] contents;
        public GenerationConfig generationConfig = new GenerationConfig();

        public GeminiPayload(String promptText) {
            this.contents = new Content[]{new Content(promptText)};
        }

        public static class Content {
            public Part[] parts;

            public Content(String text) {
                this.parts = new Part[]{new Part(text)};
            }
        }

        public static class Part {
            public String text;

            public Part(String text) {
                this.text = text;
            }
        }

        public static class GenerationConfig {
            public double temperature = 0.2;
            public int maxOutputTokens = 250;
        }
    }
}
