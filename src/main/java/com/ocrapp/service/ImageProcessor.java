package com.ocrapp.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

/**
 * Service class for image processing operations.
 * Handles loading, preprocessing, and optimizing images for OCR.
 */
public class ImageProcessor {
    
    private BufferedImage currentImage;
    private File currentImageFile;
    
    // Image processing parameters
    private static final int MAX_IMAGE_WIDTH = 3000;
    private static final int MAX_IMAGE_HEIGHT = 3000;
    private static final float CONTRAST_FACTOR = 1.2f;
    private static final float BRIGHTNESS_OFFSET = 10.0f;
    
    /**
     * Default constructor
     */
    public ImageProcessor() {
        this.currentImage = null;
        this.currentImageFile = null;
    }
    
    /**
     * Load an image from file
     * @param imageFile File to load
     * @return BufferedImage object, or null if loading fails
     */
    public BufferedImage loadImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            System.err.println("Invalid image file");
            return null;
        }
        
        try {
            BufferedImage image = ImageIO.read(imageFile);
            
            if (image == null) {
                System.err.println("Failed to read image file: " + imageFile.getName());
                return null;
            }
            
            this.currentImage = image;
            this.currentImageFile = imageFile;
            
            System.out.println("Image loaded successfully: " + imageFile.getName());
            System.out.println("Image dimensions: " + image.getWidth() + "x" + image.getHeight());
            
            return image;
            
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Load image from file path
     * @param imagePath Path to image file
     * @return BufferedImage object, or null if loading fails
     */
    public BufferedImage loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            System.err.println("Invalid image path");
            return null;
        }
        
        return loadImage(new File(imagePath));
    }
    
    /**
     * Preprocess image for OCR (grayscale, contrast, resize if needed)
     * @param image Image to preprocess
     * @return Preprocessed BufferedImage
     */
    public BufferedImage preprocessImage(BufferedImage image) {
        if (image == null) {
            System.err.println("Cannot preprocess null image");
            return null;
        }
        
        System.out.println("Starting image preprocessing...");
        
        // Step 1: Resize if too large
        BufferedImage processedImage = resizeIfNeeded(image);
        
        // Step 2: Invert color brightness if needed
        if (shouldInvert(processedImage)) {
            System.out.println("Image is  too dark - inverting for better OCR...");
            processedImage = invertImage(processedImage);
        } else {
            System.out.println("I see the light - skipping inversion.");
        }
        
        // Step 3: Convert to grayscale
        processedImage = convertToGrayscale(processedImage);
        
        // Step 4: Enhance contrast
        processedImage = enhanceContrast(processedImage);
        
        System.out.println("Image preprocessing completed");
        
        return processedImage;
    }
    
    /**
     * Check if theres a need to invert colors
     */
    public static boolean shouldInvert(BufferedImage image) {
        long totalBrightness = 0;
        int count = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                int brightness = (red + green + blue) / 3;
                totalBrightness += brightness;
                count++;
            }
        }

        double avgBrightness = (double) totalBrightness / count;
        System.out.println("Average brightness: " + avgBrightness);

        // Threshold of ~100 works well for most images
        return avgBrightness < 100;
    }
    
    /**
     * 
     * @param image
     * @return inverted image
     */
    public static BufferedImage invertImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage inverted = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = image.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int red = 255 - ((rgba >> 16) & 0xFF);
                int green = 255 - ((rgba >> 8) & 0xFF);
                int blue = 255 - (rgba & 0xFF);

                int invertedRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                inverted.setRGB(x, y, invertedRGB);
            }
        }

        return inverted;
    }
    /**
     * Convert image to grayscale
     * @param image Image to convert
     * @return Grayscale BufferedImage
     */
    public BufferedImage convertToGrayscale(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        // Create grayscale image
        BufferedImage grayImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        
        // Convert using ColorConvertOp
        ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                grayImage.getColorModel().getColorSpace(),
                null
        );
        
        op.filter(image, grayImage);
        
        System.out.println("Image converted to grayscale");
        
        return grayImage;
    }
    
    /**
     * Enhance image contrast for better OCR results
     * @param image Image to enhance
     * @return Enhanced BufferedImage
     */
    public BufferedImage enhanceContrast(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        // Create rescale operation for contrast enhancement
        RescaleOp rescaleOp = new RescaleOp(CONTRAST_FACTOR, BRIGHTNESS_OFFSET, null);
        
        BufferedImage enhancedImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType()
        );
        
        rescaleOp.filter(image, enhancedImage);
        
        System.out.println("Image contrast enhanced");
        
        return enhancedImage;
    }
    
    /**
     * Resize image if it exceeds maximum dimensions
     * @param image Image to resize
     * @return Resized BufferedImage (or original if within limits)
     */
    public BufferedImage resizeIfNeeded(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Check if resizing is needed
        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            System.out.println("Image size within limits, no resizing needed");
            return image;
        }
        
        // Calculate new dimensions while maintaining aspect ratio
        double scale = Math.min(
                (double) MAX_IMAGE_WIDTH / width,
                (double) MAX_IMAGE_HEIGHT / height
        );
        
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        
        System.out.println("Resizing image from " + width + "x" + height + 
                          " to " + newWidth + "x" + newHeight);
        
        return resizeImage(image, newWidth, newHeight);
    }
    
    /**
     * Resize image to specific dimensions
     * @param image Image to resize
     * @param targetWidth Target width
     * @param targetHeight Target height
     * @return Resized BufferedImage
     */
    public BufferedImage resizeImage(BufferedImage image, int targetWidth, int targetHeight) {
        if (image == null || targetWidth <= 0 || targetHeight <= 0) {
            return null;
        }
        
        Image scaledImage = image.getScaledInstance(
                targetWidth,
                targetHeight,
                Image.SCALE_SMOOTH
        );
        
        BufferedImage resizedImage = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * Validate image format and readability
     * @param imageFile File to validate
     * @return true if valid, false otherwise
     */
    public boolean validateImage(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            return false;
        }
        
        try {
            BufferedImage testImage = ImageIO.read(imageFile);
            return testImage != null;
        } catch (IOException e) {
            System.err.println("Image validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get dimensions of an image file without fully loading it
     * @param imageFile Image file
     * @return Dimension object with width and height
     */
    public Dimension getImageDimensions(File imageFile) {
        BufferedImage image = loadImage(imageFile);
        
        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        }
        
        return null;
    }
    
    /**
     * Create a copy of an image
     * @param image Image to copy
     * @return Copy of the BufferedImage
     */
    public BufferedImage copyImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        BufferedImage copy = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType()
        );
        
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return copy;
    }
    
    // Getters
    
    public BufferedImage getCurrentImage() {
        return currentImage;
    }
    
    public File getCurrentImageFile() {
        return currentImageFile;
    }
    
    /**
     * Clear current image from memory
     */
    public void clearCurrentImage() {
        this.currentImage = null;
        this.currentImageFile = null;
        System.out.println("Current image cleared from memory");
    }
}