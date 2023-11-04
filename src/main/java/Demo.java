import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;

public class Demo implements Runnable {

    private final String windowName;
    private final int mode;
    private final FrameGrabber grabber;
//    private BufferedImage image;


    public Demo(String windowName, int mode, FrameGrabber grabber) {
        this.windowName = windowName;
        this.mode = mode;
        this.grabber = grabber;
    }

    @Override
    public void run() {
        double framerate = grabber.getFrameRate();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        CanvasFrame window = new CanvasFrame(windowName);
        switch (mode) {
            case 0:
                while (true) {
                    try {
                        window.showImage(grabber.grab());
                        Thread.sleep(1000/(long) framerate);
                    } catch (Exception e) {

                    }
                }
            case 1:
                while (true) {
                    try {
                        window.showImage(ColorQuantizer.toRGB(converter.convert(grabber.grab())));
                        Thread.sleep(1000/(long) framerate);
                    } catch (Exception e) {

                    }
                }
            case 2:
                while (true) {
                    try {
                        window.showImage(ColorQuantizer.dither(converter.convert(grabber.grab())));
                        Thread.sleep(1000/(long) framerate);
                    } catch (Exception e) {

                    }
                }
            default:
                System.out.println("Mode is outside the range! " + mode);
                return;
        }
    }

}
