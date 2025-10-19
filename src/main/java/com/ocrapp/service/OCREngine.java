package com.ocrapp.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import com.ocrapp.model.OCRResult;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Service class for OCR operations using Tesseract.
 * Handles text extraction from images.
 */
public class OCREngine {
    
    private ITesseract tesseract;
    private String currentLanguage;
    private boolean isInitialized;
    
    // Default settings
    private static final String DEFAULT_LANGUAGE = "eng";
    private static final String DEFAULT_DATA_PATH = "C:\\Program Files\\Tesseract-OCR\\tessdata";
    
    /**
     * Default constructor - initializes with English language
     */
    public OCREngine() {
        this(DEFAULT_LANGUAGE);
    }
    
    /**
     * Constructor with language specification
     * @param language Language code (e.g., "eng" for English)
     */
    public OCREngine(String language) {
        this.currentLanguage = language;
        this.isInitialized = false;
        initializeEngine();
    }
    
    /**
     * Initialize the Tesseract OCR engine
     */
    private void initializeEngine() {
        try {
            System.out.println("Initializing Tesseract OCR engine...");
            
            tesseract = new Tesseract();
            
            tesseract.setDatapath(DEFAULT_DATA_PATH);
            tesseract.setLanguage(currentLanguage);
            
            // Default OEM & PSM (3)
            tesseract.setOcrEngineMode(3);
            tesseract.setPageSegMode(3);
            
            this.isInitialized = true;
            System.out.println("Tesseract OCR engine initialized successfully");
            System.out.println("Language: " + currentLanguage);
            System.out.println("Data path: " + DEFAULT_DATA_PATH);
            
        } catch (Exception e) {
            System.err.println("Failed to initialize Tesseract OCR engine: " + e.getMessage());
            e.printStackTrace();
            this.isInitialized = false;
        }
    }
    
    /**
     * Extract text from an image file
     * @param imageFile Image file to process
     * @return OCRResult object containing extracted text and metadata
     */
    public OCRResult extractText(File imageFile) {
        if (!isInitialized) {
            System.err.println("OCR Engine is not initialized");
            return new OCRResult("", 0.0f, "");
        }
        
        if (imageFile == null || !imageFile.exists()) {
            System.err.println("Invalid image file");
            return new OCRResult("", 0.0f, "");
        }
        
        try {
            System.out.println("Starting OCR on: " + imageFile.getName());
            
            long startTime = System.currentTimeMillis();
            
            // Perform OCR
            String extractedText = tesseract.doOCR(imageFile);
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            System.out.println("OCR completed in " + processingTime + "ms");
            System.out.println("Extracted " + extractedText.length() + " characters");
            
            // Create OCR result
            OCRResult result = new OCRResult(
                    extractedText != null ? extractedText : "",
                    calculateConfidence(extractedText),
                    imageFile.getAbsolutePath()
            );
            
            return result;
            
        } catch (TesseractException e) {
            System.err.println("OCR failed: " + e.getMessage());
            e.printStackTrace();
            return new OCRResult("OCR Error: " + e.getMessage(), 0.0f, imageFile.getAbsolutePath());
        }
    }
    
    /**
     * Extract text from a BufferedImage
     * @param image BufferedImage to process
     * @return OCRResult object containing extracted text and metadata
     */
    public OCRResult extractText(BufferedImage image) {
        if (!isInitialized) {
            System.err.println("OCR Engine is not initialized");
            return new OCRResult("", 0.0f, "");
        }
        
        if (image == null) {
            System.err.println("Invalid image");
            return new OCRResult("", 0.0f, "");
        }
        
        try {
            System.out.println("Starting OCR on BufferedImage...");
            
            long startTime = System.currentTimeMillis();
            
            // Perform OCR
            String extractedText = tesseract.doOCR(image);
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            System.out.println("OCR completed in " + processingTime + "ms");
            System.out.println("Extracted " + extractedText.length() + " characters");
            
            // Create OCR result
            OCRResult result = new OCRResult(
                    extractedText != null ? extractedText : "",
                    calculateConfidence(extractedText),
                    "BufferedImage"
            );
            
            return result;
            
        } catch (TesseractException e) {
            System.err.println("OCR failed: " + e.getMessage());
            e.printStackTrace();
            return new OCRResult("OCR Error: " + e.getMessage(), 0.0f, "BufferedImage");
        }
    }
    
    /**
     * Extract text from image file and preprocessed image
     * @param imageFile Original image file (for metadata)
     * @param processedImage Preprocessed BufferedImage
     * @return OCRResult object
     */
    public OCRResult extractText(File imageFile, BufferedImage processedImage) {
        if (!isInitialized) {
            System.err.println("OCR Engine is not initialized");
            return new OCRResult("", 0.0f, "");
        }
        
        if (processedImage == null) {
            System.err.println("Invalid processed image");
            return new OCRResult("", 0.0f, "");
        }
        
        try {
            System.out.println("Starting OCR on preprocessed image...");
            
            long startTime = System.currentTimeMillis();
            
            // Perform OCR on preprocessed image
            
            applyLanguageSpecificSettings();
            String extractedText = tesseract.doOCR(processedImage);
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            System.out.println("OCR completed in " + processingTime + "ms");
            System.out.println("Extracted " + extractedText.length() + " characters");
            
            // Create OCR result
            String sourcePath = (imageFile != null) ? imageFile.getAbsolutePath() : "Preprocessed Image";
            OCRResult result = new OCRResult(
                    extractedText != null ? extractedText : "",
                    calculateConfidence(extractedText),
                    sourcePath
            );
            
            return result;
            
        } catch (TesseractException e) {
            System.err.println("OCR failed: " + e.getMessage());
            e.printStackTrace();
            String sourcePath = (imageFile != null) ? imageFile.getAbsolutePath() : "Preprocessed Image";
            return new OCRResult("OCR Error: " + e.getMessage(), 0.0f, sourcePath);
        }
    }
    
