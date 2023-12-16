import java.awt.image.BufferedImage;

public class Atkinson implements ImageDither {

    private final int WIDTH;
    private final int HEIGHT;
    private final BufferedImage IMAGE;
    private final int[] CANVAS;
    private final int[] LUT;
    private final ColorPalette PALETTE;

    public Atkinson(int width, int height, int[] lut) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.CANVAS = new int[width * height];
        this.LUT = lut;
        this.PALETTE = null;
    }

    public Atkinson(int width, int height, ColorPalette palette) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.CANVAS = new int[width * height];
        this.PALETTE = palette;
        this.LUT = null;
    }

    public BufferedImage dither(int[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int[] quantizedErrors = findAndSetNearestColors(raster, y, WIDTH, x, LUT, PALETTE, CANVAS);
                if (x < WIDTH - 2) {
                    distributeError(raster, WIDTH, x + 2, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                }
                if (x < WIDTH - 1) {
                    distributeError(raster, WIDTH, x + 1, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                }
                if (y < HEIGHT - 1) {
                    if (x > 0) {
                        distributeError(raster, WIDTH, x - 1, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                    distributeError(raster, WIDTH, x, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    if (x < WIDTH - 1) {
                        distributeError(raster, WIDTH, x + 1, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                }
                if (y < HEIGHT - 2) {
                    distributeError(raster, WIDTH, x, y + 2, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                }
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage dither(byte[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            int bufferOffset = 0;
            for (int x = 0; x < WIDTH; x++) {
                int[] quantizedErrors = findAndSetNearestColors(raster, y, WIDTH, x, bufferOffset, LUT, PALETTE, CANVAS);
                if (x < WIDTH - 1) {
                    distributeError(raster, WIDTH, bufferOffset + 3, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    if (x < WIDTH - 2) {
                        distributeError(raster, WIDTH, bufferOffset + 6, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                }
                if (y < HEIGHT - 1) {
                    if (x > 0) {
                        distributeError(raster, WIDTH, bufferOffset - 3, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                    distributeError(raster, WIDTH, bufferOffset, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    if (x < WIDTH - 1) {
                        distributeError(raster, WIDTH, bufferOffset + 3, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                    if (y < HEIGHT - 2) {
                        distributeError(raster, WIDTH, bufferOffset, y + 2, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 8.0);
                    }
                }
                bufferOffset += 3;
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

}
