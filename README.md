# Parralelize Image Dithering Algorithms
This repository is an attempt at parallelizing image dithering algorithims. 

# Introduction

This project seeks to find ways to speed-up the sequential nature of image dithering algorithms. For now, we will only tackle the [Floyd-Steinberg Image Dithering algorithm](https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering). This is repository written in Java, but will create a C/C++ version for performance.

Traditionally, the Floyd-Steinberg algorithm works like this:
  1. Create a color palette

     * You can either store it in an array of `Color` objects, or if you want to save memory, store it in an integer representation of the sRGB data.
       
       
       ```java
    
                     integer representation                    |                           color array
                                                               |
                int[] colorPalette = new int[]{                |                Color[] colorPalette = new Color[]{
                  -10912473,                                   |                  new Color(89, 125, 39),
                  -9594576,                                    |                  new Color(109, 153, 48),
                  -8408520,                                    |                  new Color(127, 178, 56),
                  -12362211,                                   |                  new Color(67, 94, 29)
                  // so on                                     |                // so on
                };                                             |                };
                                                               |
          ex.                                                  |
          -10912473 = 11111111 01011001 01111101 00100111      |
           (color)    (alpha)    (red)   (green)  (blue)       |
    
       ```
  2. Create a method that accepts an integer representation of sRGB data that utilizes the [nearest neighbor search](https://en.wikipedia.org/wiki/Nearest_neighbor_search) to acquire the nearest color to the created palette.
     
     * For my approach, I created a method that utilizes the [Euclidian Distance](https://en.wikipedia.org/wiki/Color_difference#sRGB) between the input and will iterate on each element of the color palette array

       ```java

       static int findNearestColor(int rgb) {
         double minDistance = Double.MAX_VALUE;
         int index = 0;
         for (int i = 0; i < colorPalette.length; i++) {
           double distance = Math.sqrt(
             Math.pow(((rgb >> 16) & 0xFF), 2) - (((colorPalette[i] >> 16) & 0xFF), 2) +
             Math.pow(((rgb >> 8) & 0xFF), 2) - (((colorPalette[i] >> 8) & 0xFF), 2) +
             Math.pow((rgb & 0xFF), 2) - ((colorPalette[i] & 0xFF), 2)
           };
           if (distance < minDistance) {
             minDistance = distance;
             index = i;
           }
         }
         return colorPalette[index]
       }

       ```

  3. Starting from the top, scan the image from left to right. Get the RGB value of the pixel by doing `getRGB(x, y)`, find its nearest color using the method that we've created on the previous number, and spread the error on each color channel to the neighboring pixels. Check also if the neighboring pixels are outside of the image coordinates.
     
     * _The Floyd-Steinberg Algorithm (or any error-diffused dithering) requires the quantization error to be placed somewhere for reference. If you are going to use this method and need to preserve the original image object, duplicate it first. The quantized error will be overwritten to the passed BufferedImage object._

       ```java

       static BufferedImage dither(BufferedImage image) {
         int width = image.getWidth();
         int height = image.getHeight();
         BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         for (int y = 0; y < height; y++) {
           for (int x = 0; x < width; x++) {
             int currentColor = new Color(image.getRGB(x, y)).getRGB();
             int nearestColor = findNearestColor(currentColor);
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

     ```
  Visualization of the algorithm
     
  ![demonstration](https://github.com/Zjaun/parralel-image-dithering-algorithm/assets/91415509/20c52092-7c01-44a7-b7db-8d1c8c060fba)
