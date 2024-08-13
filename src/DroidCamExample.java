import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class DroidCamExample {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);


        VideoCapture camera = new VideoCapture(3);

        if (!camera.isOpened()) {
            System.err.println("Error: Failed to open camera");
            return;
        }

        while (true) {
            Mat frame = new Mat();
            if (camera.read(frame)) {

            } else {
                System.err.println("Error: Failed to read frame");
                break;
            }
        }

        camera.release();
    }
}
