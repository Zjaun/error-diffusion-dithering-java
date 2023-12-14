import java.awt.image.BufferedImage;

public class FloydSteinberg implements ImageDither {

    private final int WIDTH;
    private final int HEIGHT;
    private final BufferedImage IMAGE;
    private final int[] CANVAS;
    private final int[] LUT;
    private final ColorPalette PALETTE;

    public FloydSteinberg(int width, int height, int[] lut) {
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

    public BufferedImage dither(byte[] bytes) {
        for (int y = 0; y < HEIGHT; y++) {
            int bufferIndex = 0;
            for (int x = 0; x < WIDTH; x++) {

                int currentColorR = bytes[(y * WIDTH * 3) + (bufferIndex + 2)] & 0xFF;
                int currentColorG = bytes[(y * WIDTH * 3) + (bufferIndex + 1)] & 0xFF;
                int currentColorB = bytes[(y * WIDTH * 3) + bufferIndex] & 0xFF;
                int currentColor = (255 << 24) + (currentColorR << 16) + (currentColorG << 8) + currentColorB;

                int nearestColor;
                if (LUT == null) {
                    assert PALETTE != null;
                    nearestColor = PALETTE.findNearestColorRGB(currentColor);
                } else {
                    nearestColor = LUT[currentColor];
                }
                int nearestColorR = (nearestColor >> 16) & 0xFF;
                int nearestColorG = (nearestColor >> 8) & 0xFF;
                int nearestColorB = nearestColor & 0xFF;

                CANVAS[(y * WIDTH) + x] = nearestColor;

                int quantErrorR = currentColorR - nearestColorR;
                int quantErrorG = currentColorG - nearestColorG;
                int quantErrorB = currentColorB - nearestColorB;

                if (x < WIDTH - 1) {
                    distributeError(bytes, WIDTH, bufferIndex + 3, y, quantErrorR, quantErrorG, quantErrorB, 7.0 / 16.0);
                }
                if (y < HEIGHT - 1) {
                    if (x > 0) {
                        distributeError(bytes, WIDTH, bufferIndex - 3, y + 1, quantErrorR, quantErrorG, quantErrorB, 3.0 / 16.0);
                    }
                    distributeError(bytes, WIDTH, bufferIndex, y + 1, quantErrorR, quantErrorG, quantErrorB, 5.0 / 16.0);
                    if (x < WIDTH - 1) {
                        distributeError(bytes, WIDTH, bufferIndex + 3, y + 1, quantErrorR, quantErrorG, quantErrorB, 1.0 / 16.0);
                    }
                }

                bufferIndex += 3;
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage toRGB(byte[] bytes) {
        for (int y = 0; y < HEIGHT; y++) {
            int bufferIndex = 0;
            for (int x = 0; x < WIDTH; x++) {

                int currentColorR = bytes[(y * WIDTH * 3) + (bufferIndex + 2)] & 0xFF;
                int currentColorG = bytes[(y * WIDTH * 3) + (bufferIndex + 1)] & 0xFF;
                int currentColorB = bytes[(y * WIDTH * 3) + bufferIndex] & 0xFF;
                int currentColor = (255 << 24) + (currentColorR << 16) + (currentColorG << 8) + currentColorB;

                int nearestColor = LUT == null ? PALETTE.findNearestColorRGB(currentColor) : LUT[currentColor];
                CANVAS[(y * WIDTH) + x] = nearestColor;

                bufferIndex += 3;

            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage toRGB(int[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                CANVAS[(y * WIDTH) + x] = PALETTE.findNearestColorRGB(raster[(y * WIDTH) + x]);
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    public BufferedImage toCIELab(int[] raster) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                CANVAS[(y * WIDTH) + x] = PALETTE.findNearestColorCIELab(raster[(y * WIDTH) + x]);
            }
        }
        IMAGE.setRGB(0, 0, WIDTH, HEIGHT, CANVAS, 0, WIDTH);
        return IMAGE;
    }

    private void distributeError(
            byte[] errorBuffer,
            int width,
            int x,
            int y,
            int quantizationErrorR,
            int quantizationErrorG,
            int quantizationErrorB,
            double weight
    ) {
        int currentColorR = errorBuffer[(y * width * 3) + (x + 2)] & 0xFF;
        int currentColorG = errorBuffer[(y * width * 3) + (x + 1)] & 0xFF;
        int currentColorB = errorBuffer[(y * width * 3) + x] & 0xFF;
        int newRed = Math.min(255, Math.max(0, (int) (currentColorR + weight * quantizationErrorR)));
        int newGreen = Math.min(255, Math.max(0, (int) (currentColorG + weight * quantizationErrorG)));
        int newBlue = Math.min(255, Math.max(0, (int) (currentColorB + weight * quantizationErrorB)));
        errorBuffer[(y * width * 3) + (x + 2)] = (byte) newRed;
        errorBuffer[(y * width * 3) + (x + 1)] = (byte) newGreen;
        errorBuffer[(y * width * 3) + x] = (byte) newBlue;
    }



}