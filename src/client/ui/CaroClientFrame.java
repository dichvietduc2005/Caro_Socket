package client.ui;

import client.controller.ClientController;
import client.model.GameState;

import javax.swing.*;
import java.awt.*;

public class CaroClientFrame extends JFrame {

    // === Color Constants ===
    private static final Color BG_DARK = new Color(0x12, 0x16, 0x21);
    private static final Color BG_PANEL = new Color(0x1D, 0x24, 0x33);
    private static final Color BG_BOARD = new Color(0x18, 0x1F, 0x2E);
    private static final Color COLOR_PRIMARY = new Color(0x2D, 0x89, 0xEF);
    private static final Color COLOR_SECONDARY = new Color(0x36, 0x3B, 0x4A);
    private static final Color COLOR_HIGHLIGHT = new Color(0x33, 0x99, 0xFF);

    private final ClientController controller;
    private final GameState gameState;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private JTextField txtPlayerName;

    private JLabel lblPlayer1Name;
    private JLabel lblPlayer2Name;
    private JLabel lblPlayer1Time;
    private JLabel lblPlayer2Time;
    private JPanel boardPanel;
    private JButton[][] cells;

    private Timer turnTimer;

    public CaroClientFrame(ClientController controller, GameState gameState) {
        this.controller = controller;
        this.gameState = gameState;
        initFrame();
        initHomePanel();
        initGamePanel();
        setupTimer();
    }

