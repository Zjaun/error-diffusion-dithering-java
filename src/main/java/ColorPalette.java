import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ColorPalette {

    private final ArrayList<Integer> palette = new ArrayList<>();
    private double[] paletteCIEL = null;
    private double[] paletteCIEu = null;
    private double[] paletteCIEv = null;

    public ColorPalette(Color[] colors) {
        if (colors == null) {
            throw new IllegalArgumentException("Color array is null.");
        }
        for (Color color : colors) {
            addColor(color);
        }
    }

    public ColorPalette(Color color) {
        addColor(color);
    }

    public ColorPalette(int[] colors) {
        if (colors == null) {
            throw new IllegalArgumentException("Color array is null.");
        }
        for (int color : colors) {
            addColor(color);
        }
    }

    public ColorPalette(int color) {
        addColor(color);
    }

    public void addColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color is null.");
        }
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        palette.add((255 << 24) + (r << 16) + (g << 8) + b);
    }

    public void addColor(int color) {
        if (withinRGBRange(color)) {
            palette.add(color);
        } else {
            throw new IllegalArgumentException("Color outside of sRGB range.");
        }
    }

    public void addColor(byte r, byte g, byte b) {
        palette.add((255 << 24) + (r << 16) + (g << 8) + b);
    }

    public void removeColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color is null.");
        }
        int intRepresentation = color.getRGB();
        if (palette.contains(intRepresentation)) {
            palette.remove(intRepresentation);
        } else {
            throw new IllegalArgumentException(String.format("Color %d is not in the palette.", intRepresentation));
        }
    }

    public void removeColor(int color) {
        if (palette.contains(color)) {
            palette.remove(color);
        } else {
            throw new IllegalArgumentException(String.format("Color %d is not in the palette.", color));
        }
    }

    public int getNumOfColors() {
        return palette.size();
    }

    public int[] createLUT() {
        LUT lut =  new LUT(this);
        lut.createLUT();
        return lut.getLUT();
    }

    public int[] createLUT(boolean multiThreadded) {
        LUT lut =  new LUT(this);
        if (multiThreadded) {
            lut.enableMultiThreading();
            lut.createLUT();
        } else {
            lut.createLUT();
        }
        return lut.getLUT();
    }

    public int[] createLUT(int threadCount) {
        LUT lut =  new LUT(this);
        lut.enableMultiThreading();
        lut.setThreadCount(threadCount);
        lut.createLUT();
        return lut.getLUT();
    }

    public int findNearestColorRGB(int rgb) {
        int pixelRed = (rgb >> 16) & 0xFF;
        int pixelGreen = (rgb >> 8) & 0xFF;
        int pixelBlue = rgb & 0xFF;
        int nearestColor = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int color : palette) {
            int baseColorRed = (color >> 16) & 0xFF;
            int baseColorGreen = (color >> 8) & 0xFF;
            int baseColorBlue = color & 0xFF;
            int distance = ((pixelRed - baseColorRed) * (pixelRed - baseColorRed)) +
                    ((pixelGreen - baseColorGreen) * (pixelGreen - baseColorGreen)) +
                    ((pixelBlue - baseColorBlue) * (pixelBlue - baseColorBlue));
            if (distance < minDistance) {
                minDistance = distance;
                nearestColor = color;
            }
        }
        return nearestColor;
    }

    public int findNearestColorCIELab(int rgb) {
        if (paletteCIEL == null || paletteCIEu == null || paletteCIEv == null) {
            throw new IllegalStateException("Call convertPaletteToCIELuv() first.");
        }
        int pixelRed = (rgb >> 16) & 0xFF;
        int pixelGreen = (rgb >> 8) & 0xFF;
        int pixelBlue = rgb & 0xFF;
        int index = 0;
        double[] coordinates = rgbToCIELuv(pixelRed, pixelGreen, pixelBlue);
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < palette.size(); i++) {
            double distance = ((coordinates[0] - paletteCIEL[i]) * (coordinates[0] - paletteCIEL[i])) +
                    ((coordinates[1] - paletteCIEu[i]) * (coordinates[1] - paletteCIEu[i])) +
                    ((coordinates[2] - paletteCIEv[i]) * (coordinates[2] - paletteCIEv[i]));
            if (distance < minDistance) {
                minDistance = distance;
                index = i;
            }
        }
        return palette.get(index);
    }
    public void convertPaletteToCIELab() {
        int paletteSize = palette.size();
        paletteCIEL = new double[paletteSize];
        paletteCIEu = new double[paletteSize];
        paletteCIEv = new double[paletteSize];
        int index = 0;
        for (int color: palette) {
            int baseColorRed = (color >> 16) & 0xFF;
            int baseColorGreen = (color >> 8) & 0xFF;
            int baseColorBlue = color & 0xFF;
            double[] coordinates = rgbToCIELuv(baseColorRed, baseColorGreen, baseColorBlue);
            paletteCIEL[index] = coordinates[0];
            paletteCIEu[index] = coordinates[1];
            paletteCIEv[index] = coordinates[2];
            index++;
        }
    }

    private double[] rgbToCIELuv(int r, int g, int b) {

        // http://www.brucelindbloom.com/index.html?Eqn_RGB_to_XYZ.html

        double rN = r / 255.0;
        double gN = g / 255.0;
        double bN = b / 255.0;

        rN = rN <= 0.04045 ? rN / 12.92 : Math.pow((rN + 0.055) / 1.055, 2.4);
        gN = gN <= 0.04045 ? gN / 12.92 : Math.pow((gN + 0.055) / 1.055, 2.4);
        bN = bN <= 0.04045 ? bN / 12.92 : Math.pow((bN + 0.055) / 1.055, 2.4);

        double xN = rN * 0.4124564 + gN * 0.3575761 + bN * 0.1804375;
        double yN = rN * 0.2126729 + gN * 0.7151522 + bN * 0.0721750;
        double zN = rN * 0.0193339 + gN * 0.1191920 + bN * 0.9503041;

        return new double[3];

    }

    private boolean withinRGBRange(int number) {
        return number <= -1 && number >= -16777216;
    }

}