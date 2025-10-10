package com.ocrapp.service;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Service class for text processing and cleanup.
 * Handles text formatting, cleaning, and common OCR error corrections.
 */
public class TextProcessor {
    
    // Common OCR error patterns, need to add more later
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \t]+");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\n{3,}");
    private static final Pattern TRAILING_SPACES = Pattern.compile("[ \t]+$", Pattern.MULTILINE);
    private static final Pattern LEADING_SPACES = Pattern.compile("^[ \t]+", Pattern.MULTILINE);
    

    public TextProcessor() {
        // Default constructor, modify later if needed
    }
    
    /**
     * Clean and format extracted text
     * Applies all cleaning operations
     * @param rawText Raw text from OCR
     * @return Cleaned and formatted text
     */
    public String cleanText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }
        
        System.out.println("Starting text cleanup...");
        
        String cleanedText = rawText;
        
        // Apply cleaning operations
        cleanedText = removeExtraWhitespace(cleanedText);
        cleanedText = fixCommonOCRErrors(cleanedText);
        cleanedText = normalizeLineBreaks(cleanedText);
        cleanedText = trimLines(cleanedText);
        cleanedText = removeExcessiveNewlines(cleanedText);
        
        System.out.println("Text cleanup completed");
        
        return cleanedText.trim();
    }
    
    /**
     * Remove extra whitespace (multiple spaces, tabs)
     * @param text Text to process
     * @return Text with normalized whitespace
     */
    public String removeExtraWhitespace(String text) {
        if (text == null) {
            return "";
        }
        
        // Replace multiple spaces with single space
        Matcher matcher = MULTIPLE_SPACES.matcher(text);
        return matcher.replaceAll(" ");
    }
    
    /**
     * Fix common OCR recognition errors
     * @param text Text to process
     * @return Text with corrections applied
     */
    public String fixCommonOCRErrors(String text) {
        if (text == null) {
            return "";
        }
        
        String correctedText = text;
        
        // Common OCR mistakes
        // just some basic stuff - should be expanded based on testing
        
        // correctedText = correctedText.replace("0", "O"); // In some contexts, 0 might be O
        // correctedText = correctedText.replace("|", "I"); // Pipe often mistaken for I
        // correctedText = correctedText.replace("5", "S"); // In some words
        
        // Note: later might use:
        // - Dictionary lookups
        // - Context-aware corrections
        // - Machine learning models
        
        // For now, spacing issues
        correctedText = correctedText.replace(" ,", ",");
        correctedText = correctedText.replace(" .", ".");
        correctedText = correctedText.replace(" !", "!");
        correctedText = correctedText.replace(" ?", "?");
        correctedText = correctedText.replace(" :", ":");
        correctedText = correctedText.replace(" ;", ";");
        
        return correctedText;
    }
    
    /**
     * Normalize line breaks (convert different line break styles to \n)
     * @param text Text to process
     * @return Text with normalized line breaks
     */
    public String normalizeLineBreaks(String text) {
        if (text == null) {
            return "";
        }
        
        // Convert Windows-style (\r\n) and Mac-style (\r) to Unix-style (\n)
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }
    
    /**
     * Remove excessive newlines (more than 2 consecutive)
     * @param text Text to process
     * @return Text with normalized newlines
     */
    public String removeExcessiveNewlines(String text) {
        if (text == null) {
            return "";
        }
        
        // Replace 3 or more newlines with just 2
        Matcher matcher = MULTIPLE_NEWLINES.matcher(text);
        return matcher.replaceAll("\n\n");
    }
    
    /**
     * Trim trailing and leading spaces from each line
     * @param text Text to process
     * @return Text with trimmed lines
     */
    public String trimLines(String text) {
        if (text == null) {
            return "";
        }
        
        // Remove trailing spaces
        String result = TRAILING_SPACES.matcher(text).replaceAll("");
        
        // Remove leading spaces
        result = LEADING_SPACES.matcher(result).replaceAll("");
        
        return result;
    }
    
    /**
     * Format text for display (ensure consistent spacing and formatting)
     * @param text Text to format
     * @return Formatted text
     */
    public String formatText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String formattedText = text;
        
        // Ensure proper spacing after punctuation
        formattedText = formattedText.replaceAll("([.!?])([A-Z])", "$1 $2");
        
        // Ensure no space before punctuation
        formattedText = formattedText.replaceAll("\\s+([.,!?;:])", "$1");
        
        return formattedText;
    }
    
    /**
     * Validate text quality
     * Checks if text appears to be valid/readable
     * @param text Text to validate
     * @return true if text appears valid, false otherwise
     */
    public boolean validateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // Check minimum length
        if (text.length() < 3) {
            return false;
        }
        
        // Count alphanumeric characters
        long alphanumericCount = text.chars()
                .filter(Character::isLetterOrDigit)
                .count();
        
        // Calculate ratio of valid characters
        double validRatio = (double) alphanumericCount / text.length();
        
        // Text should be at least 30% alphanumeric to be considered valid
        return validRatio >= 0.3;
    }
    
    /**
     * Get text statistics
     * @param text Text to analyze
     * @return Statistics string
     */
    public String getTextStatistics(String text) {
        if (text == null) {
            return "No text to analyze";
        }
        
        int totalChars = text.length();
        int totalWords = countWords(text);
        int totalLines = countLines(text);
        int alphanumericChars = (int) text.chars().filter(Character::isLetterOrDigit).count();
        int whitespaceChars = (int) text.chars().filter(Character::isWhitespace).count();
        
        StringBuilder stats = new StringBuilder();
        stats.append("Text Statistics:\n");
        stats.append("================\n");
        stats.append("Total Characters: ").append(totalChars).append("\n");
        stats.append("Alphanumeric: ").append(alphanumericChars).append("\n");
        stats.append("Whitespace: ").append(whitespaceChars).append("\n");
        stats.append("Total Words: ").append(totalWords).append("\n");
        stats.append("Total Lines: ").append(totalLines).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Count words in text
     * @param text Text to analyze
     * @return Word count
     */
    public int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * Count lines in text
     * @param text Text to analyze
     * @return Line count
     */
    public int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        String[] lines = text.split("\n");
        return lines.length;
    }
    
    /**
     * Extract preview text (first N characters)
     * @param text Text to extract from
     * @param maxLength Maximum length of preview
     * @return Preview text
     */
    public String getPreview(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength) + "...";
    }
    
    /**
     * Convert text to uppercase
     * @param text Text to convert
     * @return Uppercase text
     */
    public String toUpperCase(String text) {
        if (text == null) {
            return "";
        }
        return text.toUpperCase();
    }
    
    /**
     * Convert text to lowercase
     * @param text Text to convert
     * @return Lowercase text
     */
    public String toLowerCase(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase();
    }
    
    /**
     * Convert text to title case (capitalize first letter of each word)
     * @param text Text to convert
     * @return Title case text
     */
    public String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String[] words = text.split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    titleCase.append(word.substring(1).toLowerCase());
                }
                titleCase.append(" ");
            }
        }
        
        return titleCase.toString().trim();
    }
    
    /**
     * Remove all punctuation from text
     * @param text Text to process
     * @return Text without punctuation
     */
    public String removePunctuation(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replaceAll("[^a-zA-Z0-9\\s]", "");
    }
    
    /**
     * Check if text contains only printable characters
     * @param text Text to check
     * @return true if all characters are printable, false otherwise
     */
    public boolean isPrintable(String text) {
        if (text == null) {
            return false;
        }
        
        return text.chars().allMatch(c -> c >= 32 && c <= 126 || c == '\n' || c == '\r' || c == '\t');
    }
}