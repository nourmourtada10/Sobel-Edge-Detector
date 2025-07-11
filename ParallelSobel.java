import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO;

public class ParallelSobel {
    static final int THRESHOLD = 100;

    public static void main(String[] args) throws IOException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : Runtime.getRuntime().availableProcessors();

        BufferedImage input = ImageIO.read(new File("input.jpg"));
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        ForkJoinPool pool = new ForkJoinPool(threads);

        long start = System.nanoTime();
        pool.invoke(new SobelTask(input, output, 1, input.getHeight() - 1));
        long end = System.nanoTime();

        System.out.printf("Parallel Time: %.2f ms%n", (end - start) / 1e6);

        ImageIO.write(output, "jpg", new File("output_parallel_" + threads + ".jpg"));
    }

    static class SobelTask extends RecursiveAction {
        BufferedImage input, output;
        int startY, endY;

        SobelTask(BufferedImage input, BufferedImage output, int startY, int endY) {
            this.input = input;
            this.output = output;
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        protected void compute() {
            if (endY - startY <= THRESHOLD) {
                processRange(startY, endY);
            } else {
                int mid = (startY + endY) / 2;
                invokeAll(
                    new SobelTask(input, output, startY, mid),
                    new SobelTask(input, output, mid + 1, endY)
                );
            }
        }

        void processRange(int yStart, int yEnd) {
            int width = input.getWidth();
            int[] gx = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
            int[] gy = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

            for (int y = yStart; y < yEnd; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int pixelX = 0, pixelY = 0;
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            int rgb = input.getRGB(x + i, y + j);
                            int gray = (rgb >> 16) & 0xff;
                            int idx = (j + 1) * 3 + (i + 1);
                            pixelX += gray * gx[idx];
                            pixelY += gray * gy[idx];
                        }
                    }
                    int magnitude = Math.min(255, (int) Math.hypot(pixelX, pixelY));
                    int edgeColor = (magnitude << 16) | (magnitude << 8) | magnitude;
                    output.setRGB(x, y, edgeColor);
                }
            }
        }
    }
}

