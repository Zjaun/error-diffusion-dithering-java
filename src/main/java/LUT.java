import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LUT {

    private boolean lutCreated = false;
    private boolean multiThread = false;
    private int threadCount;
    private final ColorPalette PALETTE;
    private final int POSSIBLE_COLORS = 16777216;
    private final int[] colors = new int[POSSIBLE_COLORS];

    public LUT(ColorPalette palette) {
        this.PALETTE = palette;
    }

    public void createLUT() {
        if (multiThread) {
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            int elementsPerThread = POSSIBLE_COLORS / threadCount;
            int remainingPixels = POSSIBLE_COLORS % threadCount;
            for (int i = 0; i < threadCount; i++) {
                int startElement = i * elementsPerThread;
                int endElement = startElement + elementsPerThread - 1;
                if (i == threadCount - 1) {
                    endElement += remainingPixels;
                }
                int finalEndElement = endElement;
                executorService.submit(() -> {
                    for (int k = startElement; k < finalEndElement + 1; k++) {
                        colors[k] = PALETTE.findNearestColorRGB((k + 1) * -1);
                    }
                });
            }
            executorService.shutdown();
            while (!executorService.isTerminated()) {

            }
        } else {
            for (int i = 0; i < colors.length; i++) {
                colors[i] = PALETTE.findNearestColorRGB((i + 1) * -1);
            }
        }
        lutCreated = true;
    }

    public int[] getLUT() {
        if (!isLUTCreated()) {
            throw new IllegalStateException("LUT is not created. Call createLUT() first.");
        }
        return colors;
    }

    public boolean isLUTCreated() {
        return lutCreated;
    }

    public void enableMultiThreading() {
        threadCount = (int) Math.floor(Runtime.getRuntime().availableProcessors() * 0.75);
        multiThread = true;
    }

    public void setThreadCount(int threads) {
        int maxThreadCount = Runtime.getRuntime().availableProcessors();
        if (!multiThread) {
            throw new IllegalStateException("Multi-threading is not enabled. Call enableMultiThreading() first.");
        }
        if (threadCount <= 0 || threadCount > maxThreadCount) {
            throw new IllegalArgumentException("Invalid thread count. Must be between 1 and " + maxThreadCount + ".");
        }
        threadCount = threads;
    }

}
