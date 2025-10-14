package com.ocrapp.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;

/**
 * Panel that displays line numbers for a JTextArea
 */
public class LineNumberPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private JTextArea textArea;
    private static final int MARGIN = 5;
    private static final Color LINE_NUMBER_COLOR = new Color(120, 120, 120);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    
    public LineNumberPanel(JTextArea textArea) {
        this.textArea = textArea;
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(0, MARGIN, 0, MARGIN));
        setPreferredSize(new Dimension(50, 0));
        
        // Update when text changes
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                repaint();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                repaint();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int fontHeight = fm.getHeight();
        
        Element root = textArea.getDocument().getDefaultRootElement();
        int lineCount = root.getElementCount();
        
        // width needed for line numbers
        int maxDigits = String.valueOf(lineCount).length();
        int width = fm.stringWidth("0") * maxDigits + 2 * MARGIN;
        
        if (getPreferredSize().width != width) {
            setPreferredSize(new Dimension(width, 0));
            revalidate();
        }
        
        g2d.setColor(LINE_NUMBER_COLOR);
        g2d.setFont(textArea.getFont());
        
        try {
            Rectangle viewRect = textArea.getVisibleRect();
            int startLine = textArea.viewToModel2D(new Point(0, viewRect.y));
            int endLine = textArea.viewToModel2D(new Point(0, viewRect.y + viewRect.height));
            
            startLine = root.getElementIndex(startLine);
            endLine = root.getElementIndex(endLine);
            
            for (int i = startLine; i <= endLine; i++) {
                Element line = root.getElement(i);
                int lineStart = line.getStartOffset();
                
                try {
                    Rectangle rect = textArea.modelToView2D(lineStart).getBounds();
                    int y = rect.y + fm.getAscent();
                    
                    String lineNumber = String.valueOf(i + 1);
                    int x = getWidth() - fm.stringWidth(lineNumber) - MARGIN;
                    
                    g2d.drawString(lineNumber, x, y);
                } catch (BadLocationException e) {
                    // Skip this line
                }
            }
        } catch (Exception e) {
            // Fallback: just draw all visible lines
            for (int i = 0; i < lineCount; i++) {
                String lineNumber = String.valueOf(i + 1);
                int x = getWidth() - fm.stringWidth(lineNumber) - MARGIN;
                int y = (i * fontHeight) + fm.getAscent();
                
                g2d.drawString(lineNumber, x, y);
            }
        }
    }
}