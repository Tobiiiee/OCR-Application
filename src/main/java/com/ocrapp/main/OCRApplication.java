package com.ocrapp.main;

import com.ocrapp.controller.OCRController;
import com.ocrapp.view.OCRView;

import javax.swing.*;

/**
 * Main application class - Entry point for the OCR Application.
 * Initializes and launches the GUI.
 */
public class OCRApplication {
    
    /**
     * Main method - Application entry point
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
    	
        System.out.println("========================================");
        System.out.println("   OCR Application Starting...        ");
        System.out.println("========================================");
        System.out.println("Initializing components...");
        
        // Set system look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("UI Look and Feel set successfully");
        } catch (Exception e) {
            System.err.println("Failed to set Look and Feel: " + e.getMessage());
            // default look & feel
        }
        
        // Launch application on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                OCRView view = new OCRView();
                
                OCRController controller = new OCRController(view);
                
                view.setVisible(true);
                
                System.out.println("Application launched successfully!");
                System.out.println("========================================");
                System.out.println();
                
                System.out.println(controller.getOCREngineInfo());
                
            } catch (Exception e) {
                System.err.println("Fatal error during application startup:");
                e.printStackTrace();
                
                // error dialog
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start OCR Application:\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                
                System.exit(1);
            }
        });
    }
}