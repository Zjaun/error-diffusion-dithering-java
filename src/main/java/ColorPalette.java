import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

// alpha channel ignored when calculating distances, hence alpha channel is dropped in every insertion of colors
// https://reference.wolfram.com/language/ref/ColorDistance.html#:~:text=The%20alpha%20channel%20is%20not%20used%20when%20computing%20the%20distance%3A

public class ColorPalette {

    private final ArrayList<Integer> palette = new ArrayList<>();
    private float[] paletteLinearR = null;
    private float[] paletteLinearG = null;
    private float[] paletteLinearB = null;
    private boolean paletteChanged = false;

    public ColorPalette() {}

    public ColorPalette(Color[] colors) {
        if (colors != null) {
            Arrays.stream(colors)
                    .filter(Objects::nonNull)
                    .forEach(this::addColor);
        }
    }

    public ColorPalette(Color color) {
        addColor(color.getRGB());
    }

    public ColorPalette(int[] colors) {
        if (colors != null) {
            Arrays.stream(colors)
                    .filter(Objects::nonNull)
                    .forEach(this::addColor);
        }
    }

    public ColorPalette(int color) {
        addColor(color);
    }

    public void addColor(Color color) {
        if (color != null) {
            addColor(color.getRGB());
        }
    }

    public void addColor(Color[] colors) {
        if (colors != null) {
            Arrays.stream(colors)
                    .filter(Objects::nonNull)
                    .forEach(this::addColor);
        }
    }

    public void addColor(int color) {
        if (!palette.contains(color)) {
            palette.add(color);
            paletteChanged = true;
        }
    }

    public void addColor(int[] colors) {
        if (colors != null) {
            Arrays.stream(colors)
                    .filter(Objects::nonNull)
                    .forEach(this::addColor);
        }
    }

    public void removeColor(Color color) {
        if (color != null) {
            int intRepresentation = color.getRGB();
            removeColor(intRepresentation);
        }
    }

    public void removeColor(Color[] colors) {
        if (colors != null) {
            Arrays.stream(colors)
                    .filter(Objects::nonNull)
                    .forEach(this::removeColor);
        }
    }

    public void removeColor(int color) {
        if (palette.contains(color)) {
            palette.remove(color);
            paletteChanged = true;
        } else {
            throw new IllegalArgumentException(String.format("Color not present in the palette. (%d)", color));
        }
    }

    public int getLength() {
        return palette.size();
    }

    public int[] createLUT() {
        LUT lut = new LUT(this);
        lut.createLUT();
        return lut.getLUT();
    }

    public int[] createLUT(boolean multiThreadded) {
        LUT lut = new LUT(this);
        if (multiThreadded) {
            lut.enableMultiThreading();
            lut.createLUT();
        } else {
            lut.createLUT();
        }
        return lut.getLUT();
    }

    public int[] createLUT(int threadCount) {
        LUT lut = new LUT(this);
        lut.enableMultiThreading();
        lut.setThreadCount(threadCount);
        return lut.getLUT();
    }

    public int findNearestColorRGB(Color color) {
        if (color != null) {
            return findNearestColorRGB(color.getRGB());
        } else {
            return 0; // if color is null, return 0. but since 0 is black, we might just throw an exception
        }
    }

    public int findNearestColorRGB(int rgb) {
        if (palette.isEmpty()) throw new IllegalStateException("No colors are present in the palette. Add by calling addColor() methods");
        if (paletteLinearR == null || paletteChanged) {
            convertPaletteToLinear();
        }
        float scaledRed = ((rgb >> 16) & 0xFF) / 255f;
        float scaledGreen = ((rgb >> 8) & 0xFF) / 255f;
        float scaledBlue = (rgb & 0xFF) / 255f;
        float[] linear = srgbToLinear(scaledRed, scaledGreen, scaledBlue);
        return getNearestColor(linear);
    }

    private int getNearestColor(float[] linearCoordinates) {
        int i = 0;
        int nearestColor = 0;
        float minDistance = Float.MAX_VALUE;
        for (int color : palette) {
            float distance = ((linearCoordinates[0] - paletteLinearR[i]) * (linearCoordinates[0] - paletteLinearR[i])) +
                    ((linearCoordinates[1] - paletteLinearG[i]) * (linearCoordinates[1] - paletteLinearG[i])) +
                    ((linearCoordinates[2] - paletteLinearB[i]) * (linearCoordinates[2] - paletteLinearB[i]));
            if (distance < minDistance) {
                minDistance = distance;
                nearestColor = color;
            }
            i++;
        }
        return nearestColor;
    }

    // enables faster computation of distances
    private void convertPaletteToLinear() {
        int paletteSize = palette.size();
        paletteLinearR = new float[paletteSize];
        paletteLinearG = new float[paletteSize];
        paletteLinearB = new float[paletteSize];
        int index = 0;
        for (int color: palette) {
            float scaledRed = ((color >> 16) & 0xFF) / 255f;
            float scaledGreen = ((color >> 8) & 0xFF) / 255f;
            float scaledBlue = (color & 0xFF) / 255f;
            float[] coordinates = srgbToLinear(scaledRed, scaledGreen, scaledBlue);
            paletteLinearR[index] = coordinates[0];
            paletteLinearG[index] = coordinates[1];
            paletteLinearB[index] = coordinates[2];
            index++;
        }
        paletteChanged = false;
    }

    // https://surma.dev/things/ditherpunk/#:~:text=Gamma,following%20it%20myself
    // http://www.brucelindbloom.com/index.html?Eqn_RGB_to_XYZ.html (Inverse sRGB Companding)
    // https://en.wikipedia.org/wiki/SRGB#From_sRGB_to_CIE_XYZ
    private float[] srgbToLinear(float scaledRed, float scaledGreen, float scaledBlue) {
        return new float[]{
                scaledRed <= 0.04045 ? scaledRed / 12.92f : (float) Math.pow(((scaledRed + 0.055) / 1.055), 2.4),
                scaledGreen <= 0.04045 ? scaledGreen / 12.92f : (float) Math.pow(((scaledGreen + 0.055) / 1.055), 2.4),
                scaledBlue <= 0.04045 ? scaledBlue / 12.92f : (float) Math.pow(((scaledBlue + 0.055) / 1.055), 2.4)
        };
    }

}