package com.ocrapp.controller;

import com.ocrapp.model.OCRResult;
import com.ocrapp.service.ImageProcessor;
import com.ocrapp.service.OCREngine;
import com.ocrapp.service.TextProcessor;
import com.ocrapp.util.FileManager;
import com.ocrapp.view.OCRView;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
    private BufferedImage selectedRegionImage;
    private boolean isRegionSelected;
    
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
        this.selectedRegionImage = null;
        this.isRegionSelected = false;
        
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
        view.getSelectRegionButton().addActionListener(e -> handleSelectRegion());
        view.getClearSelectionButton().addActionListener(e -> handleClearSelection());
        view.getExtractTextButton().addActionListener(e -> handleExtractText());
        view.getSaveTextButton().addActionListener(e -> handleSaveText());
        view.getCopyButton().addActionListener(e -> handleCopyToClipboard());
        view.getClearButton().addActionListener(e -> handleClear());
        
        view.getLanguageComboBox().addActionListener(e -> handleLanguageChange());
        
        view.getOpenMenuItem().addActionListener(e -> handleLoadImage());
        view.getSaveMenuItem().addActionListener(e -> handleSaveText());
        view.getCopyMenuItem().addActionListener(e -> handleCopyToClipboard());
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
        view.setSelectRegionButtonEnabled(true);
        view.setStatus("Image loaded - Draw a selection box on the image or click 'Extract Text' for full image");
        
        // Reset any previous region selection
        selectedRegionImage = null;
        isRegionSelected = false;
        
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
    	    
    	    // Determine what to process: selected region or full image
    	    final BufferedImage imageToProcess;
    	    final String statusMessage;
    	    BufferedImage sourceImage = (isRegionSelected && selectedRegionImage != null)
    	    	    ? selectedRegionImage
    	    	    : currentImage;
    	    
    	    BufferedImage processedImage = imageProcessor.preprocessImage(sourceImage);
    	    
    	    if (isRegionSelected && selectedRegionImage != null) {
    	        // User selected a region - process only that area
    	        imageToProcess = selectedRegionImage;
    	        statusMessage = "Processing selected region...";
    	        System.out.println("OCR processing selected region: " + 
    	                          imageToProcess.getWidth() + "x" + imageToProcess.getHeight());
    	    } else {
    	        // No region selected - process entire image
    	        imageToProcess = currentImage;
    	        statusMessage = "Processing entire image...";
    	        System.out.println("OCR processing entire image");
    	    }
    	    
    	    // Disable buttons during processing
    	    view.setExtractButtonEnabled(false);
    	    view.getLoadImageButton().setEnabled(false);
    	    view.setSelectRegionButtonEnabled(false);
    	    
    	    // Show progress bar
    	    view.showProgress("Initializing...");
    	    view.setStatus(statusMessage);
        
        // Process in a separate thread to keep UI responsive
        SwingWorker<OCRResult, Void> worker = new SwingWorker<OCRResult, Void>() {
            @Override
            protected OCRResult doInBackground() throws Exception {
                // Step 1: Preprocessing (0-30%)
                updateProgressSmooth(0, 15, "Analyzing image...");
                
                updateProgressSmooth(15, 30, "Preprocessing image...");              
                
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
                        view.setCopyButtonEnabled(true);
                        view.getCopyMenuItem().setEnabled(true);
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
                    view.setSelectRegionButtonEnabled(true);
                    
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
    
    /**
     * Handle Select Region button click
     */
    private void handleSelectRegion() {
        if (currentImage == null) {
            view.showError("No image loaded.\nPlease load an image first.");
            return;
        }
        
        // Check if user has made a selection
        if (view.hasSelection()) {
            // User has selected - confirm and store it
            BufferedImage selected = view.getSelectedRegion();
            view.setClearSelectionButtonEnabled(true);
            
            if (selected != null) {
                this.selectedRegionImage = selected;
                this.isRegionSelected = true;
                
                // Update display
                String imageInfo = String.format("Image: %s (%dx%d) | Selected: %dx%d - Ready to extract",
                        currentImageFile.getName(),
                        currentImage.getWidth(),
                        currentImage.getHeight(),
                        selected.getWidth(),
                        selected.getHeight());
                view.setImageInfo(imageInfo);
                
                view.setStatus("Region confirmed! Click 'Extract Text' to OCR this area");
                
                System.out.println("Region confirmed: " + selected.getWidth() + "x" + selected.getHeight());
                
                // Show confirmation
                int result = JOptionPane.showConfirmDialog(
                        view,
                        "Region selected: " + selected.getWidth() + "×" + selected.getHeight() + " pixels\n\n" +
                        "Click 'Yes' to extract text from this region now,\n" +
                        "or 'No' to adjust your selection.",
                        "Confirm Selection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    // User confirmed - trigger extraction immediately
                    handleExtractText();
                }
            } else {
                view.showError("Failed to extract selected region.\nPlease try selecting again.");
                view.clearSelection();
            }
        } else {
            // No selection yet - instruct user to make one
            view.showInfo(
                "How to Select a Region:\n\n" +
                "1. Click and DRAG on the image to draw a selection box\n" +
                "2. Release mouse when you've selected the desired area\n" +
                "3. Click 'Select Region' button again to confirm\n\n" +
                "The selection appears as a blue rectangle.\n" +
                "Perfect for selecting individual text sections!"
            );
            view.setStatus("Draw a selection on the image by clicking and dragging...");
        }
    }
    
    /**
     * Handle Copy to Clipboard button click
     */
    private void handleCopyToClipboard() {
        String text = view.getText();
        
        if (text == null || text.trim().isEmpty()) {
            view.showError("No text to copy.\nPlease extract text first.");
            return;
        }
        
        try {
            // Copy to system clipboard
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            view.setStatus("Text copied to clipboard (" + text.length() + " characters)");
            view.showInfo("Text copied to clipboard!\n" +
                         "Characters: " + text.length() + "\n" +
                         "Words: " + textProcessor.countWords(text));
            
            System.out.println("Text copied to clipboard: " + text.length() + " characters");
            
        } catch (Exception e) {
            view.showError("Failed to copy text to clipboard:\n" + e.getMessage());
            view.setStatus("Clipboard copy failed");
            System.err.println("Clipboard error: " + e.getMessage());
            e.printStackTrace();
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
            selectedRegionImage = null;
            isRegionSelected = false;
            
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
    
    /**
     * Handle Clear Selection button click
     */
    private void handleClearSelection() {
        view.clearSelection();
        selectedRegionImage = null;
        isRegionSelected = false;
        
        view.setImageInfo("Image: " + currentImageFile.getName() + 
                          " (" + currentImage.getWidth() + "x" + currentImage.getHeight() + ")");
        view.setStatus("Selection cleared - Ready to select again or extract from full image");
        view.setClearSelectionButtonEnabled(false);
        
        System.out.println("Selection cleared");
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