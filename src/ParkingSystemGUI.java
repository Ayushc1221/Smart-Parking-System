import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParkingSystemGUI {
    private JTextArea statusTextArea;
    private JLabel cameraFeedLabel;
    private FrameProcessor frameProcessor;

    private boolean isEntryMode = false;
    private boolean isExitMode = false;

    private JButton manualEntryButton;
    private JButton automaticEntryButton;
    private JButton manualExitButton;
    private JButton automaticExitButton;
    private JButton nextEntryButton;
    private JButton nextExitButton;
    private JButton exitEntryModeButton;
    private JButton exitExitModeButton;
    private ExecutorService executor;

    public ParkingSystemGUI() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Smart Parking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load background image

        ImageIcon backgroundImage = new ImageIcon("background.jpg"); // Change the file name to your image file
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(new BorderLayout());

        // Add components to the background label
        cameraFeedLabel = new JLabel();
        backgroundLabel.add(cameraFeedLabel, BorderLayout.WEST);

        statusTextArea = new JTextArea(10, 30);
        statusTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusTextArea);
        backgroundLabel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 2)); // Adjust the layout for two columns

        JButton entryModeButton = new JButton("Entry Mode");
        JButton exitModeButton = new JButton("Exit Mode");
        manualEntryButton = new JButton("Manual Entry");
        manualExitButton = new JButton("Manual Exit");
        automaticEntryButton = new JButton("Automatic Entry");
        automaticExitButton = new JButton("Automatic Exit");
        nextEntryButton = new JButton("Next Vehicle (Entry)");
        nextExitButton = new JButton("Next Vehicle (Exit)");
        exitEntryModeButton = new JButton("Exit Entry Mode");
        exitExitModeButton = new JButton("Exit Exit Mode");

        buttonPanel.add(entryModeButton);
        buttonPanel.add(exitModeButton);
        buttonPanel.add(manualEntryButton);
        buttonPanel.add(manualExitButton);
        buttonPanel.add(automaticEntryButton);
        buttonPanel.add(automaticExitButton);
        buttonPanel.add(nextEntryButton);
        buttonPanel.add(nextExitButton);
        buttonPanel.add(exitEntryModeButton);
        buttonPanel.add(exitExitModeButton);

        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(backgroundLabel);
        frame.pack();
        frame.setVisible(true);

        // Add action listeners to buttons
        entryModeButton.addActionListener(e -> setEntryMode(true));
        exitModeButton.addActionListener(e -> setExitMode(true));
        manualEntryButton.addActionListener(e -> manualEntry());
        automaticEntryButton.addActionListener(e -> automaticEntry());
        manualExitButton.addActionListener(e -> manualExit());
        automaticExitButton.addActionListener(e -> automaticExit());
        nextEntryButton.addActionListener(e -> nextEntry());
        nextExitButton.addActionListener(e -> nextExit());
        exitEntryModeButton.addActionListener(e -> exitEntryMode());
        exitExitModeButton.addActionListener(e -> exitExitMode());

        // Initialize FrameProcessor
        frameProcessor = new FrameProcessor(this);
        frameProcessor.processFrames();

       // startCameraFeed();

        // Initially disable all entry and exit buttons
        setEntryMode(false);
        setExitMode(false);
    }

    // Set entry mode
    private void setEntryMode(boolean enabled) {
        isEntryMode = enabled;
        isExitMode = false;
        updateButtonStates();
        updateStatus("Entry Mode: " + (enabled ? "Enabled" : "Disabled"));
    }

    // Set exit mode
    private void setExitMode(boolean enabled) {
        isEntryMode = false;
        isExitMode = enabled;
        updateButtonStates();
        updateStatus("Exit Mode: " + (enabled ? "Enabled" : "Disabled"));
    }

    // Exit entry mode
    private void exitEntryMode() {
        setEntryMode(false);
        updateStatus("Exited Entry Mode");
    }

    // Exit exit mode
    private void exitExitMode() {
        setExitMode(false);
        updateStatus("Exited Exit Mode");
    }

    // Update button states based on the current mode
    private void updateButtonStates() {
        manualEntryButton.setEnabled(isEntryMode);
        automaticEntryButton.setEnabled(isEntryMode);
        nextEntryButton.setEnabled(isEntryMode);
        manualExitButton.setEnabled(isExitMode);
        automaticExitButton.setEnabled(isExitMode);
        nextExitButton.setEnabled(isExitMode);
    }

    // Define button action methods
    private void manualEntry() {
        if (isEntryMode) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String numberPlate = JOptionPane.showInputDialog("Enter Vehicle Number Plate:");
                    if (numberPlate != null && !numberPlate.trim().isEmpty()) {
                        frameProcessor.manualEntry(numberPlate);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    updateStatus("Manual entry processed.");
                }
            };
            worker.execute();
        }
    }

    private void automaticEntry() {
        if (isEntryMode) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    frameProcessor.entryVehicle();
                    return null;
                }

                @Override
                protected void done() {
                    updateStatus("Automatic entry processed.");
                }
            };
            worker.execute();
        }
    }

    private void manualExit() {
        if (isExitMode) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String numberPlate = JOptionPane.showInputDialog("Enter Vehicle Number Plate:");
                    if (numberPlate != null && !numberPlate.trim().isEmpty()) {
                        frameProcessor.manualExit(numberPlate);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    updateStatus("Manual exit processed.");
                }
            };
            worker.execute();
        }
    }

    private void automaticExit() {
        if (isExitMode) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    frameProcessor.exitVehicle();
                    return null;
                }

                @Override
                protected void done() {
                    updateStatus("Automatic exit processed.");
                }
            };
            worker.execute();
        }
    }

    private void nextEntry() {
        if (isEntryMode) {
           // frameProcessor.resetVehicleDetection();
            updateStatus("Ready for next vehicle entry.");
        }
    }

    private void nextExit() {
        if (isExitMode) {
           // frameProcessor.resetVehicleDetection();
            updateStatus("Ready for next vehicle exit.");
        }
    }

    public void updateCameraFeed(ImageIcon icon) {
        SwingUtilities.invokeLater(() -> cameraFeedLabel.setIcon(icon));
    }

    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusTextArea.append(message + "\n");
            statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
        });
    }
}
