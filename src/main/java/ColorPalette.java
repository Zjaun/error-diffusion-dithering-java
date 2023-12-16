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

    public int[] createLUT(boolean isCIELab) {
        LUT lut =  new LUT(this);
        lut.createLUT(isCIELab);
        return lut.getLUT();
    }

    public int[] createLUT(boolean multiThreadded, boolean isCIELab) {
        LUT lut =  new LUT(this);
        if (multiThreadded) {
            lut.enableMultiThreading();
            lut.createLUT(isCIELab);
        } else {
            lut.createLUT(isCIELab);
        }
        return lut.getLUT();
    }

    public int[] createLUT(int threadCount, boolean isCIELab) {
        LUT lut =  new LUT(this);
        lut.enableMultiThreading();
        lut.setThreadCount(threadCount);
        lut.createLUT(isCIELab);
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
        double[] cieLabCoordinates = rgbToCIELab(pixelRed, pixelGreen, pixelBlue);
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < palette.size(); i++) {
            double distance = ((cieLabCoordinates[0] - paletteCIEL[i]) * (cieLabCoordinates[0] - paletteCIEL[i])) +
                    ((cieLabCoordinates[1] - paletteCIEu[i]) * (cieLabCoordinates[1] - paletteCIEu[i])) +
                    ((cieLabCoordinates[2] - paletteCIEv[i]) * (cieLabCoordinates[2] - paletteCIEv[i]));
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
            double[] coordinates = rgbToCIELab(baseColorRed, baseColorGreen, baseColorBlue);
            paletteCIEL[index] = coordinates[0];
            paletteCIEu[index] = coordinates[1];
            paletteCIEv[index] = coordinates[2];
            index++;
        }
    }

    private double[] rgbToCIELab(int r, int g, int b) {

        // http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html
        // http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
        // https://en.wikipedia.org/wiki/Illuminant_D65#Definition

        double vR = r / 255.0;
        double vG = g / 255.0;
        double vB = b / 255.0;

        vR = vR <= 0.04045 ? vR / 12.92 : Math.pow((vR + 0.055) / 1.055, 2.4);
        vG = vG <= 0.04045 ? vG / 12.92 : Math.pow((vG + 0.055) / 1.055, 2.4);
        vB = vB <= 0.04045 ? vB / 12.92 : Math.pow((vB + 0.055) / 1.055, 2.4);

        double X = (vR * 0.4124564 + vG * 0.3575761 + vB * 0.1804375) * 100;
        double Y = (vR * 0.2126729 + vG * 0.7151522 + vB * 0.0721750) * 100;
        double Z = (vR * 0.0193339 + vG * 0.1191920 + vB * 0.9503041) * 100;

        double xr = X / 95.047;
        double yr = Y / 100.000;
        double zr = Z / 108.883;
        double e = 0.008856;
        double k = 903.3;
        double fx = xr > e ? Math.pow(xr, (double) 1/3) : ((k * xr) + 16) / 116;
        double fy = yr > e ? Math.pow(yr, (double) 1/3) : ((k * yr) + 16) / 116;
        double fz = zr > e ? Math.pow(zr, (double) 1/3) : ((k * zr) + 16) / 116;

        double[] coordinates = new double[3];
        coordinates[0] = 116 * fy - 16;
        coordinates[1] = 500 * (fx - fy);
        coordinates[2] = 500 * (fy - fz);

        return coordinates;

    }

    private double getDistanceCIE2000(double L2, double a2, double b2, double L1, double a1, double b1) {
        double L = (L1 + L2) / 2;
        double c1 = Math.sqrt(Math.pow(a1, 2) + Math.pow(b1, 2));
        double c2 = Math.sqrt(Math.pow(a2, 2) + Math.pow(b2, 2));
        double c = (c1 + c2) / 2;
        double G = 0.5 * (1 - (Math.sqrt(Math.pow(c, 7) / (Math.pow(c, 7) + Math.pow(25, 7)))));
        double A1 = (a1 * (1 + G));
        double A2 = (a2 * (1 + G));
        double C1 = Math.sqrt(Math.pow(A1, 2) + Math.pow(b1, 2));
        double C2 = Math.sqrt(Math.pow(A2, 2) + Math.pow(b2, 2));
        double C = (C1 + C2) / 2;
        double h1 = Math.atan2(b1, A1) >= 0 ? Math.atan2(b1, A1) : Math.atan2(b1, A1) + Math.toDegrees(360);
        double h2 = Math.atan2(b2, A2) >= 0 ? Math.atan2(b2, A2) : Math.atan2(b2, A2) + Math.toDegrees(360);
        double H = Math.abs(h1 - h2) > Math.toDegrees(180) ? (h1 + h2 + Math.toDegrees(360)) / 2 : (h1 + h2) / 2;
        double T = 1 - (0.17 * Math.cos(Math.toRadians(H - Math.toDegrees(30)))) + (0.24 * Math.cos(Math.toRadians(2 * H))) +
                (0.32 * Math.cos(Math.toRadians(3 * H + Math.toDegrees(6)))) -
                (0.20 * Math.cos(Math.toRadians(4 * H - Math.toDegrees(63))));
            double deltah;
        if (Math.abs(h2 - h1) <= Math.toDegrees(180)) {
            deltah = h2 - h1;
        } else if (Math.abs(h2 - h1) > Math.toDegrees(180) && h2 <= h1) {
            deltah = h2 - h1 + Math.toDegrees(360);
        } else {
            deltah = h2 - h1 - Math.toDegrees(360);
        }
        double deltaL = L2 - L1;
        double deltaC = C2 - C1;
        double deltaH = 2 * Math.sqrt(C1 * C2 * Math.sin(Math.toRadians(deltah / 2)));
        double SL = 1 + (0.015 * Math.pow((L - 50), 2) / Math.sqrt(20 + Math.pow((L - 50), 2)));
        double SC = 1 + 0.045 * C;
        double SH = 1 + 0.015 * C * T;
        double deltaTheta = 30 * Math.exp(-1 * Math.pow((H - Math.toDegrees(275)) / 25, 2));
        double RC = 2 * Math.sqrt(Math.pow(C, 7) / (Math.pow(C, 7) + Math.pow(25, 7)));
        double RT = -1 * RC * Math.sin(Math.toRadians(2 * deltaTheta));
        double KL = 1;
        double KC = 1;
        double KH = 1;
        return  Math.sqrt(Math.pow(deltaL / (KL * SL), 2) +
                Math.pow(deltaC / (KC * SC), 2) +
                Math.pow(deltaH / (KH * SH), 2) +
                RT * (deltaC / (KC * SC)) * (deltaH / (KH * SH)));
    }

    private boolean withinRGBRange(int number) {
        return number <= -1 && number >= -16777216;
    }

}