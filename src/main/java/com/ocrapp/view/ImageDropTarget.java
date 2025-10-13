package com.ocrapp.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drag and drop handler for image files
 */
public class ImageDropTarget extends DropTargetAdapter {
    
    private Consumer<File> onFileDropped;
    
    public ImageDropTarget(Consumer<File> onFileDropped) {
        this.onFileDropped = onFileDropped;
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            
            Transferable transferable = dtde.getTransferable();
            
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                
                if (!files.isEmpty()) {
                    File file = files.get(0); // first file only
                    
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                        fileName.endsWith(".png") || fileName.endsWith(".bmp") || 
                        fileName.endsWith(".tiff") || fileName.endsWith(".tif") ||
                        fileName.endsWith(".gif")) {
                        
                        if (onFileDropped != null) {
                            onFileDropped.accept(file);
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                }
            }
            
            dtde.dropComplete(false);
            
        } catch (Exception e) {
            System.err.println("Drop failed: " + e.getMessage());
            dtde.dropComplete(false);
        }
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // Visual feedback
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }
}