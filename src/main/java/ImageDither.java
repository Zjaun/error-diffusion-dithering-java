import java.awt.image.BufferedImage;

public interface ImageDither {

//    BufferedImage dither(int[] raster);
    BufferedImage dither(byte[] raster);
    private void distributeError(
            int[] raster,
            int width,
            int x,
            int y,
            int quantizationErrorR,
            int quantizationErrorG,
            int quantizationErrorB,
            double weight
    ) {

    };
    private void distributeError(
            byte[] raster,
            int width,
            int x,
            int y,
            int quantizationErrorR,
            int quantizationErrorG,
            int quantizationErrorB,
            double weight
    ) {

    };

}
