package com.ocrapp.view;

import com.ocrapp.util.AppPreferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JButton clearButton;
    private JButton copyClipboardButton;

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

    	applyThemeDefaults();
        initializeComponents();
        setupLayout();
        setupMenuBar();
        configureWindow();


        setFocusable(true); 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow(); // click focuses this panel
            }
        });
    }

    /**
     * Initialize all GUI components
     */
    private void initializeComponents() {
        // Main panel
        mainPanel = new JPanel();
        mainPanel.setBackground(Theme.getBgPrimary());
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Image panel
        imageCropPanel = new ImageCropPanel();
        imageCropPanel.setPreferredSize(new Dimension(550, 500));
        imageCropPanel.setBackground(Theme.getBgSecondary());
        imageCropPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.getBorder()));

        imageCropPanel.setDropTarget(null); // Placeholder, controller will set actual target

        imageScrollPane = new JScrollPane(imageCropPanel);
        imageScrollPane.setPreferredSize(new Dimension(570, 520));
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        imageScrollPane.getHorizontalScrollBar().setBackground(Theme.getBgTertiary());
        imageScrollPane.getVerticalScrollBar().setBackground(Theme.getBgTertiary());
        
        JPanel imageHeaderPanel = new JPanel(new BorderLayout());
        imageHeaderPanel.setBackground(Theme.getBgSecondary());
        imageHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorder()));

        JLabel imageHeaderLabel = new JLabel("Image Preview");
        imageHeaderLabel.setFont(Theme.FONT_MEDIUM);
        imageHeaderLabel.setForeground(Theme.getTextPrimary());
        imageHeaderLabel.setBorder(new EmptyBorder(12, 15, 12, 15));
        imageHeaderPanel.add(imageHeaderLabel, BorderLayout.WEST);

        imagePanelContainer = new JPanel(new BorderLayout());
        imagePanelContainer.setBackground(Theme.getBgSecondary());
        imagePanelContainer.add(imageHeaderPanel, BorderLayout.NORTH);
        imagePanelContainer.add(imageScrollPane, BorderLayout.CENTER);

        imageInfoLabel = new JLabel("Image: None");
        imageInfoLabel.setFont(Theme.FONT_REGULAR);
        imageInfoLabel.setForeground(Theme.getTextSecondary());
        imageInfoLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
        imagePanelContainer.add(imageInfoLabel, BorderLayout.SOUTH);

        textArea = new JTextArea();
        textArea.setFont(Theme.FONT_MONO);
        textArea.setBackground(Theme.getBgTertiary());
        textArea.setForeground(Theme.getTextPrimary());
        textArea.setCaretColor(Theme.getTextPrimary());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(true);

        undoManager = new UndoManager();
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        if (undoManager.canUndo()) {
                            undoManager.undo();
                            updateUndoRedoState();
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        if (undoManager.canRedo()) {
                            undoManager.redo();
                            updateUndoRedoState();
                        }
                        e.consume();
                    }
                }
            }
        });

        textArea.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            updateUndoRedoState();
        });

        textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(570, 520));
        lineNumberPanel = new LineNumberPanel(textArea);
        textScrollPane.setRowHeaderView(lineNumberPanel);

        textInfoLabel = new JLabel("Text: 0 characters, 0 words");
        textInfoLabel.setFont(Theme.FONT_REGULAR);
        textInfoLabel.setForeground(Theme.getTextSecondary());
        textInfoLabel.setBackground(Theme.getBgTertiary());
        textInfoLabel.setOpaque(true);
        textInfoLabel.setBorder(new EmptyBorder(8, 15, 8, 15));

        JPanel textHeaderPanel = new JPanel(new BorderLayout());
        textHeaderPanel.setBackground(Theme.getBgTertiary());
        textHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorder()));

        JLabel textHeaderLabel = new JLabel("Extracted Text");
        textHeaderLabel.setFont(Theme.FONT_MEDIUM);
        textHeaderLabel.setForeground(Theme.getTextPrimary());
        textHeaderLabel.setBorder(new EmptyBorder(12, 15, 12, 15));
        textHeaderPanel.add(textHeaderLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(Theme.getBgTertiary());
        textPanel.add(textHeaderPanel, BorderLayout.NORTH);
        textPanel.add(textScrollPane, BorderLayout.CENTER);
        textPanel.add(textInfoLabel, BorderLayout.SOUTH);

        // Buttons
        loadImageButton = createStyledButton("Open", 120, 35);
        loadImageButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadImageButton.setPreferredSize(new Dimension(150, 40));
        loadImageButton.setToolTipText("Load an image file for OCR processing");

        extractTextButton = createStyledButton("Extract All", 120, 35);
        extractTextButton.setFont(new Font("Arial", Font.BOLD, 14));
        extractTextButton.setPreferredSize(new Dimension(150, 40));
        extractTextButton.setToolTipText("Perform OCR on the entire loaded image");

        copyClipboardButton = createStyledButton("Copy All", 120, 35);
        copyClipboardButton.setFont(new Font("Arial", Font.BOLD, 14));
        copyClipboardButton.setPreferredSize(new Dimension(150, 40));
        copyClipboardButton.setToolTipText("Perform OCR on the entire loaded image");

        clearButton = createStyledButton("Clear All", 120, 35, Theme.getError());
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setPreferredSize(new Dimension(150, 40));
        clearButton.setToolTipText("Clear image and text");

        clearMenuItem = new JMenuItem("Clear All");
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke("control L"));
        clearMenuItem.setMnemonic('L');

        // Language selection
        languageLabel = new JLabel("Select Language:");
        languageLabel.setFont(Theme.FONT_REGULAR);
        languageLabel.setForeground(Theme.getTextPrimary());

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
        languageComboBox.setFont(Theme.FONT_REGULAR);
        languageComboBox.setBackground(Theme.getBgPrimary());
        languageComboBox.setPreferredSize(new Dimension(150, 40));
        languageComboBox.setBorder(BorderFactory.createLineBorder(Theme.getBorder()));

        // Load last selected language
        String lastLanguage = AppPreferences.getLastLanguage();
        for (int i = 0; i < languageComboBox.getItemCount(); i++) {
            if (languageComboBox.getItemAt(i).equals(lastLanguage)) {
                languageComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setBackground(Theme.getBgTertiary());
        progressBar.setForeground(Theme.getAccent());
        progressBar.setBorderPainted(false);

        progressLabel = new JLabel("");
        progressLabel.setFont(Theme.FONT_REGULAR);
        progressLabel.setForeground(Theme.getTextSecondary());
        progressLabel.setVisible(false);

        // Status label
        statusLabel = new JLabel("Ready - OCR Engine successfully initialized");
        statusLabel.setFont(Theme.FONT_REGULAR);
        statusLabel.setForeground(Theme.getTextSecondary());
        statusLabel.setBackground(Theme.getBgSecondary());
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

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
    	JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
    	languagePanel.setBackground(Theme.getBgSecondary());
    	languagePanel.add(languageLabel);
    	languagePanel.add(languageComboBox);
    	languagePanel.add(loadImageButton);

    	// Button panel
    	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    	buttonPanel.setBackground(Theme.getBgSecondary());
    	buttonPanel.add(extractTextButton);
    	buttonPanel.add(copyClipboardButton);
    	buttonPanel.add(clearButton);

    	// bottom control panel
    	JPanel controlPanel = new JPanel(new BorderLayout());
    	controlPanel.setBackground(Theme.getBgSecondary());
    	controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.getBorder()));
    	controlPanel.add(languagePanel, BorderLayout.WEST);
    	controlPanel.add(buttonPanel, BorderLayout.EAST);

    	mainPanel.add(controlPanel, BorderLayout.SOUTH);

    	// Status panel - three sections: left (status), center (progress), right (empty for balance)
    	JPanel statusPanel = new JPanel(new BorderLayout());
    	statusPanel.setBackground(Theme.getBgSecondary());

    	// Left side - status label
    	statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    	JPanel statusLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    	statusLeftPanel.setBackground(Theme.getBgSecondary());
    	statusLeftPanel.add(statusLabel);
    	statusPanel.add(statusLeftPanel, BorderLayout.WEST);

    	// Center - progress bar and label
    	JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    	progressPanel.setBackground(Theme.getBgSecondary());
    	progressPanel.add(progressLabel);
    	progressPanel.add(progressBar);
    	statusPanel.add(progressPanel, BorderLayout.CENTER);

    	// Right side - Empty (for now) for balance
    	JPanel statusRightPanel = new JPanel();
    	statusRightPanel.setBackground(Theme.getBgSecondary());
    	statusRightPanel.setPreferredSize(new Dimension(200, 0));
    	statusPanel.add(statusRightPanel, BorderLayout.EAST);

        // Main container
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
    	menuBar = new JMenuBar();
    	menuBar.setBackground(Theme.getBgSecondary());
    	menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorder()));


    	// File Menu
    	fileMenu = new JMenu("File");
    	fileMenu.setFont(Theme.FONT_REGULAR);
    	fileMenu.setForeground(Theme.getTextPrimary());
    	fileMenu.setMnemonic('F');

    	// Apply to all menu items
    	openMenuItem = createStyledMenuItem("Open Image...", "control O", 'O');
    	saveMenuItem = createStyledMenuItem("Save Text...", "control S", 'S');
    	saveMenuItem.setEnabled(false);
    	exitMenuItem = createStyledMenuItem("Exit", "control Q", 'X');

    	fileMenu.add(openMenuItem);
    	fileMenu.add(saveMenuItem);
    	fileMenu.addSeparator();
    	fileMenu.add(exitMenuItem);

    	// Edit Menu
    	editMenu = new JMenu("Edit");
    	editMenu.setFont(Theme.FONT_REGULAR);
    	editMenu.setForeground(Theme.getTextPrimary());
    	editMenu.setMnemonic('E');

    	undoMenuItem = createStyledMenuItem("Undo", "control Z", (char)0);
    	undoMenuItem.setEnabled(false);
    	redoMenuItem = createStyledMenuItem("Redo", "control Y", (char)0);
    	redoMenuItem.setEnabled(false);
    	cutMenuItem = createStyledMenuItem("Cut", "control X", (char)0);
    	copyMenuItem = createStyledMenuItem("Copy Text", "control C", 'C');
    	copyMenuItem.setEnabled(false);
    	pasteMenuItem = createStyledMenuItem("Paste", "control V", (char)0);
    	selectAllMenuItem = createStyledMenuItem("Select All", "control A", (char)0);
    	clearMenuItem = createStyledMenuItem("Clear All", "control L", 'L');

    	editMenu.add(undoMenuItem);
    	editMenu.add(redoMenuItem);
    	editMenu.addSeparator();
    	editMenu.add(cutMenuItem);
    	editMenu.add(copyMenuItem);
    	editMenu.add(pasteMenuItem);
    	editMenu.addSeparator();
    	editMenu.add(selectAllMenuItem);
    	editMenu.addSeparator();
    	editMenu.add(clearMenuItem);

    	// Help Menu
    	helpMenu = new JMenu("Help");
    	helpMenu.setFont(Theme.FONT_REGULAR);
    	helpMenu.setForeground(Theme.getTextPrimary());
    	helpMenu.setMnemonic('H');

    	aboutMenuItem = createStyledMenuItem("About", null, 'A');
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
        saveMenuItem.setEnabled(false);
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
    public void setSaveMenuEnabled(boolean enabled) {
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

    private void applyThemeDefaults() {
        UIManager.put("Panel.background", Theme.getBgPrimary());
        UIManager.put("Label.foreground", Theme.getTextPrimary());
        UIManager.put("Button.background", Theme.getBgSecondary());
        UIManager.put("Button.foreground", Theme.getTextPrimary());
        UIManager.put("TextField.background", Theme.getBgTertiary());
        UIManager.put("TextField.foreground", Theme.getTextPrimary());
        UIManager.put("TextArea.background", Theme.getBgTertiary());
        UIManager.put("TextArea.foreground", Theme.getTextPrimary());
        UIManager.put("MenuBar.background", new Color(40, 40, 40));
        UIManager.put("Menu.background", new Color(50, 50, 50));
        UIManager.put("MenuItem.background", Theme.getBgSecondary());
        UIManager.put("MenuItem.foreground", Theme.getTextPrimary());
        UIManager.put("MenuBar.background", Theme.getBgSecondary());
        UIManager.put("MenuBar.foreground", Theme.getTextPrimary());

        UIManager.put("ComboBox.background", Theme.getBgSecondary());
        UIManager.put("ComboBox.foreground", Theme.getTextPrimary());
        UIManager.put("ComboBox.selectionBackground", Theme.getAccent());
        UIManager.put("ComboBox.selectionForeground", Theme.getTextPrimary());
        UIManager.put("ComboBox.buttonBackground", Theme.getBgSecondary());
        UIManager.put("ComboBox.buttonDarkShadow", Theme.getBorder());

    }

    private JButton createStyledButton(String text, int width, int height) {
        return createStyledButton(text, width, height, Theme.getAccent());
    }

    private JButton createStyledButton(String text, int width, int height, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(Theme.FONT_BUTTON);
        button.setPreferredSize(new Dimension(width, height));
        button.setBackground(bgColor);
        button.setForeground(Theme.getTextPrimary());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor == Theme.getError() ? 
                    new Color(220, 50, 50) : Theme.getAccentHover());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JMenuItem createStyledMenuItem(String text, String accelerator, char mnemonic) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.FONT_REGULAR);
        item.setBackground(Theme.getBgSecondary());
        item.setForeground(Theme.getTextPrimary());

        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        if (mnemonic != 0) {
            item.setMnemonic(mnemonic);
        }

        return item;
    }

    // ========== Getters for Buttons (for Controller to add listeners) ==========

    public JButton getLoadImageButton() {
        return loadImageButton;
    }

    public JButton getExtractTextButton() {
        return extractTextButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getCopyClipboardButton() {
    	return copyClipboardButton;
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

    public ImageCropPanel getImageCropPanel() {
        return imageCropPanel;
    }
}