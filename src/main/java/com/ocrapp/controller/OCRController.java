package com.ocrapp.controller;

import com.ocrapp.model.OCRResult;
import com.ocrapp.service.ImageProcessor;
import com.ocrapp.service.OCREngine;
import com.ocrapp.service.TextProcessor;
import com.ocrapp.util.FileManager;
import com.ocrapp.view.OCRView;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Controller class that coordinates between the view and services.
 * Handles user interactions and application logic.
 */
public class OCRController {
    
    private OCRView view;
    private FileManager fileManager;
    private ImageProcessor imageProcessor;
    private OCREngine ocrEngine;
    private TextProcessor textProcessor;
    
    private File currentImageFile;
    private BufferedImage currentImage;
    private OCRResult currentResult;
    
    /**
     * Constructor - initializes all components
     * @param The GUI view
     */
    public OCRController(OCRView view) {
        this.view = view;
        
        // services
        this.fileManager = new FileManager();
        this.imageProcessor = new ImageProcessor();
        this.ocrEngine = new OCREngine();
        this.textProcessor = new TextProcessor();
        
        // state
        this.currentImageFile = null;
        this.currentImage = null;
        this.currentResult = null;
        
        initializeListeners();
        
        // Check OCR engine initialization
        if (!ocrEngine.isInitialized()) {
            view.showError("Failed to initialize OCR Engine.\n" +
                          "Please ensure Tesseract is installed correctly.");
        } else {
            view.setStatus("Ready - OCR Engine initialized successfully");
        }
    }
    
    /**
     * Initialize all event listeners for GUI components
     */
    private void initializeListeners() {
        view.getLoadImageButton().addActionListener(e -> handleLoadImage());
        view.getExtractTextButton().addActionListener(e -> handleExtractText());
        view.getSaveTextButton().addActionListener(e -> handleSaveText());
        view.getClearButton().addActionListener(e -> handleClear());
        
        view.getLanguageComboBox().addActionListener(e -> handleLanguageChange());
        
        view.getOpenMenuItem().addActionListener(e -> handleLoadImage());
        view.getSaveMenuItem().addActionListener(e -> handleSaveText());
        view.getExitMenuItem().addActionListener(e -> handleExit());
        view.getClearMenuItem().addActionListener(e -> handleClear());
        view.getAboutMenuItem().addActionListener(e -> view.showAboutDialog());
    }
    
    private void handleLoadImage() {
        view.setStatus("Selecting image file...");
        
        // Open file dialog
        File selectedFile = fileManager.selectImageFile();
        
        if (selectedFile == null) {
            view.setStatus("Image selection cancelled");
            return;
        }
        
        // Validate image file
        if (!fileManager.isValidImageFile(selectedFile)) {
            view.showError("Invalid image file selected.\n" +
                          "Please select a valid image file (JPG, PNG, BMP, TIFF, GIF).");
            view.setStatus("Invalid image file");
            return;
        }
        
        view.setStatus("Loading image...");
        
        // Load image
        BufferedImage image = imageProcessor.loadImage(selectedFile);
        
        if (image == null) {
            view.showError("Failed to load image.\n" +
                          "The file may be corrupted or in an unsupported format.");
            view.setStatus("Failed to load image");
            return;
        }
        
        // Store current image
        this.currentImageFile = selectedFile;
        this.currentImage = image;
        
        view.displayImage(image);
        
        // Update image info
        String imageInfo = String.format("Image: %s (%dx%d) - %s",
                selectedFile.getName(),
                image.getWidth(),
                image.getHeight(),
                fileManager.getFormattedFileSize(selectedFile));
        view.setImageInfo(imageInfo);
        
        view.setExtractButtonEnabled(true);
        
        // Clear previous text
        view.displayText("");
        view.setTextInfo("Text: 0 characters, 0 words");
        view.setSaveButtonEnabled(false);
        
        view.setStatus("Image loaded successfully - Ready for OCR");
        
        System.out.println("Image loaded: " + selectedFile.getAbsolutePath());
    }
    
