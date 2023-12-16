import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {

   public static void main(String[] args) throws Exception {
//      ColorPalette palette = new ColorPalette(new Color[]{
//              new Color(0, 0, 0),
//              new Color(18, 18, 18),
//              new Color(28, 28, 28),
//              new Color(38, 38, 38),
//              new Color(47, 47, 47),
//              new Color(56, 56, 56),
//              new Color(64, 64, 64),
//              new Color(71, 71, 71),
//              new Color(126, 126, 126),
//              new Color(140, 140, 140),
//              new Color(155, 155, 155),
//              new Color(171, 171, 171),
//              new Color(189, 189, 189),
//              new Color(209, 209, 209),
//              new Color(231, 231, 231),
//              new Color(255, 255, 255),
//      });
       ColorPalette palette = new ColorPalette(MinecraftMapColor.baseColors);
       palette.convertPaletteToCIELab();
       FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
       grabber.setFormat("gdigrab");
//       VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(4);
//       grabber.setVideoCodec(18);
       grabber.setImageWidth(1280);
       grabber.setImageHeight(720);
       final int WIDTH = 1280;
       final int HEIGHT = 720;
       grabber.start();
       byte[] bytes = new byte[(WIDTH * HEIGHT) * 3];
       BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
       Graphics2D g2d = resized.createGraphics();
       ExecutorService executorService = Executors.newFixedThreadPool(2);
//       FloydSteinberg fsd = new FloydSteinberg(WIDTH, HEIGHT, palette.createLUT(true, false));
       Atkinson atk = new Atkinson(WIDTH, HEIGHT, palette.createLUT(true, false));
//       MinimizedAverageError jjn = new MinimizedAverageError(WIDTH, HEIGHT, palette.createLUT(true, false));
//       Stucki stk = new Stucki(WIDTH, HEIGHT, palette.createLUT(true, false));
//       Burkes brk = new Burkes(WIDTH, HEIGHT, palette.createLUT(true, false));
//       SierraLite sieLite = new SierraLite(WIDTH, HEIGHT, palette.createLUT(true, false));
       CanvasFrame window = new CanvasFrame("Floyd-Steinberg Dithering (" + palette.getNumOfColors() + " Colors)");
       Java2DFrameConverter converter = new Java2DFrameConverter();
       executorService.submit(() -> {
           while (true) {
//               BufferedImage image = converter.convert(grabber.grab());
//               g2d.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
               Frame frame = grabber.grab();
               Buffer[] buffer = frame.image;
               ByteBuffer byteBuffer = (ByteBuffer) buffer[0];
               byteBuffer.get(bytes);
               long start = System.currentTimeMillis();
//               window.showImage(fsd.dither(image.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH)));
               window.showImage(atk.dither(bytes));
               long end = System.currentTimeMillis();
               printRenderTime(start, end);
               byteBuffer.rewind();
           }
       });
//       CanvasFrame window1 = new CanvasFrame("Webcam (Full range, 10648000 Colors)");
//       executorService.submit(() -> {
//           while (true) {
//               window1.showImage(grabber.grab());
//           }
//       });
   }

   private static void printRenderTime(long start, long end) {
       System.out.println("Render time: " + (end - start) + "ms | FPS: " + ((int) (1000 / (end - start))));
   }

}
