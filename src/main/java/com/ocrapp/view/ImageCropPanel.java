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
    
    private BufferedImage originalImage;
    private BufferedImage image;
    private BufferedImage scaledImage;
    private Rectangle selectionRect;
    private Point startPoint;
    private Point endPoint;
    private boolean isDragging;
    private boolean selectionEnabled;
    
    private double scaleX;
    private double scaleY;
    
    private static final Color SELECTION_COLOR = Theme.getSelectionFill();
    private static final Color BORDER_COLOR = Theme.getSelectionBorder();
    private static final BasicStroke BORDER_STROKE = new BasicStroke(2.0f);
    
    private double zoomLevel = 1.0;
    private static final double ZOOM_MIN = 0.25;  // 25%
    private static final double ZOOM_MAX = 4.0;   // 400%
    private static final double ZOOM_STEP = 0.25; // 25% per step
    
    private int imageXOffset;
    private int imageYOffset;
    
    private DropZonePanel dropZonePanel;
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
        
        setBackground(Theme.getBgSecondary());
        setPreferredSize(new Dimension(600, 400));
        setLayout(new BorderLayout());
        
        dropZonePanel = new DropZonePanel();
        add(dropZonePanel, BorderLayout.CENTER);
        
        setupMouseListeners();
        setupResizeListener();
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
        if (startPoint != null && endPoint != null && scaledImage != null) {
            int x1 = Math.max(imageXOffset, Math.min(startPoint.x, endPoint.x));
            int y1 = Math.max(imageYOffset, Math.min(startPoint.y, endPoint.y));
            int x2 = Math.min(imageXOffset + scaledImage.getWidth(), Math.max(startPoint.x, endPoint.x));
            int y2 = Math.min(imageYOffset + scaledImage.getHeight(), Math.max(startPoint.y, endPoint.y));

            int width = Math.max(0, x2 - x1);
            int height = Math.max(0, y2 - y1);

            selectionRect = new Rectangle(x1, y1, width, height);
        }
    }

    
    /**
     * Set image to display
     * @param image Image to display
     */
    public void setImage(BufferedImage image) {
        this.originalImage = image;  // ALWAYS store original
        this.image = image;           // Keep for compatibility
        this.selectionRect = null;
        this.startPoint = null;
        this.endPoint = null;
        this.zoomLevel = 1.0;
        
        if (image != null) {
            scaleImageToFit();
            if (dropZonePanel != null) {
                dropZonePanel.setVisible(false);
            }
        } else {
            this.scaledImage = null;
            this.originalImage = null;
            if (dropZonePanel != null) {
                dropZonePanel.setVisible(true);
            }
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
        
        int imgWidth = originalImage.getWidth();
        int imgHeight = originalImage.getHeight();

        double scale = Math.min(
            (double) panelWidth / imgWidth,
            (double) panelHeight / imgHeight
        );
        
        int scaledWidth = (int) (imgWidth * scale * zoomLevel);
        int scaledHeight = (int) (imgHeight * scale * zoomLevel);
        
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
        if (originalImage == null || selectionRect == null) return null;

        // Adjust for centering offset
        int adjX = selectionRect.x - imageXOffset;
        int adjY = selectionRect.y - imageYOffset;

        // Ensure selection is inside displayed image
        if (adjX < 0 || adjY < 0 || 
            adjX > scaledImage.getWidth() || adjY > scaledImage.getHeight()) {
            return null;
        }
     
        // Convert to ORIGINAL image coordinates
        int x = (int) (adjX * scaleX);
        int y = (int) (adjY * scaleY);
        int width = (int) (selectionRect.width * scaleX);
        int height = (int) (selectionRect.height * scaleY);

        x = Math.max(0, Math.min(x, originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, originalImage.getHeight() - 1));
        width = Math.min(width, originalImage.getWidth() - x);
        height = Math.min(height, originalImage.getHeight() - y);

        if (width <= 0 || height <= 0) return null;

        try {
            return originalImage.getSubimage(x, y, width, height);  // Use ORIGINAL!
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
    	  if (image == null) {
    	        return;
    	    }
    	  
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (originalImage != null) {
            int scaledWidth = (int) (originalImage.getWidth() / scaleX);
            int scaledHeight = (int) (originalImage.getHeight() / scaleY);
            int x = imageXOffset;
            int y = imageYOffset;

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
            
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

             FontMetrics fm = g2d.getFontMetrics();
             int textWidth = fm.stringWidth(dimensions);
             int textHeight = fm.getHeight();

             // position near the top right edge of the image area
             int boxPadding = 8;
             int boxWidth = textWidth + boxPadding * 2;
             int boxHeight = textHeight + 4;

             int boxX = getWidth() - boxWidth - 15;
             int boxY = 10;

             Color bgColor = Theme.getBgPrimary();
             Color textColor = Color.WHITE;

             g2d.setColor(bgColor);
             g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

             g2d.setColor(textColor);
             g2d.drawString(dimensions, boxX + boxPadding, boxY + fm.getAscent());

            }
        } else {
            g2d.setColor(Theme.getTextSecondary());
            String message = "No image loaded";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
    
    /**
     * Setup listener to handle panel resizing
     */
    private void setupResizeListener() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Recalculate scaling when panel is resized
                if (originalImage != null) {
                    scaleImageToFit();
                    repaint();
                }
            }
        });
    }
    
    public void zoomIn() {
        if (zoomLevel < ZOOM_MAX) {
            zoomLevel += ZOOM_STEP;
            if (originalImage != null) {
                scaleImageToFit();
                repaint();
            }
        }
    }

    public void zoomOut() {
        if (zoomLevel > ZOOM_MIN) {
            zoomLevel -= ZOOM_STEP;
            if (originalImage != null) {
                scaleImageToFit();
                repaint();
            }
        }
    }

    public void resetZoom() {
        zoomLevel = 1.0;
        if (originalImage != null) {
            scaleImageToFit();
            repaint();
        }
    }

    public double getZoomLevel() {
        return zoomLevel;
    }
}