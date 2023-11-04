public class Processor implements Runnable {
    private final int elements;
    private final int process;

    public Processor(int elements, int process) {
        this.elements = elements;
        this.process = process;
    }

    @Override
    public void run() {
        int start = 0;
        if (process > 0) {
            for (int i = 0; i < process; i++) {
                start += ColorQuantizer.elements[i];
            }
        }
        for (int i = start; i < (start + elements); i++) {
            ColorQuantizer.colors[i] = ColorPalette.findNearestColor((i + 1) * -1);
        }
    }

}