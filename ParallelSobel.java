import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelSobel {
    
    // Sobel operator kernels
    private static final int[][] SOBEL_X = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static final int[][] SOBEL_Y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
    
    public static BufferedImage applySobel(BufferedImage inputImage, int numThreads) throws InterruptedException {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Split work into horizontal strips
        int stripHeight = height / numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            final int startY = i * stripHeight;
            final int endY = (i == numThreads - 1) ? height - 1 : (i + 1) * stripHeight;
            
            executor.execute(() -> {
                for (int y = Math.max(1, startY); y < Math.min(height - 1, endY); y++) {
                    for (int x = 1; x < width - 1; x++) {
                        
                        // Initialize gradients
                        int gx = 0;
                        int gy = 0;
                        
                        // Apply Sobel kernels
                        for (int ky = -1; ky <= 1; ky++) {
                            for (int kx = -1; kx <= 1; kx++) {
                                int pixel = inputImage.getRGB(x + kx, y + ky) & 0xFF;
                                gx += pixel * SOBEL_X[ky + 1][kx + 1];
                                gy += pixel * SOBEL_Y[ky + 1][kx + 1];
                            }
                        }
                        
                        // Calculate magnitude and clamp to 0-255
                        int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                        magnitude = Math.min(255, Math.max(0, magnitude));
                        
                        // Set output pixel
                        int edgePixel = (0xFF << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                        outputImage.setRGB(x, y, edgePixel);
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        
        return outputImage;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: ParallelSobel <inputImage> <outputImage> <numThreads>");
            return;
        }
        
        int numThreads = Integer.parseInt(args[2]);
        BufferedImage inputImage = ImageUtils.loadImage(args[0]);
        BufferedImage grayscaleImage = ImageUtils.convertToGrayscale(inputImage);
        BufferedImage edgeImage = applySobel(grayscaleImage, numThreads);
        ImageUtils.saveImage(edgeImage, args[1]);
    }
}