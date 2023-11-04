import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

//import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.Arrays;
import java.util.Optional;

public class ColorQuantizer {

    public static int[] colors = new int[16777216];
    public static int[] elements;

    static void computeValues(boolean isMultiThreaded, Optional<Integer> coreCount) throws Exception {
        int threadNumber = isMultiThreaded && coreCount.isPresent() && coreCount.get() > 0 ? coreCount.get() : isMultiThreaded ? (int) Math.floor(Runtime.getRuntime().availableProcessors() * 0.75) : 1;
        elements = new int[threadNumber];
        Thread[] threads = new Thread[threadNumber];
        if (16777216 % threadNumber != 0) {
            elements[0] += 1;
        }
        for (int i = 0; i < threadNumber; i++) {
            elements[i] += (int) Math.floor(16777216.0 / threadNumber);
            Processor processor = new Processor(elements[i], i);
            threads[i] = new Thread(processor);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    static BufferedImage toRGB(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        int height = image.getHeight();
        int width = image.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int current = image.getRGB(x, y);
                newImage.setRGB(x, y, colors[(current * -1) - 1]);
            }
        }
        return newImage;
    }

    static BufferedImage dither(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentColor = new Color(image.getRGB(x, y)).getRGB();
                int nearestColor = colors[(currentColor * -1) - 1];
                newImage.setRGB(x, y, nearestColor);
                int quantErrorR = ((currentColor >> 16) & 0xFF) - ((nearestColor >> 16) & 0xFF);
                int quantErrorG = ((currentColor >> 8) & 0xFF) - ((nearestColor >> 8) & 0xFF);
                int quantErrorB = (currentColor & 0xFF) - (nearestColor & 0xFF);
                if (x < width - 1) {
                    distributeError(image, x + 1, y, quantErrorR, quantErrorG, quantErrorB, 7.0 / 16.0);
                }
                if (y < height - 1) {
                    if (x > 0) {
                        distributeError(image, x - 1, y + 1, quantErrorR, quantErrorG, quantErrorB, 3.0 / 16.0);
                    }
                    distributeError(image, x, y + 1, quantErrorR, quantErrorG, quantErrorB, 5.0 / 16.0);
                    if (x < width - 1) {
                        distributeError(image, x + 1, y + 1, quantErrorR, quantErrorG, quantErrorB, 1.0 / 16.0);
                    }
                }
            }
        }
        return newImage;
    }

    static void distributeError(BufferedImage image, int x, int y, int quantizationErrorR, int quantizationErrorG, int quantizationErrorB, double weight) {
        int pixel = new Color(image.getRGB(x, y)).getRGB();
        int newRed = Math.min(255, Math.max(0, (int) (((pixel >> 16) & 0xFF) + weight * quantizationErrorR)));
        int newGreen = Math.min(255, Math.max(0, (int) (((pixel >> 8) & 0xFF) + weight * quantizationErrorG)));
        int newBlue = Math.min(255, Math.max(0, (int) ((pixel & 0xFF) + weight * quantizationErrorB)));
        int newColor = (255 << 24) + (newRed << 16) + (newGreen << 8) + newBlue;
        image.setRGB(x, y, newColor);
    }

    public static void main(String[] args) throws Exception {
        computeValues(true, Optional.empty());
        Java2DFrameConverter converter = new Java2DFrameConverter();
        FrameGrabber grabber = new CustomVideoInputFrameGrabber(0);
        grabber.setFormat("MJPG");
        grabber.setImageWidth(1280);
        grabber.setImageHeight(720);
        grabber.setFrameRate(30.0);
        grabber.start();
        Demo webcam = new Demo("Webcam", 0, grabber);
        Demo directRGB = new Demo("Direct Palette Conversion (1 Thread)", 1, grabber);
        Demo directDither = new Demo("Dithered-FSD (1 Thread)", 2, grabber);
        Thread webcamThread = new Thread(webcam);
        Thread directRGBThread = new Thread(directRGB);
        Thread directDitherThread = new Thread(directDither);
//        webcamThread.start();
//        directRGBThread.start();
        directDitherThread.start();
    }

}