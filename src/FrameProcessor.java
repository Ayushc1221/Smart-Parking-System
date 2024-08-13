import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class FrameProcessor {
    private VideoCapture camera;
    private ITesseract tesseract;
    private ParkingSystemGUI gui;
    private boolean vehicleDetected;
    private Connection connection;
    private boolean[] parkingSlots = new boolean[501];
    private int nextAvailableSlot = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public FrameProcessor(ParkingSystemGUI gui) {
        this.gui = gui;
        int correctIndex = 3;
        this.camera = new VideoCapture(correctIndex);
        if (!camera.isOpened()) {
            System.err.print("Error: Camera not opened");
        } else {
            System.out.print("Camera opened successfully");
        }
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("C:\\Users\\ayush\\Downloads\\tessdata-4.0.0\\tessdata-4.0.0");
        this.tesseract.setLanguage("eng");
        this.tesseract.setPageSegMode(7);
        this.tesseract.setOcrEngineMode(1);
        this.tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        this.vehicleDetected = false;
        this.connection = establishConnection();
        Arrays.fill(parkingSlots, false);
    }

    // Establish database connection
    public Connection establishConnection() {
        String url = "jdbc:mysql://localhost:3306/parking_system";
        String username = "root";
        String password = "11JAN2001";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return null;
        }
    }

    // Close database connection
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Get a free parking slot
    public int getFreeParkingSlot() {
        for (int i = 1; i <= 500; i++) {
            if (!parkingSlots[i]) {
                return i; // Return the first free parking slot
            }
        }
        return -1; // No free parking slots available
    }

    // Mark a parking slot as occupied
    private void markParkingSlotOccupied(int slot) {
        if (slot >= 1 && slot <= 500) {
            parkingSlots[slot] = true; // Mark the parking slot as occupied
        }
    }

    // Mark a parking slot as free
    private void markParkingSlotFree(int slot) {
        if (slot >= 1 && slot <= 500) {
            parkingSlots[slot] = false; // Mark the parking slot as free
        }
    }

    // Insert data into database
    public void entryVehicle() {
        if (connection != null) {
            Mat frame = new Mat();
            camera.read(frame);
            if (!frame.empty()) {
                updateCameraFeed(frame);
                String numberPlate = recognizeNumberPlate(frame);
                gui.updateStatus("scanned plate no. is "+ numberPlate);
                if (!numberPlate.isEmpty()) {
                    // Get a free parking slot
                    int Slotno = getFreeParkingSlot();
                    if (Slotno != -1) {
                        // Get the current date and time
                        Date currentDate = new Date();
                        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

                        // Prepare the SQL query
                        String sqlQuery = "INSERT INTO parking_records (plate_no, entry_date, entry_time, slot_no) VALUES (?, ?, ?, ?)";

                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                            // Set the values for the placeholders in the prepared statement
                            preparedStatement.setString(1, numberPlate);
                            preparedStatement.setString(2, dateFormat.format(currentDate));
                            preparedStatement.setString(3, timeFormat.format(currentDate));
                            preparedStatement.setInt(4, Slotno);

                            // Execute the SQL query
                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Vehicle entered successfully into slot " + Slotno);
                                gui.updateStatus("Vehicle entered successfully into slot " + Slotno);
                                markParkingSlotOccupied(Slotno); // Mark the parking slot as occupied
                            } else {
                                System.err.println("Failed to enter vehicle into database.");
                            }
                        } catch (SQLException e) {
                            System.err.println("Error executing SQL query: " + e.getMessage());
                        }
                    } else {
                        System.err.println("No free parking slots available.");
                    }
                }
            } else {
                System.err.println("Error: Frame is empty");
            }
        } else {
            System.err.println("Database connection is null.");
        }

    }
    // Method to manually enter a vehicle into the parking system
    public int manualEntry(String numberPlate) {
        //Scanner scanner = new Scanner(System.in);
       // System.out.print("Enter the plate number of the vehicle to enter: ");
       // String numberPlate = scanner.nextLine();

        // Get the current date and time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String entryDate = dateFormat.format(currentDate);
        String entryTime = timeFormat.format(currentDate);

        // Get a free parking slot
        int slotNumber = getFreeParkingSlot();
        if (slotNumber != -1) {
            // Insert data into database
            if (connection != null) {
                String sqlQuery = "INSERT INTO parking_records (plate_no, entry_date, entry_time, slot_no) VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                    preparedStatement.setString(1, numberPlate);
                    preparedStatement.setString(2, entryDate);
                    preparedStatement.setString(3, entryTime);
                    preparedStatement.setInt(4, slotNumber);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Vehicle entered successfully into slot " + slotNumber);
                        gui.updateStatus("Vehicle entered successfully into slot " + slotNumber);
                        markParkingSlotOccupied(slotNumber); // Mark the parking slot as occupied
                    } else {
                        System.err.println("Failed to enter vehicle into database.");
                    }
                } catch (SQLException e) {
                    System.err.println("Error executing SQL query: " + e.getMessage());
                }
            } else {
                System.err.println("Database connection is null.");
            }
        } else {
            System.err.println("No free parking slots available.");
        }
        return slotNumber;

    }




    // Exit a vehicle from the parking system
    public void exitVehicle() {
        if (connection != null) {
            Mat frame = new Mat();
            camera.read(frame);
            if (!frame.empty()) {
                updateCameraFeed(frame);
                String plateNumber = recognizeNumberPlate(frame);
                if (!plateNumber.isEmpty()) {
                    try {
                        String sqlQuery = "SELECT * FROM parking_records WHERE plate_no = ? AND exit_date IS NULL";

                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                            preparedStatement.setString(1, plateNumber);
                            ResultSet resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                int slotNo = resultSet.getInt("slot_no");

                                // Get the current date and time
                                Date currentDate = new Date();
                                Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

                                // Update the database with exit date and time
                                String updateQuery = "UPDATE parking_records SET exit_date = ?, exit_time = ? WHERE plate_no = ? AND exit_date IS NULL";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setString(1, dateFormat.format(currentDate));
                                    updateStatement.setString(2, timeFormat.format(currentDate));
                                    updateStatement.setString(3, plateNumber);
                                    int rowsAffected = updateStatement.executeUpdate();

                                    if (rowsAffected > 0) {
                                        System.out.println("Vehicle exited successfully from slot " + slotNo);
                                        markParkingSlotFree(slotNo); // Mark the parking slot as free
                                    } else {
                                        System.err.println("Failed to exit vehicle from slot " + slotNo);
                                    }
                                }
                            } else {
                                System.out.println("Vehicle with plate number " + plateNumber + " not found or already exited.");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error executing SQL query: " + e.getMessage());
                    }
                } else {
                    System.err.println("No number plate detected in the frame.");
                }
            } else {
                System.err.println("Error: Frame is empty");
            }
        } else {
            System.err.println("Database connection is null.");
        }
    }
    // Method to manually exit a vehicle from the parking system
    public void manualExit(String numberPlate) {
       // Scanner scanner = new Scanner(System.in);
        //System.out.print("Enter the plate number of the vehicle to exit: ");
       // String plateNumber = scanner.nextLine();

        // Get the current date and time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String exitDate = dateFormat.format(currentDate);
        String exitTime = timeFormat.format(currentDate);

        if (connection != null) {
            String sqlQuery = "SELECT * FROM parking_records WHERE plate_no = ? AND exit_date IS NULL";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, numberPlate);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int slotNo = resultSet.getInt("slot_no");

                    // Update the database with exit date and time
                    String updateQuery = "UPDATE parking_records SET exit_date = ?, exit_time = ? WHERE plate_no = ? AND exit_date IS NULL";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setString(1, exitDate);
                        updateStatement.setString(2, exitTime);
                        updateStatement.setString(3, numberPlate);
                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected > 0) {

                            System.out.println("Vehicle exited successfully from slot " + slotNo);
                            gui.updateStatus("Vehicle exited successfully from slot " + slotNo);
                            markParkingSlotFree(slotNo); // Mark the parking slot as free
                        } else {
                            System.err.println("Failed to exit vehicle from slot " + slotNo);
                        }
                    }
                } else {
                    System.out.println("Vehicle with plate number " + numberPlate+ " not found or already exited.");
                    gui.updateStatus("Vehicle with plate number " + numberPlate+ " not found or already exited.");
                }
            } catch (SQLException e) {
                System.err.println("Error executing SQL query: " + e.getMessage());
            }
        } else {
            System.err.println("Database connection is null.");
        }
    }
    // Method to enter the system into entry mode
    private void entryMode() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Entry Mode: Press 0 for automatic entry, 2 for manual entry, 3 to exit entry mode:");
            char input = scanner.nextLine().charAt(0);

            switch (input) {
                case '0':
                    entryVehicle();
                    break;
                case '2':
                    Scanner sc=new Scanner(System.in);
                    String numberPlate=sc.nextLine();
                    manualEntry(numberPlate);
                    break;
                case '3':
                    return; //entryMode();
                default:
                    System.err.println("Invalid input.");
            }
        }
    }

    // Method to enter the system into exit mode
    private void exitMode() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Exit Mode: Press 3 for automatic exit, 4 for manual exit, 9 to exit exit mode:");
            char input = scanner.nextLine().charAt(0);

            switch (input) {
                case '3':
                    exitVehicle();
                    break;
                case '4':
                    Scanner sc = new Scanner(System.in);
                    System.out.print("Enter the plate number of the vehicle to exit: ");
                    String numberPlate = sc.nextLine();

                    manualExit("");
                    break;
                case '9':
                    return; // Exit exit mode
                default:
                    System.err.println("Invalid input.");
            }
        }
    }


    public void processFrames() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Press 0 entry mode, 2 to exit mode");
            char input = scanner.nextLine().charAt(0);

            switch (input) {
                case '0':
                    entryMode(); // Call the entry method
                    break;
                case '2':
                    exitMode(); // Call the manual entr
                default:
                    System.err.println("Invalid input. Press 0 to enter a vehicle, 1 to manually enter a vehicle, 4 to exit a vehicle, 5 to manually exit a vehicle.");
                    continue; // Skip the rest of the loop and wait for the next input
            }

            Mat frame = new Mat();
            camera.read(frame);
            if (!frame.empty()) {
                updateCameraFeed(frame);
                if (!vehicleDetected) {
                    String numberPlate = recognizeNumberPlate(frame);
                    if (!numberPlate.isEmpty()) {
                        System.out.println("Detected Number Plate: " + numberPlate);
                        // Enter vehicle into the parking system
                        vehicleDetected = true; // Set flag to true indicating vehicle detection
                    }
                }
            } else {
                System.err.println("Error: Frame is empty");
            }
            // Reset vehicleDetected flag if needed
            if (vehicleDetected) {
                System.out.print("Enter '1' to indicate end of vehicle presence: ");
                String inputStr = scanner.nextLine();
                if ("1".equals(inputStr.trim())) {
                    vehicleDetected = false;
                }
            }
        }
    }
    private void updateCameraFeed(Mat frame) {
        BufferedImage image = MatToBufferedImage(frame);
        ImageIcon icon = new ImageIcon(image);
        gui.updateCameraFeed(icon);
    }

    private BufferedImage MatToBufferedImage(Mat frame) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray)) {
            return ImageIO.read(inputStream);
        } catch (Exception e) {
            System.err.println("Error converting Mat to BufferedImage: " + e.getMessage());
        }
        return null;
    }

    private String recognizeNumberPlate(Mat frame) {
        // Perform preprocessing steps if needed

        // Convert Mat to BufferedImage
        BufferedImage image = MatToBufferedImage(frame);

        try {
            // Recognize text using Tesseract
            String text = tesseract.doOCR(image);

            return text.trim(); // Trim any leading or trailing whitespace
        } catch (TesseractException e) {
            System.err.println("Error during OCR: " + e.getMessage());
            return ""; // Return empty string in case of error
        }
    }
}
