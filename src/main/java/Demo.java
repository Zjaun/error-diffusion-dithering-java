import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
       FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
       grabber.setFormat("gdigrab");
//       VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(4);
//       grabber.setVideoCodec(18);
       final int width = 1280;
       final int height = 720;
       grabber.setImageWidth(width);
       grabber.setImageHeight(height);
       grabber.start();
       byte[] bytes = new byte[(height * width) * 3];
       ExecutorService executorService = Executors.newFixedThreadPool(2);
       FloydSteinberg quantizer = new FloydSteinberg(width, height, palette.createLUT(true));
       CanvasFrame window = new CanvasFrame("Floyd-Steinberg Dithering (" + palette.getNumOfColors() + " Colors)");
       executorService.submit(() -> {
           while (true) {
               Frame frame = grabber.grab();
               Buffer[] buffer = frame.image;
               ByteBuffer byteBuffer = (ByteBuffer) buffer[0];
               byteBuffer.get(bytes);
               window.showImage(quantizer.toRGB(bytes));
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

}
