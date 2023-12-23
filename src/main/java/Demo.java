import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Demo {

   public static void main(String[] args) throws Exception {
//      ColorPalette palette = new ColorPalette(new Color[]{
//              new Color(0, 0, 0),
////              new Color(18, 18, 18),
////              new Color(28, 28, 28),
////              new Color(38, 38, 38),
////              new Color(47, 47, 47),
////              new Color(56, 56, 56),
////              new Color(64, 64, 64),
////              new Color(71, 71, 71),
////              new Color(126, 126, 126),
////              new Color(140, 140, 140),
////              new Color(155, 155, 155),
////              new Color(171, 171, 171),
////              new Color(189, 189, 189),
////              new Color(209, 209, 209),
////              new Color(231, 231, 231),
//              new Color(255, 255, 255)
//      });
       ColorPalette palette = new ColorPalette(MinecraftMapColor.baseColors);
//       FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("video=screen-capture-recorder");
//       grabber.setFormat("dshow");
       FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
       grabber.setFormat("gdigrab");
//       VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(1);
//       grabber.setVideoCodec(4);
       final int WIDTH = 1920;
       final int HEIGHT = 1080;
       grabber.setImageWidth(WIDTH);
       grabber.setImageHeight(HEIGHT);
       byte[] bytes = new byte[(WIDTH * HEIGHT) * 3];
       BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
       Graphics2D g2d = resized.createGraphics();
       FloydSteinberg alg = new FloydSteinberg(WIDTH, HEIGHT, palette.createLUT(true));
       CanvasFrame window = new CanvasFrame("Floyd-Steinberg Dithering (" + palette.getLength() + " Colors)");
       Java2DFrameConverter converter = new Java2DFrameConverter();
       grabber.start();
       while (true) {
           long grabStart = System.currentTimeMillis();
           Frame frame = grabber.grab();
           BufferedImage image = converter.convert(frame);
           g2d.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
//           Buffer[] buffer = frame.image;
//           ByteBuffer byteBuffer = (ByteBuffer) buffer[0];
//           byteBuffer.get(bytes);
           long grabEnd = System.currentTimeMillis();
           window.showImage(alg.dither(resized.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH)));
//           window.showImage(alg.dither(bytes));
           long ditherEnd = System.currentTimeMillis();
           printMetrics(grabStart, grabEnd, ditherEnd);
//           byteBuffer.rewind();
       }
   }

   private static void printMetrics(long grabStart, long grabEnd, long ditherEnd) {
       System.out.println("Grab time: " + (grabEnd - grabStart) + "ms | Render time: " + (ditherEnd - grabEnd) + " ms | FPS: " + ((int) (1000 / (ditherEnd - grabStart))));
   }

}
