package com.ocrapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Visual drop zone panel displayed when no image is loaded
 */
public class DropZonePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public DropZonePanel() {
        setBackground(Theme.getBgSecondary());
        setOpaque(true);
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        
        // dashed outline
        drawDashedBorder(g2d, width, height);
        
        int iconY = centerY - 50;
        drawDownloadIcon(g2d, centerX, iconY);
        
        g2d.setColor(Theme.getTextPrimary());
        g2d.setFont(Theme.FONT_LARGE);
        String mainText = "Drag and Drop an image here";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(mainText);
        g2d.drawString(mainText, centerX - textWidth / 2, centerY + 20);
        
        g2d.setColor(Theme.getTextSecondary());
        String orText = "or";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(orText);
        g2d.drawString(orText, centerX - textWidth / 2, centerY + 50);
        
        String instructionText = "Click 'Open'";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(instructionText);
        g2d.drawString(instructionText, centerX - textWidth / 2, centerY + 75);
    }
    
    /**
     * Draw dashed border rectangle
     */
    private void drawDashedBorder(Graphics2D g2d, int width, int height) {
        int padding = 80;
        int rectX = padding;
        int rectY = padding;
        int rectWidth = width - (padding * 2);
        int rectHeight = height - (padding * 2);
        int cornerRadius = 12;
        
        float[] dashPattern = {10f, 10f};
        Stroke dashedStroke = new BasicStroke(2f, BasicStroke.CAP_BUTT, 
                                              BasicStroke.JOIN_MITER, 10f, 
                                              dashPattern, 0f);
        g2d.setStroke(dashedStroke);
        g2d.setColor(Theme.getBorder());
        
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
            rectX, rectY, rectWidth, rectHeight, cornerRadius, cornerRadius
        );
        g2d.draw(roundedRect);
    }
    
    /**
     * Draw download/arrow icon
     */
    private void drawDownloadIcon(Graphics2D g2d, int centerX, int centerY) {
        int iconSize = 60;
        int boxSize = 50;
        int arrowWidth = 3;
        
        g2d.setColor(Theme.getTextSecondary());
        g2d.setStroke(new BasicStroke(arrowWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int boxX = centerX - boxSize / 2;
        int boxY = centerY + 10;
        g2d.drawRoundRect(boxX, boxY, boxSize, boxSize / 2, 8, 8);
        
        int arrowX = centerX;
        int arrowTopY = centerY - 5 - iconSize / 2 ;
        int arrowBottomY = centerY - 5;
        g2d.drawLine(arrowX, arrowTopY, arrowX, arrowBottomY);
        
        // arrow head
        int arrowHeadSize = 12;
        int[] xPoints = {
            arrowX - arrowHeadSize, 
            arrowX, 
            arrowX + arrowHeadSize
        };
        int[] yPoints = {
            arrowBottomY - arrowHeadSize / 2, 
            arrowBottomY + arrowHeadSize / 2, 
            arrowBottomY - arrowHeadSize / 2
        };
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}