import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class CameraTest {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        for (int index = 0; index < 5; index++) { // Test indices 0 to 4
            VideoCapture camera = new VideoCapture(3);
            if (camera.isOpened()) {
                System.out.print("Camera opened successfully at index: " + 3);
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    System.out.print("Frame captured successfully from camera index: " + 3);
                } else {
                    System.err.print("Error: Frame not captured from camera index: " + 3);
                }
                camera.release();
                break;
            } else {
                System.err.print("Error: Camera not opened at index: " + 3);
            }
        }
    }
}
