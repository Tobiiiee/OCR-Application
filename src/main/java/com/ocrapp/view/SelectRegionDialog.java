package com.ocrapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Dialog for selecting specific regions from images for focused OCR.
 */
public class SelectRegionDialog extends JDialog {
    
	// this is unused, its just here to remove eclipse warning
	private static final long serialVersionUID = 1L;
	
    private ImageCropPanel selectionPanel;
    private JButton selectButton;
    private JButton cancelButton;
    private JButton resetButton;
    private JLabel instructionLabel;
    
    private BufferedImage selectedRegion;
    private boolean approved;
    
    /**
     * Constructor
     * @param parent Parent frame
     * @param image Image to select from
     */
    public SelectRegionDialog(JFrame parent, BufferedImage image) {
        super(parent, "Select Region for OCR", true);
        
        this.selectedRegion = null;
        this.approved = false;
        
        initializeComponents(image);
        setupLayout();
        configureDialog();
    }
    
    /**
     * Initialize components
     */
    private void initializeComponents(BufferedImage image) {
        selectionPanel = new ImageCropPanel();
        selectionPanel.setImage(image);
        selectionPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        instructionLabel = new JLabel("Click and drag to select the text region");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        selectButton = new JButton("Extract from Selection");
        selectButton.setFont(new Font("Arial", Font.BOLD, 14));
        selectButton.setPreferredSize(new Dimension(200, 35));
        selectButton.addActionListener(e -> handleSelect());
        
        resetButton = new JButton("Reset Selection");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.setPreferredSize(new Dimension(150, 35));
        resetButton.addActionListener(e -> selectionPanel.clearSelection());
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> handleCancel());
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        add(instructionLabel, BorderLayout.NORTH);
        add(selectionPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(resetButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Configure dialog properties
     */
    private void configureDialog() {
        setSize(700, 550);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
    }
    
    /**
     * Handle select button click
     */
    private void handleSelect() {
        if (!selectionPanel.hasSelection()) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a region by clicking and dragging on the image.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        selectedRegion = selectionPanel.getSelectedRegion();
        
        if (selectedRegion == null) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to extract selection. Please try again.",
                "Selection Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        approved = true;
        dispose();
    }
    
    /**
     * Handle cancel button click
     */
    private void handleCancel() {
        approved = false;
        selectedRegion = null;
        dispose();
    }
    
    /**
     * Get selected region
     * @return BufferedImage of selected region, or null if cancelled
     */
    public BufferedImage getSelectedRegion() {
        return selectedRegion;
    }
    
    /**
     * Check if selection was approved
     * @return true if user clicked Select button
     */
    public boolean isApproved() {
        return approved;
    }
}