package com.ocrapp.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main GUI view for the OCR Application.
 * Provides user interface for image loading, text extraction, and saving.
 */
public class OCRView extends JFrame {
	
	// this is unused, its just here to remove eclipse warning
	private static final long serialVersionUID = 1L;
	
    // GUI Components
    private JPanel mainPanel;
    private ImageCropPanel imageCropPanel;
    private JPanel imagePanelContainer;
    private JScrollPane imageScrollPane;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private LineNumberPanel lineNumberPanel;
    
    private JTextArea textArea;
    private JScrollPane textScrollPane;
    
    private JButton loadImageButton;
    private JButton extractTextButton;
    private JButton saveTextButton;
    private JButton clearButton;
    private JButton selectRegionButton;
    private JButton copyButton;
    
    private JLabel statusLabel;
    private JLabel imageInfoLabel;
    private JLabel textInfoLabel;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu helpMenu;
    
    private JComboBox<String> languageComboBox;
    private JLabel languageLabel;
    
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem clearMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem copyMenuItem;
    
    private UndoManager undoManager;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem selectAllMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem pasteMenuItem;
    
    // Window properties
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 700;
    private static final String WINDOW_TITLE = "OCR Application - Text Extraction by Tobiiiee";
    
    public OCRView() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        configureWindow();
    }
    
    /**
     * Initialize all GUI components
     */
    private void initializeComponents() {
        // Main panel
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Image display components - interactive selection panel
        imageCropPanel = new ImageCropPanel();
        imageCropPanel.setPreferredSize(new Dimension(550, 500));
        imageCropPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        imageScrollPane = new JScrollPane(imageCropPanel);
        imageScrollPane.setPreferredSize(new Dimension(570, 520));

        imagePanelContainer = new JPanel(new BorderLayout());
        imagePanelContainer.setBorder(new TitledBorder("Image Preview"));
        imagePanelContainer.add(imageScrollPane, BorderLayout.CENTER);

        imageInfoLabel = new JLabel("Image: None");
        imageInfoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        imagePanelContainer.add(imageInfoLabel, BorderLayout.SOUTH);
        
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(true);
        
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            updateUndoRedoState();
        });
        
        textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(570, 520));
        lineNumberPanel = new LineNumberPanel(textArea);
        textScrollPane.setRowHeaderView(lineNumberPanel);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(new TitledBorder("Extracted Text"));
        textPanel.add(textScrollPane, BorderLayout.CENTER);
        
        textInfoLabel = new JLabel("Text: 0 characters, 0 words");
        textInfoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        textPanel.add(textInfoLabel, BorderLayout.SOUTH);
        
        // Buttons
        loadImageButton = new JButton("Load Image");
        loadImageButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadImageButton.setPreferredSize(new Dimension(150, 40));
        loadImageButton.setToolTipText("Load an image file for OCR processing");
        
        selectRegionButton = new JButton("Select Area");
        selectRegionButton.setFont(new Font("Arial", Font.BOLD, 14));
        selectRegionButton.setPreferredSize(new Dimension(150, 40));
        selectRegionButton.setEnabled(false);
        selectRegionButton.setToolTipText("Select a specific region from the image for OCR");
        
        extractTextButton = new JButton("Extract Text");
        extractTextButton.setFont(new Font("Arial", Font.BOLD, 14));
        extractTextButton.setPreferredSize(new Dimension(150, 40));
        extractTextButton.setEnabled(false);
        extractTextButton.setToolTipText("Perform OCR on the entire loaded image");
        
        saveTextButton = new JButton("Save Text");
        saveTextButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveTextButton.setPreferredSize(new Dimension(150, 40));
        saveTextButton.setEnabled(false);
        saveTextButton.setToolTipText("Save extracted text to a file");
        
        copyButton = new JButton("Copy to Clipboard");
        copyButton.setFont(new Font("Arial", Font.BOLD, 14));
        copyButton.setPreferredSize(new Dimension(180, 40));
        copyButton.setEnabled(false);
        copyButton.setToolTipText("Copy extracted text to clipboard");
        
        clearButton = new JButton("Clear All");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setPreferredSize(new Dimension(150, 40));
        clearButton.setToolTipText("Clear image and text");
        
        clearMenuItem = new JMenuItem("Clear All");
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke("control L"));
        clearMenuItem.setMnemonic('L');
        
        // Language selection
        languageLabel = new JLabel("OCR Language:");
        languageLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        String[] languages = {
            "English",
            "Spanish", 
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Arabic",
            "Chinese (Simplified)",
            "Japanese",
            "Korean",
            "Russian"
        };

        languageComboBox = new JComboBox<>(languages);
        languageComboBox.setPreferredSize(new Dimension(150, 30));
        languageComboBox.setToolTipText("Select OCR language");
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false); // Hidden by default
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setForeground(new Color(76, 175, 80));
        
        progressLabel = new JLabel("");
        progressLabel.setVisible(false);
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Split panes for image and text
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanelContainer, textPanel);
        splitPane.setDividerLocation(580);
        splitPane.setResizeWeight(0.5);
        
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Setup the layout of components
     */
    private void setupLayout() {
    	// Language selection panel
    	JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    	languagePanel.add(languageLabel);
    	languagePanel.add(languageComboBox);

    	// Button panel - removed clearSelectionButton
    	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
    	buttonPanel.add(loadImageButton);
    	buttonPanel.add(selectRegionButton);
    	buttonPanel.add(extractTextButton);
    	buttonPanel.add(copyButton);
    	buttonPanel.add(saveTextButton);
    	buttonPanel.add(clearButton);
    	
    	// Combined panel with language selector and buttons
    	JPanel controlPanel = new JPanel(new BorderLayout());
    	controlPanel.add(languagePanel, BorderLayout.NORTH);
    	controlPanel.add(buttonPanel, BorderLayout.CENTER);

    	mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Progress panel with bar and label
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        progressPanel.add(progressLabel);
        progressPanel.add(progressBar);
        progressPanel.setVisible(false); // Hidden by default

        // Status panel with progress
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressPanel, BorderLayout.NORTH);
        
        // Main container
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        openMenuItem = new JMenuItem("Open Image...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openMenuItem.setMnemonic('O');
        
        saveMenuItem = new JMenuItem("Save Text...");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setEnabled(false);
        
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitMenuItem.setMnemonic('X');
        
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        undoMenuItem.setEnabled(false);

        redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        redoMenuItem.setEnabled(false);

        cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke("control X"));

        copyMenuItem = new JMenuItem("Copy Text");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        copyMenuItem.setEnabled(false);

        pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke("control V"));

        selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("control A"));

        editMenu.add(clearMenuItem);
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.addSeparator();
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(selectAllMenuItem);
        editMenu.addSeparator();
        
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setMnemonic('A');
        
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Configure window properties
     */
    private void configureWindow() {
        setTitle(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setMinimumSize(new Dimension(800, 600));
        
        // Set application icon (later if made)
        try {
            // setIconImage(ImageIO.read(new File("icon.png")));
        } catch (Exception e) {
            // Icon not available, continue without it
        }
    }
    
    // ========== Public Methods for Controller ==========
    
    /**
     * Display an image in the image panel
     * @param image BufferedImage to display
     */
    public void displayImage(BufferedImage image) {
        imageCropPanel.setImage(image);
    }
    
    /**
     * Display text in the text area
     * @param text Text to display
     */
    public void displayText(String text) {
        if (text == null) {
            textArea.setText("");
        } else {
            textArea.setText(text);
            textArea.setCaretPosition(0); // Scroll to top
        }
    }
    
    /**
     * Get text from text area
     * @return Current text in text area
     */
    public String getText() {
        return textArea.getText();
    }
    
    /**
     * Update status label
     * @param status Status message
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * Update image info label
     * @param info Image information
     */
    public void setImageInfo(String info) {
        imageInfoLabel.setText(info);
    }
    
    /**
     * Update text info label
     * @param info Text information
     */
    public void setTextInfo(String info) {
        textInfoLabel.setText(info);
    }
    
    /**
     * Show error message dialog
     * @param message Error message
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show information message dialog
     * @param message Information message
     */
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show success message dialog
     * @param message Success message
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Clear all content (image and text)
     */
    public void clearAll() {
    	imageCropPanel.setImage(null);
    	imageCropPanel.clearSelection();
        textArea.setText("");
        undoManager.discardAllEdits();
        updateUndoRedoState();
        imageInfoLabel.setText("Image: None");
        textInfoLabel.setText("Text: 0 characters, 0 words");
        setStatus("Ready");
        extractTextButton.setEnabled(false);
        copyButton.setEnabled(false);
        saveTextButton.setEnabled(false);
        saveMenuItem.setEnabled(false);
        selectRegionButton.setEnabled(false);
        selectRegionButton.setText("Select Area"); // Reset button text
    }
    
    /**
     * Enable or disable extract button
     * @param enabled true to enable, false to disable
     */
    public void setExtractButtonEnabled(boolean enabled) {
        extractTextButton.setEnabled(enabled);
    }
    
    /**
     * Enable or disable save button
     * @param enabled true to enable, false to disable
     */
    public void setSaveButtonEnabled(boolean enabled) {
        saveTextButton.setEnabled(enabled);
        saveMenuItem.setEnabled(enabled);
    }
    
    /**
     * Show about dialog
     */
    public void showAboutDialog() {
        String message = "OCR Application v1.0\n\n" +
                "A Java-based Optical Character Recognition application\n" +
                "using Tesseract OCR engine.\n\n" +
                "Developed using:\n" +
                "- Java 21\n" +
                "- Tesseract OCR\n" +
                "- Maven\n" +
                "- Swing GUI\n\n" +
                "© 2025 - Tobiiiee";
        
        JOptionPane.showMessageDialog(this, message, "About OCR Application", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show progress bar with message
     * @param message Progress message
     */
    public void showProgress(String message) {
        progressLabel.setText(message);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.getParent().setVisible(true);
    }

    /**
     * Update progress bar
     * @param value Progress value (0-100)
     * @param message Progress message
     */
    public void updateProgress(int value, String message) {
        progressBar.setValue(value);
        progressLabel.setText(message);
    }

    /**
     * Hide progress bar
     */
    public void hideProgress() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        progressBar.getParent().setVisible(false);
    }
    
    /**
     * Update undo/redo menu item states
     */
    private void updateUndoRedoState() {
        undoMenuItem.setEnabled(undoManager.canUndo());
        redoMenuItem.setEnabled(undoManager.canRedo());
    }
    
    public boolean hasSelection() {
        return imageCropPanel.hasSelection();
    }

    public void clearSelection() {
        imageCropPanel.clearSelection();
    }
    
    public void setCopyButtonEnabled(boolean enabled) {
        copyButton.setEnabled(enabled);
    }
    
    public void setSelectRegionButtonEnabled(boolean enabled) {
        selectRegionButton.setEnabled(enabled);
    }
    // ========== Getters for Buttons (for Controller to add listeners) ==========
    
    public JButton getLoadImageButton() {
        return loadImageButton;
    }
    
    public JButton getExtractTextButton() {
        return extractTextButton;
    }
    
    public JButton getSaveTextButton() {
        return saveTextButton;
    }
    
    public JButton getClearButton() {
        return clearButton;
    }
    
    public JMenuItem getOpenMenuItem() {
        return openMenuItem;
    }
    
    public JMenuItem getSaveMenuItem() {
        return saveMenuItem;
    }
    
    public JMenuItem getExitMenuItem() {
        return exitMenuItem;
    }
    
    public JMenuItem getClearMenuItem() {
        return clearMenuItem;
    }
    
    public JMenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }
    
    public JLabel getProgressLabel() {
        return progressLabel;
    }
    
    public JComboBox<String> getLanguageComboBox() {
        return languageComboBox;
    }
    
    public JButton getSelectRegionButton() {
        return selectRegionButton;
    }

    public JButton getCopyButton() {
        return copyButton;
    }
    
    public JMenuItem getCopyMenuItem() {
        return copyMenuItem;
    }
    
    public JMenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public JMenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public JMenuItem getCutMenuItem() {
        return cutMenuItem;
    }

    public JMenuItem getPasteMenuItem() {
        return pasteMenuItem;
    }

    public JMenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
    
    public BufferedImage getSelectedRegion() {
        return imageCropPanel.getSelectedRegion();
    }

    public ImageCropPanel getImagePanel() {
        return imageCropPanel;
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}