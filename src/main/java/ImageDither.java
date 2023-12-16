import java.awt.image.BufferedImage;

public interface ImageDither {

    BufferedImage dither(int[] raster);
    BufferedImage dither(byte[] raster);

    default void distributeError(
            int[] raster,
            int width,
            int x,
            int y,
            int quantizationErrorR,
            int quantizationErrorG,
            int quantizationErrorB,
            double weight
    ) {
        int currentColor = raster[(y * width) + x];
        int currentColorR = (currentColor >> 16) & 0xFF;
        int currentColorG = (currentColor >> 8) & 0xFF;
        int currentColorB = currentColor & 0xFF;
        int newRed = Math.min(255, Math.max(0, (int) (currentColorR + weight * quantizationErrorR)));
        int newGreen = Math.min(255, Math.max(0, (int) (currentColorG + weight * quantizationErrorG)));
        int newBlue = Math.min(255, Math.max(0, (int) (currentColorB + weight * quantizationErrorB)));
        raster[(y * width) + x] = (255 << 24) + (newRed << 16) + (newGreen << 8) + newBlue;
    }

    default void distributeError(
            byte[] raster,
            int width,
            int x,
            int y,
            int quantizationErrorR,
            int quantizationErrorG,
            int quantizationErrorB,
            double weight
    ) {
        int currentColorR = raster[(y * width * 3) + (x + 2)] & 0xFF;
        int currentColorG = raster[(y * width * 3) + (x + 1)] & 0xFF;
        int currentColorB = raster[(y * width * 3) + x] & 0xFF;
        int newRed = Math.min(255, Math.max(0, (int) (currentColorR + weight * quantizationErrorR)));
        int newGreen = Math.min(255, Math.max(0, (int) (currentColorG + weight * quantizationErrorG)));
        int newBlue = Math.min(255, Math.max(0, (int) (currentColorB + weight * quantizationErrorB)));
        raster[(y * width * 3) + (x + 2)] = (byte) newRed;
        raster[(y * width * 3) + (x + 1)] = (byte) newGreen;
        raster[(y * width * 3) + x] = (byte) newBlue;
    }

    default int[] findAndSetNearestColors(
            int[] raster,
            int y,
            int width,
            int x,
            int[] lut,
            ColorPalette palette,
            int[] canvas
    ) {
        int currentColor = raster[(y * width) + x];
        return getQuantizedErrors(currentColor, y, width, x, lut, palette, canvas);
    }

    default int[] findAndSetNearestColors(
            byte[] raster,
            int y,
            int width,
            int x,
            int bufferOffset,
            int[] lut,
            ColorPalette palette,
            int[] canvas
    ) {
        int currentColorR = raster[(y * width * 3) + (bufferOffset + 2)] & 0xFF;
        int currentColorG = raster[(y * width * 3) + (bufferOffset + 1)] & 0xFF;
        int currentColorB = raster[(y * width * 3) + bufferOffset] & 0xFF;
        int currentColor = (255 << 24) + (currentColorR << 16) + (currentColorG << 8) + currentColorB;
        return getQuantizedErrors(currentColor, y, width, x, lut, palette, canvas);
    }

    private int[] getQuantizedErrors(
            int currentColor,
            int y,
            int width,
            int x,
            int[] lut,
            ColorPalette palette,
            int[] canvas
    ) {
        int currentColorR = (currentColor >> 16) & 0xFF;
        int currentColorG = (currentColor >> 8) & 0xFF;
        int currentColorB = currentColor & 0xFF;

        int nearestColor = lut == null ? palette.findNearestColorRGB(currentColor) : lut[(-currentColor) - 1];
        int nearestColorR = (nearestColor >> 16) & 0xFF;
        int nearestColorG = (nearestColor >> 8) & 0xFF;
        int nearestColorB = nearestColor & 0xFF;

        canvas[(y * width) + x] = nearestColor;

        int[] quantizedErrors = new int[3];
        quantizedErrors[0] = currentColorR - nearestColorR;
        quantizedErrors[1] = currentColorG - nearestColorG;
        quantizedErrors[2] = currentColorB - nearestColorB;
        return quantizedErrors;
    }

}


