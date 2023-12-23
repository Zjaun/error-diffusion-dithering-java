import java.awt.image.BufferedImage;

public class FloydSteinberg implements ImageDither {

    private final int WIDTH;
    private final int HEIGHT;
    private final BufferedImage IMAGE;
    private final int[] CANVAS;
    private final int[] LUT;
    private final ColorPalette PALETTE;

    public FloydSteinberg(int width, int height, int[] lut) {
        if (lut.length < 16777216) throw new IllegalArgumentException("LUT have atleast 16777216 elements.");
        this.WIDTH = width;
        this.HEIGHT = height;
        this.IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.CANVAS = new int[width * height];
        this.LUT = lut;
        this.PALETTE = null;
    }

    public FloydSteinberg(int width, int height, ColorPalette palette) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.CANVAS = new int[width * height];
        this.PALETTE = palette;
        this.LUT = null;
    }

    public BufferedImage dither(byte[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            int bufferOffset = 0;
            for (int x = 0; x < WIDTH; x++) {
                int[] quantizedErrors = findAndSetNearestColors(raster, y, WIDTH, x, bufferOffset, LUT, PALETTE, CANVAS);
                if (quantizedErrors[0] != 0 || quantizedErrors[1] != 0 || quantizedErrors[2] != 0) {
                    if (x < WIDTH - 1) {
                        distributeError(raster, WIDTH, bufferOffset + 3, y, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 7.0 / 16.0);
                    }
                    if (y < HEIGHT - 1) {
                        if (x > 0) {
                            distributeError(raster, WIDTH, bufferOffset - 3, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 3.0 / 16.0);
                        }
                        distributeError(raster, WIDTH, bufferOffset, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 5.0 / 16.0);
                        if (x < WIDTH - 1) {
                            distributeError(raster, WIDTH, bufferOffset + 3, y + 1, quantizedErrors[0], quantizedErrors[1], quantizedErrors[2], 1.0 / 16.0);
                        }
                    }
                }
                bufferOffset += 3;
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage dither(int[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int[] quantizedErrors = findAndSetNearestColors(raster, y, WIDTH, x, LUT, PALETTE, CANVAS);
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
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage dither(BufferedImage image) {
        int[] imageArray = image.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH);
        return dither(imageArray);
    }

}