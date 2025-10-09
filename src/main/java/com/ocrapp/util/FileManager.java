package com.ocrapp.util;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for file operations.
 * Handles file selection dialogs, image loading, and text saving.
 */
public class FileManager {
    
    // Supported image formats
    private static final List<String> SUPPORTED_IMAGE_FORMATS = Arrays.asList(
            "jpg", "jpeg", "png", "bmp", "tiff", "tif", "gif"
    );
    
    private static final String IMAGE_FILTER_DESCRIPTION = "Image Files (*.jpg, *.jpeg, *.png, *.bmp, *.tiff, *.gif)";
    private static final String TEXT_FILTER_DESCRIPTION = "Text Files (*.txt)";
    
    private JFileChooser fileChooser;
    
    /**
     * Constructor - initializes file chooser
     */
    public FileManager() {
        this.fileChooser = new JFileChooser();
        this.fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }
    
    /**
     * Open file dialog to select an image file
     * @return Selected File object, or null if cancelled
     */
    public File selectImageFile() {
        // Reset file chooser
        fileChooser.resetChoosableFileFilters();
        
        // Create file filter for images
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                IMAGE_FILTER_DESCRIPTION,
                "jpg", "jpeg", "png", "bmp", "tiff", "tif", "gif"
        );
        
        fileChooser.setFileFilter(imageFilter);
        fileChooser.setDialogTitle("Select Image File");
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate the file
            if (isValidImageFile(selectedFile)) {
                return selectedFile;
            } else {
                System.err.println("Invalid image file selected: " + selectedFile.getName());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Open file dialog to select location for saving text
     * @param defaultFileName Default name for the file
     * @return Selected File object, or null if cancelled
     */
    public File selectSaveLocation(String defaultFileName) {
        // Reset file chooser
        fileChooser.resetChoosableFileFilters();
        
        // Create file filter for text files
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter(
                TEXT_FILTER_DESCRIPTION,
                "txt"
        );
        
        fileChooser.setFileFilter(textFilter);
        fileChooser.setDialogTitle("Save Text File");
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        int result = fileChooser.showSaveDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Ensure .txt extension
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            
            return selectedFile;
        }
        
        return null;
    }
    
    /**
     * Save text content to a file
     * @param text Text content to save
     * @param file File to save to
     * @return true if successful, false otherwise
     */
    public boolean saveTextToFile(String text, File file) {
        if (text == null || file == null) {
            System.err.println("Invalid parameters for saving file");
            return false;
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
            System.out.println("Text saved successfully to: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validate if a file is a supported image format
     * @param file File to validate
     * @return true if valid image file, false otherwise
     */
    public boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        
        // Check if file has a supported extension
        for (String format : SUPPORTED_IMAGE_FORMATS) {
            if (fileName.endsWith("." + format)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension from a file
     * @param file File to get extension from
     * @return File extension (lowercase), or empty string if none
     */
    public String getFileExtension(File file) {
        if (file == null) {
            return "";
        }
        
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * Check if a file exists
     * @param filePath Path to the file
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
    
    /**
     * Get file size in bytes
     * @param file File to get size of
     * @return File size in bytes, or -1 if error
     */
    public long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        
        return file.length();
    }
    
    /**
     * Get formatted file size (e.g., "2.5 MB")
     * @param file File to get size of
     * @return Formatted file size string
     */
    public String getFormattedFileSize(File file) {
        long bytes = getFileSize(file);
        
        if (bytes < 0) {
            return "Unknown";
        }
        
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Get list of supported image formats
     * @return List of supported extensions
     */
    public static List<String> getSupportedFormats() {
        return SUPPORTED_IMAGE_FORMATS;
    }
    
    /**
     * Generate default filename for saving extracted text
     * @param sourceImageFile Source image file
     * @return Generated filename
     */
    public String generateDefaultSaveFileName(File sourceImageFile) {
        if (sourceImageFile == null) {
            return "extracted_text.txt";
        }
        
        String baseName = sourceImageFile.getName();
        int lastDotIndex = baseName.lastIndexOf('.');
        
        if (lastDotIndex > 0) {
            baseName = baseName.substring(0, lastDotIndex);
        }
        
        return baseName + "_extracted.txt";
    }
}