package client.controller;

import client.MainClientFX;
import client.model.GameState;
import client.network.ClientReceiver;
import client.network.ClientSocket;
import common.packet.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ClientController {

    private final GameState gameState;
    private final MainClientFX mainFX;

    public ClientController(GameState gameState, MainClientFX mainFX) {
        this.gameState = gameState;
        this.mainFX = mainFX;
    }

    public void onPlayNow(String playerName) {
        // 1. Kết nối mạng
        if (!ClientSocket.getInstance().isConnected()) {
            if (!ClientSocket.getInstance().connect()) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Không thể kết nối tới Server!").showAndWait();
                });
                return;
            }
            // Khởi động luồng nhận tin
            new Thread(new ClientReceiver(this)).start();
        }

        // 2. Gửi JoinPacket
        String name = (playerName == null || playerName.trim().isEmpty()) ? "Player" : playerName.trim();
        ClientSocket.getInstance().send(new JoinPacket(name));
    }

    public void onLocalCellClicked(int row, int col) {
        // Chỉ cho phép đánh khi đến lượt và game chưa kết thúc
        if (gameState.isGameOver() || !gameState.isLocalPlayersTurn()) {
            return;
        }
        if (!gameState.canPlace(row, col)) {
            return;
        }

        // --- SỬA LỖI TẠI ĐÂY ---
        // MovePacket chỉ cần row và col. Server đã biết ai gửi gói tin này rồi.
        ClientSocket.getInstance().send(new MovePacket(row, col));
    }

    public void onPacketReceived(Packet packet) {
        // Chuyển sang luồng JavaFX UI để xử lý an toàn
        Platform.runLater(() -> {
            PacketType type = packet.getType();
            switch (type) {
                case START:
                    StartPacket sp = (StartPacket) packet;
                    gameState.resetForNewGame(gameState.getPlayer1Name(), sp.getOpponentName());
                    gameState.setLocalPlayerSymbol(sp.getYourSymbol());
                    mainFX.switchToGameScene();
                    break;

                // --- CẬP NHẬT LOGIC NHẬN BÀN CỜ TỪ SERVER ---
                // Server gửi UpdatePacket thay vì MovePacket
                case UPDATE:
                    if (packet instanceof UpdatePacket) {
                        UpdatePacket up = (UpdatePacket) packet;
                        int[][] board = up.getBoard();
                        int nextTurnPlayerId = up.getCurrentPlayer();

                        // Cập nhật lại toàn bộ giao diện bàn cờ dựa trên mảng 2 chiều nhận được
                        updateBoardUI(board);

                        // Cập nhật lượt đi (Server gửi ID người đi tiếp: 1 hoặc 2)
                        // Bạn cần ánh xạ ID này về logic của client (IsLocalTurn hay không)
                        // Giả sử: StartPacket trả về ID của mình, ta cần lưu lại để so sánh.
                        // Để đơn giản, ta cứ đổi lượt dựa trên state hiện tại:
                        gameState.switchTurn();
                        mainFX.updateTurnHighlight();
                    }
                    break;

                case RESULT:
                    ResultPacket rp = (ResultPacket) packet;
                    gameState.setGameOver(true);
                    mainFX.stopTimer();
                    // Code 1: Thắng, Code 2: Thua, Code 4: Đối thủ thoát
                    String msg = (rp.getResultCode() == 1 || rp.getResultCode() == 4) ? "CHIẾN THẮNG!" : "THẤT BẠI!";
                    mainFX.showResultAlert(rp.getResultCode());
                    break;

                case MESSAGE:
                    MessagePacket msgPacket = (MessagePacket) packet;
                    new Alert(Alert.AlertType.INFORMATION, msgPacket.getMessage()).showAndWait();
                    break;

                case ERROR:
                    ErrorPacket err = (ErrorPacket) packet;
                    new Alert(Alert.AlertType.ERROR, err.getErrorMessage()).show();
                    break;

                default:
                    break;
            }
        });
    }

    // Hàm phụ trợ để vẽ lại bàn cờ từ dữ liệu Server gửi về
    private void updateBoardUI(int[][] board) {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                int value = board[r][c]; // 0: Trống, 1: X, 2: O (Giả định quy ước Server)
                if (value != 0) {
                    // Mapping giá trị int sang ký tự
                    char symbol = (value == 1) ? 'X' : 'O';
                    gameState.placeMove(r, c); // Cập nhật logic client
                    mainFX.updateBoardCell(r, c, symbol, true); // Vẽ lên giao diện
                }
            }
        }
    }

    public void onTimeout() {
        Platform.runLater(() -> {
            mainFX.stopTimer();
            if (gameState.isLocalPlayersTurn()) {
                mainFX.showResultAlert(GameState.RESULT_PLAYER2_WIN);
            } else {
                mainFX.showResultAlert(GameState.RESULT_PLAYER1_WIN);
            }
            gameState.setGameOver(true);
        });
    }
}