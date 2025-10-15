package com.ocrapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Interactive panel for selecting rectangular regions from images.
 * Allows user to select a region by clicking and dragging.
 * Automatically triggers callback when selection is complete.
 */
public class ImageCropPanel extends JPanel {
    
    // this is unused, its just here to remove eclipse warning
    private static final long serialVersionUID = 1L;
        
    private BufferedImage image;
    private BufferedImage scaledImage;
    private Rectangle selectionRect;
    private Point startPoint;
    private Point endPoint;
    private boolean isDragging;
    private boolean selectionEnabled;
    
    private double scaleX;
    private double scaleY;
    
    private static final Color SELECTION_COLOR = new Color(0, 120, 215, 100);
    private static final Color BORDER_COLOR = new Color(0, 120, 215);
    private static final BasicStroke BORDER_STROKE = new BasicStroke(2.0f);
    
    private int imageXOffset;
    private int imageYOffset;
    
    // Callback for when selection is complete
    private Consumer<BufferedImage> onSelectionComplete;

    /**
     * Constructor
     */
    public ImageCropPanel() {
        this.selectionRect = null;
        this.isDragging = false;
        this.selectionEnabled = false;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
        
        setupMouseListeners();
    }
    
    /**
     * Set callback for selection completion
     * @param callback Function to call when selection is complete
     */
    public void setOnSelectionComplete(Consumer<BufferedImage> callback) {
        this.onSelectionComplete = callback;
    }
    
    /**
     * Enable or disable selection mode
     * @param enabled true to enable selection
     */
    public void setSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
        if (!enabled) {
            clearSelection();
        }
        repaint();
    }
    
    /**
     * Check if selection mode is enabled
     * @return true if selection is enabled
     */
    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }
    
    /**
     * Setup mouse listeners for selection
     */
    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (image != null && selectionEnabled) {
                    startPoint = e.getPoint();
                    endPoint = startPoint;
                    isDragging = true;
                    selectionRect = null;
                    repaint();
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && image != null && selectionEnabled) {
                    endPoint = e.getPoint();
                    updateSelectionRect();
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && image != null && selectionEnabled) {
                    endPoint = e.getPoint();
                    isDragging = false;
                    updateSelectionRect();
                    repaint();
                    
                    // Auto-trigger OCR on valid selection
                    if (hasSelection()) {
                        BufferedImage selectedRegion = getSelectedRegion();
                        if (selectedRegion != null && onSelectionComplete != null) {
                            // Disable selection mode after completing
                            selectionEnabled = false;
                            
                            // Trigger callback
                            SwingUtilities.invokeLater(() -> 
                                onSelectionComplete.accept(selectedRegion));
                        }
                    }
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (selectionEnabled && !isDragging) {
                    selectionEnabled = false;
                    repaint();
                }
                setCursor(Cursor.getDefaultCursor());
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
        // Auto-enable selection when hovering over image with loaded image
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (image != null) {
                    // Auto-enable selection mode when hovering
                    if (!selectionEnabled) {
                        selectionEnabled = true;
                        repaint();
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    selectionEnabled = false;
                }
            }
        });
    }
    
    /**
     * Update selection rectangle based on start and end points
     */
    private void updateSelectionRect() {
        if (startPoint != null && endPoint != null) {
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            int width = Math.abs(endPoint.x - startPoint.x);
            int height = Math.abs(endPoint.y - startPoint.y);
            
            selectionRect = new Rectangle(x, y, width, height);
        }
    }
    
    /**
     * Set image to display
     * @param image Image to display
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        this.selectionRect = null;
        this.startPoint = null;
        this.endPoint = null;
        
        if (image != null) {
            scaleImageToFit();
        } else {
            this.scaledImage = null;
        }
        
        repaint();
    }
    
    /**
     * Scale image to fit panel while maintaining aspect ratio
     */
    private void scaleImageToFit() {
        if (image == null) return;
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        if (panelWidth <= 0 || panelHeight <= 0) {
            panelWidth = 600;
            panelHeight = 400;
        }
        
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        
        double scale = Math.min(
            (double) panelWidth / imgWidth,
            (double) panelHeight / imgHeight
        );
        
        int scaledWidth = (int) (imgWidth * scale);
        int scaledHeight = (int) (imgHeight * scale);
        
        scaleX = (double) imgWidth / scaledWidth;
        scaleY = (double) imgHeight / scaledHeight;
        
        imageXOffset = (panelWidth - scaledWidth) / 2;
        imageYOffset = (panelHeight - scaledHeight) / 2;
        
        Image scaled = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
    }
    
    /**
     * Get selected region from original image
     * @return BufferedImage of selected region, or null if no selection
     */
    public BufferedImage getSelectedRegion() {
        if (image == null || selectionRect == null) return null;

        // Adjust for centering offset
        int adjX = selectionRect.x - imageXOffset;
        int adjY = selectionRect.y - imageYOffset;

        // Ensure selection is inside displayed image
        if (adjX < 0 || adjY < 0 || 
            adjX > scaledImage.getWidth() || adjY > scaledImage.getHeight()) {
            return null;
        }
 
        // Convert to original image coordinates
        int x = (int) (adjX * scaleX);
        int y = (int) (adjY * scaleY);
        int width = (int) (selectionRect.width * scaleX);
        int height = (int) (selectionRect.height * scaleY);

        x = Math.max(0, Math.min(x, image.getWidth() - 1));
        y = Math.max(0, Math.min(y, image.getHeight() - 1));
        width = Math.min(width, image.getWidth() - x);
        height = Math.min(height, image.getHeight() - y);

        if (width <= 0 || height <= 0) return null;

        try {
            return image.getSubimage(x, y, width, height);
        } catch (Exception e) {
            System.err.println("Error extracting region: " + e.getMessage());
            return null;
        }
    }

    
    /**
     * Check if a selection has been made
     * @return true if selection exists
     */
    public boolean hasSelection() {
        return selectionRect != null && selectionRect.width > 10 && selectionRect.height > 10;
    }
    
    /**
     * Clear selection
     */
    public void clearSelection() {
        selectionRect = null;
        startPoint = null;
        endPoint = null;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (scaledImage != null) {
            int x = (getWidth() - scaledImage.getWidth()) / 2;
            int y = (getHeight() - scaledImage.getHeight()) / 2;
            g2d.drawImage(scaledImage, x, y, null);
            
            // Draw selection indicator message when in selection mode
            if (selectionEnabled && !isDragging && selectionRect == null) {
                g2d.setColor(new Color(0, 0, 0, 150));
                String msg = "Click and drag to select text region";
                FontMetrics fm = g2d.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                int msgX = (getWidth() - msgWidth) / 2;
                int msgY = 30;
                
                g2d.fillRoundRect(msgX - 10, msgY - 20, msgWidth + 20, 30, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString(msg, msgX, msgY);
            }
            
            if (selectionRect != null) {
                g2d.setColor(SELECTION_COLOR);
                g2d.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
                
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(BORDER_STROKE);
                g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
                
                String dimensions = selectionRect.width + " × " + selectionRect.height;
                g2d.setColor(Color.WHITE);
                g2d.fillRect(selectionRect.x + 5, selectionRect.y + 5, 100, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString(dimensions, selectionRect.x + 10, selectionRect.y + 20);
            }
        } else {
            g2d.setColor(Color.GRAY);
            String message = "No image loaded";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
}