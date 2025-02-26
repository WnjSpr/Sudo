import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class SudokuGame extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;

    private JTextField[][] grid;
    private Timer timer;
    private TimerTask timerTask;
    private int seconds = 0;
    private JLabel timerLabel;
    private boolean gameRunning = false;
    private boolean isDarkMode = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGame::new);
    }

    public SudokuGame() {
        setTitle("Sudoku Game");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Grid
        grid = new JTextField[SIZE][SIZE];
        JPanel gridPanel = createSudokuGrid();

        // Timer label
        timerLabel = new JLabel("Time: 0:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Control buttons
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton modeButton = new JButton("Switch to Dark Mode");

        startButton.addActionListener(e -> startGame());
        stopButton.addActionListener(e -> stopGame());
        modeButton.addActionListener(e -> toggleMode(modeButton, gridPanel));

        // Panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(modeButton);

        // Top panel (timer + controls)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(timerLabel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        applyLightMode(gridPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createSudokuGrid() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, 20));
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                // Highlight subgrid borders (3x3 bold lines)
                if (row % SUBGRID_SIZE == 0 && row != 0) {
                    cell.setBorder(BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK));
                }
                if (col % SUBGRID_SIZE == 0 && col != 0) {
                    cell.setBorder(BorderFactory.createMatteBorder(1, 3, 1, 1, Color.BLACK));
                }

                // Ensure only 1-9 is entered
                ((AbstractDocument) cell.getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                        if (text.matches("[1-9]") && fb.getDocument().getLength() + text.length() <= 1) {
                            super.replace(fb, offset, length, text, attrs);
                            validateInput(row, col, text);
                        }
                    }
                });

                grid[row][col] = cell;
                gridPanel.add(cell);
            }
        }
        return gridPanel;
    }

    private void validateInput(int row, int col, String input) {
        // Check row
        for (int c = 0; c < SIZE; c++) {
            if (c != col && input.equals(grid[row][c].getText())) {
                showError("Angka ini sudah ada di baris ini.");
                grid[row][col].setText("");
                return;
            }
        }

        // Check column
        for (int r = 0; r < SIZE; r++) {
            if (r != row && input.equals(grid[r][col].getText())) {
                showError("Angka ini sudah ada di kolom ini.");
                grid[row][col].setText("");
                return;
            }
        }

        // Check 3x3 subgrid
        int startRow = (row / SUBGRID_SIZE) * SUBGRID_SIZE;
        int startCol = (col / SUBGRID_SIZE) * SUBGRID_SIZE;
        for (int r = startRow; r < startRow + SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SUBGRID_SIZE; c++) {
                if ((r != row || c != col) && input.equals(grid[r][c].getText())) {
                    showError("Angka ini sudah ada di kotak 3x3 ini.");
                    grid[row][col].setText("");
                    return;
                }
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    private void startGame() {
        if (gameRunning) return;
        gameRunning = true;

        if (timer == null) {
            timer = new Timer();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int sec = seconds % 60;
                timerLabel.setText(String.format("Time: %d:%02d", minutes, sec));
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void stopGame() {
        if (!gameRunning) return;
        gameRunning = false;

        if (timerTask != null) {
            timerTask.cancel();
        }

        int option = JOptionPane.showOptionDialog(this, "Game paused. Continue or quit?",
                "Game Paused", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"Continue", "Quit"}, "Continue");

        if (option == JOptionPane.NO_OPTION) {
            System.exit(0);
        } else {
            startGame();
        }
    }

    private void toggleMode(JButton modeButton, JPanel gridPanel) {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            applyDarkMode(gridPanel);
            modeButton.setText("Switch to Light Mode");
        } else {
            applyLightMode(gridPanel);
            modeButton.setText("Switch to Dark Mode");
        }
    }

    private void applyLightMode(JPanel gridPanel) {
        getContentPane().setBackground(Color.WHITE);
        timerLabel.setForeground(Color.BLACK);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setBackground(Color.WHITE);
                grid[row][col].setForeground(Color.BLACK);
                grid[row][col].setCaretColor(Color.BLACK);
            }
        }
        gridPanel.setBackground(Color.WHITE);
    }

    private void applyDarkMode(JPanel gridPanel) {
        getContentPane().setBackground(Color.DARK_GRAY);
        timerLabel.setForeground(Color.WHITE);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setBackground(Color.BLACK);
                grid[row][col].setForeground(Color.WHITE);
                grid[row][col].setCaretColor(Color.WHITE);
            }
        }
        gridPanel.setBackground(Color.BLACK);
    }
}