    private void initFrame() {
        setTitle("Caro Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mainPanel.setBackground(BG_DARK);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void initHomePanel() {
        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Caro Game", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 32f));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblName = new JLabel("Nhập tên người chơi:");
        lblName.setForeground(Color.WHITE);
        txtPlayerName = new JTextField(20);
        JButton btnPlayNow = new JButton("Chơi ngay");
        stylePrimaryButton(btnPlayNow);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        homePanel.add(lblTitle, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        homePanel.add(lblName, gbc);

        gbc.gridx = 1;
        homePanel.add(txtPlayerName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        homePanel.add(btnPlayNow, gbc);

        btnPlayNow.addActionListener(e -> {
            String name = txtPlayerName.getText();
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người chơi.");
                return;
            }
            controller.onPlayNow(name);
        });

        mainPanel.add(homePanel, "HOME");
        cardLayout.show(mainPanel, "HOME");
    }

    private void initGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(BG_DARK);

        JPanel statusPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusPanel.setBackground(BG_DARK);

        JPanel p1Panel = new JPanel(new BorderLayout());
        p1Panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p1Panel.setBackground(BG_PANEL);
        lblPlayer1Name = new JLabel("Player 1", SwingConstants.LEFT);
        lblPlayer1Time = new JLabel("05:00", SwingConstants.RIGHT);
        lblPlayer1Name.setForeground(Color.WHITE);
        lblPlayer1Time.setForeground(Color.WHITE);
        p1Panel.add(lblPlayer1Name, BorderLayout.WEST);
        p1Panel.add(lblPlayer1Time, BorderLayout.EAST);

        JPanel p2Panel = new JPanel(new BorderLayout());
        p2Panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p2Panel.setBackground(BG_PANEL);
        lblPlayer2Name = new JLabel("Đối thủ", SwingConstants.LEFT);
        lblPlayer2Time = new JLabel("05:00", SwingConstants.RIGHT);
        lblPlayer2Name.setForeground(Color.WHITE);
        lblPlayer2Time.setForeground(Color.WHITE);
        p2Panel.add(lblPlayer2Name, BorderLayout.WEST);
        p2Panel.add(lblPlayer2Time, BorderLayout.EAST);

        statusPanel.add(p1Panel);
        statusPanel.add(p2Panel);

        int size = gameState.getBoardSize();
        boardPanel = new JPanel(new GridLayout(size, size));
        boardPanel.setBackground(BG_BOARD);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        cells = new JButton[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                JButton cell = createBoardCell(i, j);
                cells[i][j] = cell;
                boardPanel.add(cell);
            }
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(BG_DARK);
        JButton btnCancel = new JButton("Hủy ván chơi");
        styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> {
            stopTimer();
            cardLayout.show(mainPanel, "HOME");
        });
        bottomPanel.add(btnCancel);

        gamePanel.add(statusPanel, BorderLayout.NORTH);
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, "GAME");
    }

    private JButton createBoardCell(int row, int col) {
        JButton cell = new JButton();
        cell.setMargin(new Insets(0, 0, 0, 0));
        cell.setFocusPainted(false);
        cell.setBorderPainted(false);
        cell.setContentAreaFilled(false);
        cell.setOpaque(true);
        cell.setBackground(BG_BOARD);
        cell.setForeground(Color.WHITE);
        cell.setFont(cell.getFont().deriveFont(Font.BOLD, 16f));
        cell.addActionListener(e -> controller.onLocalCellClicked(row, col));
        return cell;
    }

    private void setupTimer() {
        turnTimer = new Timer(1000, e -> {
            if (gameState.isGameOver()) {
                stopTimer();
                return;
            }
            gameState.decrementCurrentPlayerTime(1000);
            updateTimeLabels();
            if (gameState.isCurrentPlayerOutOfTime()) {
                stopTimer();
                controller.onTimeoutForCurrentPlayer();
            }
        });
        turnTimer.setRepeats(true);
    }

    public void initGameScreenFromState() {
        lblPlayer1Name.setText("Người chơi: " + gameState.getPlayer1Name());
        lblPlayer2Name.setText("Đối thủ: " + gameState.getPlayer2Name());
        updateTimeLabels();
        clearBoardUI();
        updateTurnHighlight();
        startTimer();
    }

    public void showGameScreen() {
        cardLayout.show(mainPanel, "GAME");
    }

    private void updateTimeLabels() {
        lblPlayer1Time.setText(formatMillis(gameState.getPlayer1RemainingMillis()));
        lblPlayer2Time.setText(formatMillis(gameState.getPlayer2RemainingMillis()));
    }

    private String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void clearBoardUI() {
        int size = gameState.getBoardSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setText("");
                cells[i][j].setBackground(BG_BOARD);
                cells[i][j].setEnabled(true);
            }
        }
    }

    public void updateBoardCell(int row, int col, char symbol, boolean highlight) {
        if (row < 0 || row >= cells.length || col < 0 || col >= cells.length) {
            return;
        }
        JButton cell = cells[row][col];
        cell.setText(String.valueOf(symbol));
        if (highlight) {
            resetBoardHighlight();
            cell.setBackground(Color.YELLOW);
        }
    }

    private void resetBoardHighlight() {
        int size = gameState.getBoardSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setBackground(BG_BOARD);
            }
        }
    }

    public void updateTurnHighlight() {
        boolean p1Turn = gameState.isPlayer1Turn();
        Color normal = getBackground();

        lblPlayer1Name.setOpaque(true);
        lblPlayer2Name.setOpaque(true);

        lblPlayer1Name.setBackground(p1Turn ? COLOR_HIGHLIGHT : normal);
        lblPlayer2Name.setBackground(p1Turn ? normal : COLOR_HIGHLIGHT);

        lblPlayer1Name.repaint();
        lblPlayer2Name.repaint();
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_SECONDARY);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void handleGameResult(int result) {
        stopTimer();
        String message;
        switch (result) {
            case GameState.RESULT_PLAYER1_WIN:
                message = "Người chơi " + gameState.getPlayer1Name() + " thắng!";
                break;
            case GameState.RESULT_PLAYER2_WIN:
                message = "Đối thủ thắng!";
                break;
            case GameState.RESULT_DRAW:
                message = "Hòa!";
                break;
            default:
                message = "Kết thúc ván!";
        }
        JOptionPane.showMessageDialog(this, message);
    }

    private void startTimer() {
        if (!turnTimer.isRunning()) {
            turnTimer.start();
        }
    }

    private void stopTimer() {
        if (turnTimer.isRunning()) {
            turnTimer.stop();
        }
    }
}