    /**
     * Handle Extract Text button click
     */
    private void handleExtractText() {
        if (currentImage == null || currentImageFile == null) {
            view.showError("No image loaded.\nPlease load an image first.");
            return;
        }
        
        // Disable buttons during processing
        view.setExtractButtonEnabled(false);
        view.getLoadImageButton().setEnabled(false);
        
        // Show progress bar
        view.showProgress("Initializing...");
        view.setStatus("Starting OCR process...");
        
        // Process in a separate thread to keep UI responsive
        SwingWorker<OCRResult, Void> worker = new SwingWorker<OCRResult, Void>() {
            @Override
            protected OCRResult doInBackground() throws Exception {
                // Step 1: Preprocessing (0-30%)
                updateProgressSmooth(0, 15, "Analyzing image...");
                
                updateProgressSmooth(15, 30, "Preprocessing image...");
                BufferedImage processedImage = imageProcessor.preprocessImage(currentImage);
                
                if (processedImage == null) {
                    throw new Exception("Image preprocessing failed");
                }
                
                SwingUtilities.invokeLater(() -> 
                    view.updateProgress(30, "Preprocessing complete"));
                
                // Step 2: OCR Processing (30-80%)
                updateProgressSmooth(30, 40, "Initializing OCR engine...");
                
                updateProgressSmooth(40, 50, "Extracting text...");
                
                // Perform OCR (this is the longest operation)
                OCRResult result = ocrEngine.extractText(currentImageFile, processedImage);
                
                if (result == null) {
                    throw new Exception("OCR extraction failed");
                }
                
                // Simulate progress during OCR
                updateProgressSmooth(50, 80, "Processing text...");
                
                SwingUtilities.invokeLater(() -> 
                    view.updateProgress(80, "Text extracted"));
                
                // Step 3: Text Processing (80-100%)
                updateProgressSmooth(80, 90, "Cleaning text...");
                
                updateProgressSmooth(90, 100, "Formatting text...");
                
                SwingUtilities.invokeLater(() -> 
                    view.updateProgress(100, "Complete!"));
                
                return result;
            }
            
            /**
             * Smoothly update progress from start to end value
             * @param start Starting percentage
             * @param end Ending percentage
             * @param message Progress message
             */
            private void updateProgressSmooth(int start, int end, String message) throws InterruptedException {
                int steps = (end - start);
                int delay = Math.max(20, 150 / steps);
                
                for (int i = start; i <= end; i++) {
                    final int progress = i;
                    SwingUtilities.invokeLater(() -> 
                        view.updateProgress(progress, message));
                    Thread.sleep(delay);
                }
            }
            
            @Override
            protected void done() {
                try {
                    // Get result
                    OCRResult result = get();
                    currentResult = result;
                    
                    // Clean and format text
                    String cleanedText = textProcessor.cleanText(result.getExtractedText());
                    result.setExtractedText(cleanedText);
                    
                    // Display text
                    view.displayText(cleanedText);
                    
                    // Update text info
                    String textInfo = String.format("Text: %d characters, %d words - Confidence: %.1f%%",
                            result.getCharacterCount(),
                            result.getWordCount(),
                            result.getConfidenceScore());
                    view.setTextInfo(textInfo);
                    
                    // Enable save button if text was extracted
                    if (result.hasText()) {
                        view.setSaveButtonEnabled(true);
                        view.setStatus("Text extraction completed successfully");
                        view.showSuccess("OCR completed!\n" +
                                       result.getWordCount() + " words extracted.");
                    } else {
                        view.setStatus("No text found in image");
                        view.showInfo("No text was detected in the image.\n" +
                                    "The image may not contain readable text.");
                    }
                    
                    System.out.println("OCR completed: " + result.getSummary());
                    
                } catch (Exception e) {
                    view.showError("OCR extraction failed:\n" + e.getMessage());
                    view.setStatus("OCR extraction failed");
                    System.err.println("OCR error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Hide progress bar and re-enable buttons
                    view.hideProgress();
                    view.setExtractButtonEnabled(true);
                    view.getLoadImageButton().setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleSaveText() {
        String text = view.getText();
        
        if (text == null || text.trim().isEmpty()) {
            view.showError("No text to save.\nPlease extract text first.");
            return;
        }
        
        view.setStatus("Selecting save location...");
        
        String defaultFileName = fileManager.generateDefaultSaveFileName(currentImageFile);
        
        // Open save dialog
        File saveFile = fileManager.selectSaveLocation(defaultFileName);
        
        if (saveFile == null) {
            view.setStatus("Save cancelled");
            return;
        }
        
        view.setStatus("Saving text...");
        
        boolean success = fileManager.saveTextToFile(text, saveFile);
        
        if (success) {
            view.setStatus("Text saved successfully to: " + saveFile.getName());
            view.showSuccess("Text saved successfully!\n" +
                           "File: " + saveFile.getAbsolutePath());
            System.out.println("Text saved to: " + saveFile.getAbsolutePath());
        } else {
            view.showError("Failed to save text file.\n" +
                         "Please check file permissions and try again.");
            view.setStatus("Failed to save text");
        }
    }
    
    private void handleClear() {
        // Confirm with user
        int choice = JOptionPane.showConfirmDialog(
                view,
                "Clear all content (image and text)?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Clear state
            currentImageFile = null;
            currentImage = null;
            currentResult = null;
            
            imageProcessor.clearCurrentImage();
            
            view.clearAll();
            
            System.out.println("Application cleared");
        }
    }
    
    private void handleExit() {
        // Confirm with user
        int choice = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            System.out.println("Application exiting...");
            System.exit(0);
        }
    }
    
    /**
     * Handle language selection change
     */
    private void handleLanguageChange() {
        String selectedLanguage = (String) view.getLanguageComboBox().getSelectedItem();
        String languageCode = OCREngine.mapLanguageToCode(selectedLanguage);
        
        boolean success = ocrEngine.setLanguage(languageCode);
        
        if (success) {
            view.setStatus("Language changed to: " + selectedLanguage);
            System.out.println("OCR language changed to: " + selectedLanguage + " (" + languageCode + ")");
        } else {
            view.showError("Failed to change language to " + selectedLanguage + "\n" +
                          "Language data may not be installed.\n" +
                          "Download from: https://github.com/tesseract-ocr/tessdata");
            view.setStatus("Language change failed");
            
            // Reset to English
            view.getLanguageComboBox().setSelectedIndex(0);
        }
    }
    
    public String getOCREngineInfo() {
        return ocrEngine.getEngineInfo();
    }

    public OCRResult getCurrentResult() {
        return currentResult;
    }

    public boolean hasImageLoaded() {
        return currentImage != null;
    }
    
    public boolean hasTextExtracted() {
        return currentResult != null && currentResult.hasText();
    }
}