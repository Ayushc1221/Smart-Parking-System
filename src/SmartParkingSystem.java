import org.opencv.core.Core;

public class SmartParkingSystem {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ParkingSystemGUI gui = new ParkingSystemGUI();
        gui.createAndShowGUI();

        FrameProcessor frameProcessor = new FrameProcessor(gui);
        new Thread(frameProcessor::processFrames).start();
    }
}
