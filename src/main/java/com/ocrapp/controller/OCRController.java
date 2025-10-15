package com.ocrapp.controller;

import com.ocrapp.model.OCRResult;
import com.ocrapp.service.ImageProcessor;
import com.ocrapp.service.OCREngine;
import com.ocrapp.service.TextProcessor;
import com.ocrapp.util.FileManager;
import com.ocrapp.view.OCRView;
import com.ocrapp.view.ImageDropTarget;
import com.ocrapp.util.AppPreferences;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.awt.dnd.DropTarget;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

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
    
    // Track extraction count for appending
    private int extractionCount;
    
    /**
     * Constructor - initializes all components
     * @param view The GUI view
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
        this.extractionCount = 0;
        
        initializeListeners();
        
        // Check OCR engine initialization
        if (!ocrEngine.isInitialized()) {
            view.showError("Failed to initialize OCR Engine.\n" +
                          "Please ensure Tesseract is installed correctly.");
        } else {
        	validateAndSetSavedLanguage();
            view.setStatus("Ready - OCR Engine initialized successfully");
        }
    }
    
    /**
     * Initialize all event listeners for GUI components
     */
    private void initializeListeners() {
        view.getLoadImageButton().addActionListener(e -> handleLoadImage());
        view.getExtractTextButton().addActionListener(e -> handleExtractText(currentImage, false));
        view.getSaveTextButton().addActionListener(e -> handleSaveText());
        view.getClearButton().addActionListener(e -> handleClear());
        
        view.getLanguageComboBox().addActionListener(e -> handleLanguageChange());
        
        view.getOpenMenuItem().addActionListener(e -> handleLoadImage());
        view.getSaveMenuItem().addActionListener(e -> handleSaveText());
        view.getCopyMenuItem().addActionListener(e -> handleCopyToClipboard());
        view.getExitMenuItem().addActionListener(e -> handleExit());
        view.getClearMenuItem().addActionListener(e -> handleClear());
        view.getAboutMenuItem().addActionListener(e -> view.showAboutDialog());
        view.getUndoMenuItem().addActionListener(e -> handleUndo());
        view.getRedoMenuItem().addActionListener(e -> handleRedo());
        view.getCutMenuItem().addActionListener(e -> view.getText());
        view.getPasteMenuItem().addActionListener(e -> handlePaste());
        view.getSelectAllMenuItem().addActionListener(e -> handleSelectAll());
        
        // drag & drop and paste from clipboard
        setupDragAndDrop();
        setupClipboardPaste();
        
        view.getImagePanel().setOnSelectionComplete(selectedRegion -> {
            handleExtractText(selectedRegion, true);
        });
    }
    
    private void handleLoadImage() {
        view.setStatus("Selecting image file...");
        
        // file dialog
        File selectedFile = fileManager.selectImageFile();
        
        if (selectedFile == null) {
            view.setStatus("Image selection cancelled");
            return;
        }
        
        if (!fileManager.isValidImageFile(selectedFile)) {
            view.showError("Invalid image file selected.\n" +
                          "Please select a valid image file (JPG, PNG, BMP, TIFF, GIF).");
            view.setStatus("Invalid image file");
            return;
        }
        
        view.setStatus("Loading image...");
        
        loadImageFromFile(selectedFile);
    }
    
    /**
     * Handle Extract Text - works for both full image and selected regions
     * @param imageToProcess The image to extract text from
     * @param appendText Whether to append text (true for regions) or replace (false for full image)
     */
    private void handleExtractText(BufferedImage imageToProcess, boolean appendText) {
        if (imageToProcess == null) {
            view.showError("No image to process.");
            return;
        }
        
        final String existingText = appendText ? view.getText() : "";
        final boolean shouldAppend = appendText && !existingText.trim().isEmpty(); 
        final String statusMessage = appendText ? 
            "Processing selected region..." : "Processing entire image...";
        
        System.out.println(appendText ? 
            "OCR processing selected region: " + imageToProcess.getWidth() + "x" + imageToProcess.getHeight() :
            "OCR processing entire image");
        
        // Disable buttons during processing
        view.setExtractButtonEnabled(false);
        view.getLoadImageButton().setEnabled(false);
        
        view.showProgress("Initializing...");
        view.setStatus(statusMessage);
        
        // Process in a separate thread to keep UI responsive
        SwingWorker<OCRResult, Void> worker = new SwingWorker<OCRResult, Void>() {
        	@Override
        	protected OCRResult doInBackground() throws Exception {
        	    // Step 1: Preprocessing (0-30%)
        	    SwingUtilities.invokeLater(() -> view.updateProgress(0, "Analyzing image..."));
        	    SwingUtilities.invokeLater(() -> view.updateProgress(15, "Preprocessing image..."));
        	    
        	    BufferedImage processedImage = imageProcessor.preprocessImage(imageToProcess);
        	    
        	    if (processedImage == null) {
        	        throw new Exception("Image preprocessing failed");
        	    }
        	    
        	    SwingUtilities.invokeLater(() -> view.updateProgress(30, "Preprocessing complete"));
        	    
        	    // Step 2: OCR Processing (30-80%)
        	    SwingUtilities.invokeLater(() -> view.updateProgress(40, "Initializing OCR engine..."));
        	    SwingUtilities.invokeLater(() -> view.updateProgress(50, "Extracting text..."));
        	    
        	    // Perform OCR (this is where the REAL work happens)
        	    OCRResult result = ocrEngine.extractText(currentImageFile, processedImage);
        	    
        	    if (result == null) {
        	        throw new Exception("OCR extraction failed");
        	    }
        	    
        	    SwingUtilities.invokeLater(() -> view.updateProgress(80, "Text extracted"));
        	    
        	    // Step 3: Text Processing (80-100%)
        	    SwingUtilities.invokeLater(() -> view.updateProgress(90, "Cleaning text..."));
        	    SwingUtilities.invokeLater(() -> view.updateProgress(100, "Complete!"));
        	    
        	    return result;
        	}
            
            
            @Override
            protected void done() {
                try {
                    OCRResult result = get();
                    
                    String cleanedText = textProcessor.cleanText(result.getExtractedText());
                    result.setExtractedText(cleanedText);
                    
                    String finalText;
                    if (shouldAppend && !cleanedText.trim().isEmpty()) {
                        finalText = existingText + "\n\n" + cleanedText;
                        extractionCount++;
                    } else if (!cleanedText.trim().isEmpty()) {
                        finalText = cleanedText;
                        extractionCount = 1;
                    } else {
                        finalText = existingText;
                        extractionCount = existingText.trim().isEmpty() ? 0 : extractionCount;
                    }
                    
                    // combine stats if appending
                    if (shouldAppend && currentResult != null) {
                        currentResult.setExtractedText(finalText);
                        // confidence is averaged, counts are summed
                    } else {
                        currentResult = result;
                        currentResult.setExtractedText(finalText);
                    }
                    
                    view.displayText(finalText);
                    
                    int totalChars = finalText.length();
                    int totalWords = textProcessor.countWords(finalText);
                    String textInfo = String.format("Text: %d characters, %d words - Extractions: %d",
                            totalChars, totalWords, extractionCount);
                    view.setTextInfo(textInfo);
                    
                    if (!finalText.trim().isEmpty()) {
                        view.setSaveButtonEnabled(true);
                        view.getCopyMenuItem().setEnabled(true);
                        
                        if (shouldAppend) {
                            view.setStatus("Region text added! Select another area or extract full image");
                            view.showSuccess("Text added!\n" +
                                           result.getWordCount() + " words extracted from region.\n" +
                                           "Total: " + totalWords + " words");
                        } else {
                            view.setStatus("Text extraction completed successfully");
                            view.showSuccess("OCR completed!\n" +
                                           result.getWordCount() + " words extracted.");
                        }
                    } else {
                        view.setStatus("No text found in " + (appendText ? "selected region" : "image"));
                        view.showInfo("No text was detected.\n" +
                                    "Try selecting a different area or checking the image quality.");
                    }
                    
                    System.out.println("OCR completed: " + result.getSummary());
                    
                } catch (Exception e) {
                    view.showError("OCR extraction failed:\n" + e.getMessage());
                    view.setStatus("OCR extraction failed");
                    System.err.println("OCR error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    view.hideProgress();
                    view.setExtractButtonEnabled(true);
                    view.getLoadImageButton().setEnabled(true);
                    
                    if (appendText) {
                        view.clearSelection();
                    }
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
            extractionCount = 0;
            
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
        	AppPreferences.saveLastLanguage(selectedLanguage);
        	 
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
     * Handle undo action
     */
    private void handleUndo() {
        try {
            if (view.getUndoManager().canUndo()) {
                view.getUndoManager().undo();
            }
        } catch (Exception e) {
            System.err.println("Cannot undo: " + e.getMessage());
        }
    }

    /**
     * Handle redo action
     */
    private void handleRedo() {
        try {
            if (view.getUndoManager().canRedo()) {
                view.getUndoManager().redo();
            }
        } catch (Exception e) {
            System.err.println("Cannot redo: " + e.getMessage());
        }
    }

    /**
     * Handle paste action
     */
    private void handlePaste() {
        // Access through view's text area
        view.getTextArea().paste();
    }

    /**
     * Handle select all action
     */
    private void handleSelectAll() {
        view.getTextArea().selectAll();
        view.getTextArea().requestFocus();
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
    
    /**
     * Setup drag and drop for image panel
     */
    private void setupDragAndDrop() {
        ImageDropTarget dropTarget = new ImageDropTarget(file -> {
            // Load dropped file
            SwingUtilities.invokeLater(() -> loadImageFromFile(file));
        });
        
        view.getImageCropPanel().setDropTarget(new DropTarget(view.getImageCropPanel(), dropTarget));
        System.out.println("Drag and drop enabled");
    }

    /**
     * Setup clipboard paste (Ctrl+V to paste image)
     */
    private void setupClipboardPaste() {
        JRootPane root = view.getRootPane();
        JComponent imagePanel = view.getImagePanel();

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control V"), "pasteImage");
        root.getActionMap().put("pasteImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasteImage();
            }
        });

        // For image panel (if focused)
        imagePanel.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke("control V"), "pasteImage");
        imagePanel.getActionMap().put("pasteImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasteImage();
            }
        });

        System.out.println("Clipboard paste enabled (Ctrl+V for both window and image area)");
    }


    /**
     * Handle paste image from clipboard
     */
    private void handlePasteImage() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
                     
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
                
                if (image != null) {
                    // release memory resources
                    if (currentImage != null) {
                        currentImage.flush();     
                        currentImage = null;
                        System.out.println("Previous Image flushed");
                    }
                    // Create temp file to store image
                    File tempFile = File.createTempFile("ocr_pasted_", ".png");
                    tempFile.deleteOnExit();
                    
                    ImageIO.write(image, "png", tempFile);
                    
                    loadImageFromFile(tempFile);
                    
                    view.setStatus("Image pasted from clipboard");
                    System.out.println("Image pasted from clipboard");
                }
            } else {
                view.setStatus("No image found in clipboard");
                System.out.println("No image was found in clipboard. Use valid formats.");
            }
            
        } catch (Exception e) {
            view.showError("Failed to paste image from clipboard:\n" + e.getMessage());
            System.err.println("Clipboard paste error: " + e.getMessage());
        }
    }

    /**
     * Load image from file (shared by drag-drop, paste, and file picker)
     */
    private void loadImageFromFile(File selectedFile) {
        if (selectedFile == null) {
            return;
        }
        
        if (!fileManager.isValidImageFile(selectedFile)) {
            view.showError("Invalid image file.\n" +
                          "Please use a valid image file (JPG, PNG, BMP, TIFF, GIF).");
            view.setStatus("Invalid image file");
            return;
        }
        
        view.setStatus("Loading image...");
        
        BufferedImage image = imageProcessor.loadImage(selectedFile);
        
        if (image == null) {
            view.showError("Failed to load image.\n" +
                          "The file may be corrupted or in an unsupported format.");
            view.setStatus("Failed to load image");
            return;
        }
        
        this.currentImageFile = selectedFile;
        this.currentImage = image;
        
        view.displayImage(image);
        
        String imageInfo = String.format("Image: %s (%dx%d) - %s",
                selectedFile.getName(),
                image.getWidth(),
                image.getHeight(),
                fileManager.getFormattedFileSize(selectedFile));
        view.setImageInfo(imageInfo);
        
        view.setExtractButtonEnabled(true);
        view.setStatus("Image loaded - Select 'Extract Text' for full image or 'Select Area' for specific regions");
        
        System.out.println("Image loaded: " + selectedFile.getAbsolutePath());
    }
    
    /**
     * Validate and set the saved language preference
     * Falls back to English if saved language is not available
     */
    private void validateAndSetSavedLanguage() {
        String savedLanguage = AppPreferences.getLastLanguage();
        String languageCode = OCREngine.mapLanguageToCode(savedLanguage);
        
        boolean success = ocrEngine.setLanguage(languageCode);
        
        if (!success) {
            // last used language not found anymore, reset to English
            System.out.println("Saved language '" + savedLanguage + "' not available. Resetting to English.");
            
            view.getLanguageComboBox().setSelectedIndex(0); // set to english
            AppPreferences.saveLastLanguage("English");
            ocrEngine.setLanguage("eng");
            
            view.showInfo(
                "Language data for '" + savedLanguage + "' was not found.\n\n" +
                "Resetting to English.\n\n" +
                "To use other languages, download language data from:\n" +
                "https://github.com/tesseract-ocr/tessdata"
            );
        } else {
            System.out.println("Language restored from preferences: " + savedLanguage);
        }
    }
}