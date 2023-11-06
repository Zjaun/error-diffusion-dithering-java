import java.awt.*;
import java.awt.image.BufferedImage;

public class DitherProcessor implements Runnable {

    final int imageWidth;
    final int imageHeight;
    final int height;
    final int threadCount;
    final Graphics2D g2d;
    final BufferedImage image;

    public DitherProcessor(BufferedImage image, Graphics2D g2d, int imageWidth, int imageHeight, int height, int threadCount) {
        this.image = image;
        this.g2d = g2d;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.height = height;
        this.threadCount = threadCount;
    }

    @Override
    public void run() {
        g2d.drawImage(ColorQuantizer.dither(image.getSubimage(0, height, imageWidth, (imageHeight / threadCount))), 0, height, null);
    }

}
