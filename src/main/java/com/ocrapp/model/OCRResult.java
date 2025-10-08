package com.ocrapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing the result of an OCR operation.
 * Contains extracted text, metadata, and confidence information.
 */
public class OCRResult {
    
    private String extractedText;
    private float confidenceScore;
    private LocalDateTime timestamp;
    private String sourceImagePath;
    private int characterCount;
    private int wordCount;
    
    /**
     * Default constructor
     */
    public OCRResult() {
        this.timestamp = LocalDateTime.now();
        this.extractedText = "";
        this.confidenceScore = 0.0f;
        this.sourceImagePath = "";
        this.characterCount = 0;
        this.wordCount = 0;
    }
    
    /**
     * Constructor with extracted text
     * @param extractedText The text extracted from the image
     */
    public OCRResult(String extractedText) {
        this();
        this.extractedText = extractedText;
        calculateStats();
    }
    
    /**
     * Full constructor
     * @param extractedText The text extracted from the image
     * @param confidenceScore OCR confidence score (0-100)
     * @param sourceImagePath Path to the source image file
     */
    public OCRResult(String extractedText, float confidenceScore, String sourceImagePath) {
        this.extractedText = extractedText;
        this.confidenceScore = confidenceScore;
        this.sourceImagePath = sourceImagePath;
        this.timestamp = LocalDateTime.now();
        calculateStats();
    }
    
    /**
     * Calculate character and word counts from extracted text
     */
    private void calculateStats() {
        if (extractedText != null && !extractedText.isEmpty()) {
            this.characterCount = extractedText.length();
            this.wordCount = extractedText.trim().split("\\s+").length;
        } else {
            this.characterCount = 0;
            this.wordCount = 0;
        }
    }
    
    // Getters and Setters
    
    public String getExtractedText() {
        return extractedText;
    }
    
    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
        calculateStats();
    }
    
    public float getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(float confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSourceImagePath() {
        return sourceImagePath;
    }
    
    public void setSourceImagePath(String sourceImagePath) {
        this.sourceImagePath = sourceImagePath;
    }
    
    public int getCharacterCount() {
        return characterCount;
    }
    
    public int getWordCount() {
        return wordCount;
    }
    
    /**
     * Get formatted timestamp string
     * @return Formatted timestamp (yyyy-MM-dd HH:mm:ss)
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    /**
     * Check if OCR result contains valid text
     * @return true if extracted text is not empty
     */
    public boolean hasText() {
        return extractedText != null && !extractedText.trim().isEmpty();
    }
    
    /**
     * Get a summary of the OCR result
     * @return Summary string with stats
     */
    public String getSummary() {
        return String.format("OCR Result - Characters: %d, Words: %d, Confidence: %.2f%%",
                characterCount, wordCount, confidenceScore);
    }
    
    @Override
    public String toString() {
        return "OCRResult{" +
                "extractedText='" + (extractedText.length() > 50 ? 
                        extractedText.substring(0, 50) + "..." : extractedText) + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", timestamp=" + getFormattedTimestamp() +
                ", sourceImagePath='" + sourceImagePath + '\'' +
                ", characterCount=" + characterCount +
                ", wordCount=" + wordCount +
                '}';
    }
}