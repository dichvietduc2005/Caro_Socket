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
                    new Alert(Alert.AlertType.INFORMATION, msgPacket.getMessage()).showAndWait();
                    break;

                case ERROR:
                    ErrorPacket err = (ErrorPacket) packet;
                    new Alert(Alert.AlertType.ERROR, err.getErrorMessage()).show();
                    break;

                case DRAW_REQUEST:
                    // Đối thủ yêu cầu cầu hòa - hiển thị dialog hỏi
                    Alert drawAlert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Đối thủ yêu cầu cầu hòa. Bạn có đồng ý?");
                    drawAlert.showAndWait().ifPresent(response -> {
                        boolean accepted = (response == javafx.scene.control.ButtonType.OK);
                        onDrawResponse(accepted);
                    });
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

    /**
     * Gửi yêu cầu xin thua lên Server
     */
    public void onSurrender() {
        ClientSocket.getInstance().send(new SurrenderPacket());
    }

    /**
     * Gửi yêu cầu cầu hòa lên Server
     */
    public void onDrawRequest() {
        ClientSocket.getInstance().send(new DrawRequestPacket());
    }

    /**
     * Gửi phản hồi cầu hòa lên Server
     */
    public void onDrawResponse(boolean accepted) {
        ClientSocket.getInstance().send(new DrawResponsePacket(accepted));
    }
}