    /**
     * Calculate confidence score based on text characteristics
     * Note: Tesseract 5.x doesn't always provide confidence scores easily,
     * so we use heuristics based on the extracted text
     * @param text Extracted text
     * @return Confidence score (0-100)
     */
    private float calculateConfidence(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0f;
        }
        
        float confidence = 50.0f; // Base confidence
        
        // Increase confidence if text has normal characteristics
        if (text.length() > 10) {
            confidence += 10.0f;
        }
        
        // Check for valid character ratio
        long validChars = text.chars().filter(c -> Character.isLetterOrDigit(c) || Character.isWhitespace(c)).count();
        float validRatio = (float) validChars / text.length();
        
        if (validRatio > 0.8) {
            confidence += 20.0f;
        } else if (validRatio > 0.6) {
            confidence += 10.0f;
        }
        
        // Check for reasonable word structure
        String[] words = text.trim().split("\\s+");
        if (words.length > 5) {
            confidence += 10.0f;
        }
        
        // Cap at 100, currently 90 is max you can get
        return Math.min(confidence, 100.0f);
    }
    
    /**
     * Set the OCR language
     * @param language Language code (e.g., "eng", "fra", "spa")
     * @return true if successful, false otherwise
     */
    public boolean setLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return false;
        }
        
        try {
            tesseract.setLanguage(language);
            this.currentLanguage = language;
            System.out.println("Language changed to: " + language);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to set language: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map language display names to Tesseract language codes
     * @param languageName Display name ("English")
     * @return Tesseract language code ("eng")
     */
    public static String mapLanguageToCode(String languageName) {
        switch (languageName) {
            case "English":
                return "eng";
            case "Spanish":
                return "spa";
            case "French":
                return "fra";
            case "German":
                return "deu";
            case "Italian":
                return "ita";
            case "Portuguese":
                return "por";
            case "Arabic":
                return "ara";
            case "Chinese (Simplified)":
                return "chi_sim";
            case "Japanese":
                return "jpn";
            case "Korean":
                return "kor";
            case "Russian":
                return "rus";
            default:
                return "eng"; // Default language
        }
    }
    
    /**
     * Get current language setting
     * @return Current language code
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Check if OCR engine is initialized
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Re-initialize the OCR engine
     */
    public void reinitialize() {
        System.out.println("Re-initializing OCR engine...");
        initializeEngine();
    }
    
    /**
     * Set custom Tesseract data path
     * @param dataPath Path to tessdata folder
     */
    public void setDataPath(String dataPath) {
        if (dataPath != null && !dataPath.isEmpty()) {
            tesseract.setDatapath(dataPath);
            System.out.println("Data path set to: " + dataPath);
        }
    }
    
    /**
     * Get information about the OCR engine
     * @return Information string
     */
    public String getEngineInfo() {
        StringBuilder info = new StringBuilder();
        info.append("OCR Engine Status\n");
        info.append("=================\n");
        info.append("Initialized: ").append(isInitialized).append("\n");
        info.append("Language: ").append(currentLanguage).append("\n");
        info.append("Data Path: ").append(DEFAULT_DATA_PATH).append("\n");
        return info.toString();
    }
    
    /**
     * Apply OCR settings based on current language
     * Different languages need different page segmentation and engine modes
     */
    private void applyLanguageSpecificSettings() {
        String currentLang = getCurrentLanguage();
        
        if (isVerticalTextLanguage(currentLang)) {
            // Settings for vertical text languages (Japanese, Chinese)
            tesseract.setPageSegMode(5);  // Single uniform block of vertically aligned text
            tesseract.setOcrEngineMode(1); // LSTM engine (better for Asian languages)
            System.out.println("Applied vertical text settings for language: " + currentLang);
        } else if (isComplexScriptLanguage(currentLang)) {
            // Settings for Arabic, Hebrew (RTL languages)
            tesseract.setPageSegMode(6);  // Uniform block of text
            tesseract.setOcrEngineMode(1); // LSTM engine
            System.out.println("Applied complex script settings for language: " + currentLang);
        } else {
            // Settings for horizontal languages (English, Spanish, French, etc.)
            tesseract.setPageSegMode(3);  // Fully automatic page segmentation
            tesseract.setOcrEngineMode(3); // Default (Tesseract + LSTM)
            System.out.println("Applied standard horizontal text settings for language: " + currentLang);
        }
    }

    /**
     * Check if language uses vertical text
     * @param langCode Language code (e.g., "jpn", "chi_sim")
     * @return true if language commonly uses vertical text
     */
    private boolean isVerticalTextLanguage(String langCode) {
        return langCode.equals("jpn") ||      // Japanese
               langCode.equals("chi_sim") ||  // Chinese Simplified
               langCode.equals("chi_tra");  // Chinese Traditional
    }

    /**
     * Check if language uses complex scripts (RTL, etc.)
     * @param langCode Language code
     * @return true if language uses complex script
     */
    private boolean isComplexScriptLanguage(String langCode) {
        return langCode.equals("ara") ||      // Arabic
               langCode.equals("heb") ||      // Hebrew
               langCode.equals("fas") ||      // Persian
               langCode.equals("urd");        // Urdu
    }
}