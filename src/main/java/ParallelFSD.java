import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * This is not fast! Just a parallel implementation of the algorithm.
 */

public class ParallelFSD implements ImageDither {

    private final int WIDTH;
    private final int HEIGHT;
    private final int THREAD_COUNT;
    private final BufferedImage IMAGE;
    private final int[] CANVAS;
    private final int[] LUT;

    public ParallelFSD(int width, int height, int @NotNull [] lut, int threadCount) {
        if (lut.length < 16777216) throw new IllegalArgumentException("LUT have atleast 16777216 elements.");
        this.WIDTH = width;
        this.HEIGHT = height;
        this.IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.CANVAS = new int[width * height];
        this.LUT = lut;
        this.THREAD_COUNT = threadCount;
    }

    public BufferedImage dither(byte[] raster) {
        return null;
    }

    public BufferedImage dither(BufferedImage image) {
        return null;
    }

    public BufferedImage dither(int[] raster) {
        AtomicIntegerArray processedPixels = new AtomicIntegerArray(HEIGHT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int yStart = 0; yStart < THREAD_COUNT; yStart++) {
            int finalYStart = yStart;
            executorService.submit(() -> {
                for (int y = finalYStart; y < HEIGHT; y += THREAD_COUNT) {
                    for (int x = 0; x < WIDTH; x++)  {
                        if (y == 0 || processedPixels.get(y - 1) == WIDTH) {
                            doDither(raster, x, y, WIDTH, HEIGHT, LUT, CANVAS);
                        } else {
                            while(processedPixels.get(y - 1) - x < 3) {
                                // do nothing
                            }
                            doDither(raster, x, y, WIDTH, HEIGHT, LUT, CANVAS);
                        }
                        processedPixels.set(y, processedPixels.incrementAndGet(y));
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {

        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    private void doDither(int[] raster, int x, int y, int WIDTH, int HEIGHT, int[] LUT, int[] CANVAS) {
        int[] quantizedErrors = findAndSetNearestColors(raster, y, WIDTH, x, LUT, null, CANVAS);
        if (quantizedErrors[0] != 0 || quantizedErrors[1] != 0 || quantizedErrors[2] != 0) {
            if (x < WIDTH - 1) {
                distributeError(raster, WIDTH, x + 1, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 7.0 / 16.0);
            }
            if (y < HEIGHT - 1) {
                if (x > 0) {
                    distributeError(raster, WIDTH, x - 1, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 3.0 / 16.0);
                }
                distributeError(raster, WIDTH, x, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 5.0 / 16.0);
                if (x < WIDTH - 1) {
                    distributeError(raster, WIDTH, x + 1, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 16.0);
                }
            }
        }
    }

}
