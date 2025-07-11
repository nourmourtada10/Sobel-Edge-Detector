import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SequentialSobel {
    public static void main(String[] args) throws IOException {
        BufferedImage input = ImageIO.read(new File("input.jpg"));
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        long start = System.nanoTime();
        applySobel(input, output);
        long end = System.nanoTime();

        System.out.printf("Sequential Time: %.2f ms%n", (end - start) / 1e6);

        ImageIO.write(output, "jpg", new File("output_sequential.jpg"));
    }

    public static void applySobel(BufferedImage input, BufferedImage output) {
        int width = input.getWidth();
        int height = input.getHeight();
        int[] gx = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        int[] gy = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

        for (int y = 1; y < height - 1; y++) {
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
