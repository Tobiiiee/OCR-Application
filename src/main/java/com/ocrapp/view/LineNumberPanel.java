package com.ocrapp.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.geom.Point2D;

public class LineNumberPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JTextArea textArea;
    private final FontMetrics fontMetrics;

    public LineNumberPanel(JTextArea textArea) {
        this.textArea = textArea;
        setFont(textArea.getFont());
        fontMetrics = getFontMetrics(getFont());
        setBackground(Theme.getBgTertiary());
        setForeground(new Color(180, 180, 180));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 60, 60)));

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { repaint(); }
            public void removeUpdate(DocumentEvent e) { repaint(); }
            public void changedUpdate(DocumentEvent e) { repaint(); }
        });

        textArea.addCaretListener(e -> repaint());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();
        int startOffset = textArea.viewToModel2D(new Point2D.Double(0, clip.y));
        int endOffset = textArea.viewToModel2D(new Point2D.Double(0, clip.y + clip.height));

        g.setColor(getForeground());
        g.setFont(getFont());

        try {
            int startLine = textArea.getLineOfOffset(startOffset);
            int endLine = textArea.getLineOfOffset(endOffset);

            for (int i = startLine; i <= endLine; i++) {
                int y = getLineY(i);
                String number = String.valueOf(i + 1);
                g.drawString(number, getWidth() - fontMetrics.stringWidth(number) - 5, y);
            }
        } catch (BadLocationException e) {
            // ignore
        }
    }

    private int getLineY(int line) throws BadLocationException {
        Rectangle r = textArea.modelToView2D(textArea.getLineStartOffset(line)).getBounds();
        return r.y + r.height - 4;
    }

    @Override
    public Dimension getPreferredSize() {
        int lineCount = textArea.getLineCount();
        int digits = Math.max(2, String.valueOf(lineCount).length());
        int width = fontMetrics.charWidth('0') * digits + 10;
        return new Dimension(width, textArea.getHeight());
    }
}
