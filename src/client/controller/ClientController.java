package client.controller;

import client.MainClientFX;
import client.model.GameState;
import client.network.ClientReceiver;
import client.network.ClientSocket;
import common.packet.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ClientController {

    private final GameState gameState;
    private final MainClientFX mainFX;
    private String localPlayerName; // Lưu tên người chơi local

    public ClientController(GameState gameState, MainClientFX mainFX) {
        this.gameState = gameState;
        this.mainFX = mainFX;
    }

    public void onPlayNow(String playerName) {
        // Hiển thị loading indicator
        Platform.runLater(() -> {
            mainFX.showLoadingDialog("Đang tìm đối thủ...", "Đang tìm đối thủ, phù hợp với bạn...");
        });
        
        // 1. Kết nối mạng
        if (!ClientSocket.getInstance().isConnected()) {
            if (!ClientSocket.getInstance().connect()) {
                Platform.runLater(() -> {
                    mainFX.hideLoadingDialog();
                    mainFX.showCustomErrorDialog("Tìm đối thủ thất bại", "Không tìm được đối thủ!");
                });
                return;
            }
            // Khởi động luồng nhận tin
            new Thread(new ClientReceiver(this)).start();
        }

        // 2. Lưu tên người chơi local
        String name = (playerName == null || playerName.trim().isEmpty()) ? "Player" : playerName.trim();
        this.localPlayerName = name;

        // 3. Gửi JoinPacket
        ClientSocket.getInstance().send(new JoinPacket(name));
        
        // Loading sẽ được ẩn khi nhận StartPacket trong onPacketReceived
    }

    public void onLocalCellClicked(int row, int col) {
        // Chỉ cho phép đánh khi đến lượt và game chưa kết thúc
        if (gameState.isGameOver() || !gameState.isLocalPlayersTurn()) {
            return;
        }
        if (!gameState.canPlace(row, col)) {
            return;
        }

        // Gửi nước đi lên Server
        ClientSocket.getInstance().send(new MovePacket(row, col));
    }

    public void onPacketReceived(Packet packet) {
        // Chuyển sang luồng JavaFX UI để xử lý an toàn
        Platform.runLater(() -> {
            PacketType type = packet.getType();
            switch (type) {
                case START:
                    // Ẩn loading dialog
                    mainFX.hideLoadingDialog();
                    
                    StartPacket sp = (StartPacket) packet;
                    // Xác định tên player1 và player2 dựa trên symbol
                    // Nếu local nhận "X" thì local là player1, nếu nhận "O" thì local là player2
                    String player1Name, player2Name;
                    // Đảm bảo localPlayerName đã được set (fallback về "Player" nếu null)
                    String localName = (localPlayerName != null && !localPlayerName.trim().isEmpty()) 
                            ? localPlayerName : "Player";
                    if ("X".equalsIgnoreCase(sp.getYourSymbol())) {
                        // Local là player1 (X)
                        player1Name = localName;
                        player2Name = sp.getOpponentName();
                    } else {
                        // Local là player2 (O)
                        player1Name = sp.getOpponentName();
                        player2Name = localName;
                    }
                    gameState.resetForNewGame(player1Name, player2Name);
                    gameState.setLocalPlayerSymbol(sp.getYourSymbol());
                    mainFX.switchToGameScene();
                    break;

                case UPDATE:
                    if (packet instanceof UpdatePacket) {
                        UpdatePacket up = (UpdatePacket) packet;
                        int[][] board = up.getBoard();
                        int nextTurnPlayerId = up.getCurrentPlayer();

                        // --- LOGIC QUAN TRỌNG: Cập nhật bàn cờ và tìm nước đi mới ---
                        updateBoardUI(board);

                        // Đồng bộ lượt đi chính xác với Server
                        // Server gửi nextTurnPlayerId: 1 = player1 (X), 2 = player2 (O)
                        boolean isPlayer1Turn = (nextTurnPlayerId == 1);
                        gameState.setPlayer1Turn(isPlayer1Turn);
                        mainFX.updateTurnHighlight();
                    }
                    break;

                case RESULT:
                    ResultPacket rp = (ResultPacket) packet;
                    gameState.setGameOver(true);
                    mainFX.stopTimer();
                    mainFX.showResultAlert(rp.getResultCode());
                    break;

                case MESSAGE:
                    MessagePacket msgPacket = (MessagePacket) packet;
                    mainFX.showCustomMessageDialog(msgPacket.getMessage());
                    break;

                case ERROR:
                    ErrorPacket err = (ErrorPacket) packet;
                    mainFX.showCustomErrorDialog("Lỗi", err.getErrorMessage());
                    break;

                case DRAW_REQUEST:
                    // Đối thủ yêu cầu cầu hòa
                    mainFX.showCustomDrawRequestDialog(
                            () -> onDrawResponse(true),
                            () -> onDrawResponse(false)
                    );
                    break;

                default:
                    break;
            }
        });
    }

    /**
     * Hàm này thực hiện 3 bước:
     * 1. Tìm sự khác biệt giữa bàn cờ Server gửi (board) và bàn cờ hiện tại (gameState).
     * 2. Cập nhật dữ liệu vào gameState.
     * 3. Vẽ lại giao diện, chỉ bật highlight cho nước đi mới nhất.
     */
    private void updateBoardUI(int[][] newBoard) {
        int lastMoveRow = -1;
        int lastMoveCol = -1;

        // BƯỚC 1: Tìm ra nước đi mới (Server có quân mà Client đang trống)
        for (int r = 0; r < newBoard.length; r++) {
            for (int c = 0; c < newBoard[r].length; c++) {
                if (newBoard[r][c] != 0 && gameState.getCell(r, c) == 0) {
                    lastMoveRow = r;
                    lastMoveCol = c;
                }
            }
        }

        // BƯỚC 2: Cập nhật dữ liệu vào gameState và vẽ các quân cờ
        for (int r = 0; r < newBoard.length; r++) {
            for (int c = 0; c < newBoard[r].length; c++) {
                if (newBoard[r][c] != 0) {
                    // Cập nhật model
                    // Lưu ý: Dùng hàm setCell hoặc tương đương để gán giá trị mà không check luật
                    if (gameState.getCell(r, c) == 0) {
                        gameState.placeMove(r, c); // Hoặc gameState.setCell(r, c, newBoard[r][c]) nếu có
                    }

                    // Mapping giá trị int sang ký tự
                    char symbol = (newBoard[r][c] == 1) ? 'X' : 'O';

                    // Vẽ quân cờ lên UI nhưng TẮT highlight trước
                    mainFX.updateBoardCell(r, c, symbol, false);
                }
            }
        }

        // BƯỚC 3: Highlight riêng nước đi mới nhất (nếu tìm thấy)
        if (lastMoveRow != -1) {
            int value = newBoard[lastMoveRow][lastMoveCol];
            char symbol = (value == 1) ? 'X' : 'O';
            // BẬT highlight cho duy nhất ô này
            mainFX.updateBoardCell(lastMoveRow, lastMoveCol, symbol, true);
        }
    }

    public void onTimeout() {
        Platform.runLater(() -> {
            mainFX.stopTimer();
            // Nếu đến lượt mình mà hết giờ -> Mình thua (Đối thủ thắng)
            if (gameState.isLocalPlayersTurn()) {
                mainFX.showResultAlert(GameState.RESULT_PLAYER2_WIN); // Server sẽ gửi Result chuẩn, đây là dự đoán UI
            } else {
                mainFX.showResultAlert(GameState.RESULT_PLAYER1_WIN);
            }
            gameState.setGameOver(true);
        });
    }

    public void onSurrender() {
        ClientSocket.getInstance().send(new SurrenderPacket());
    }

    public void onDrawRequest() {
        ClientSocket.getInstance().send(new DrawRequestPacket());
    }

    public void onDrawResponse(boolean accepted) {
        ClientSocket.getInstance().send(new DrawResponsePacket(accepted));
    }
}