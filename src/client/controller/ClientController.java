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

        // Cập nhật trạng thái chờ trên UI (nếu cần)
    }

    public void onLocalCellClicked(int row, int col) {
        // Chỉ cho phép đánh khi đến lượt và game chưa kết thúc
        if (gameState.isGameOver() || !gameState.isLocalPlayersTurn()) {
            return;
        }
        if (!gameState.canPlace(row, col)) {
            return;
        }

        // Gửi nước đi lên Server (không tự đánh cục bộ)
        ClientSocket.getInstance().send(new MovePacket(row, col, String.valueOf(gameState.getLocalPlayerSymbol())));
    }

    public void onPacketReceived(Packet packet) {
        // Chuyển sang luồng JavaFX UI để xử lý an toàn
        Platform.runLater(() -> {
            PacketType type = packet.getType();
            switch (type) {
                case START:
                    StartPacket sp = (StartPacket) packet;
                    // Server báo bắt đầu trận
                    gameState.resetForNewGame(gameState.getPlayer1Name(), sp.getOpponentName());
                    gameState.setLocalPlayerSymbol(sp.getYourSymbol());

                    // Chuyển sang màn hình game
                    mainFX.switchToGameScene();
                    break;

                case MOVE:
                    MovePacket mp = (MovePacket) packet;
                    // Nhận nước đi từ Server (có thể là của mình hoặc đối thủ)
                    char symbol = mp.getPlayerSymbol().charAt(0);
                    int r = mp.getX();
                    int c = mp.getY();

                    gameState.placeMove(r, c);
                    mainFX.updateBoardCell(r, c, symbol, true);

                    // Đổi lượt
                    gameState.switchTurn();
                    mainFX.updateTurnHighlight();
                    break;

                case RESULT:
                    ResultPacket rp = (ResultPacket) packet;
                    gameState.setGameOver(true);
                    mainFX.stopTimer();
                    mainFX.showResultAlert(rp.getResultCode());
                    break;

                case MESSAGE:
                    MessagePacket msg = (MessagePacket) packet;
                    new Alert(Alert.AlertType.INFORMATION, msg.getMessage()).showAndWait();
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

    public void onTimeout() {
        Platform.runLater(() -> {
            mainFX.stopTimer();
            if (gameState.isLocalPlayersTurn()) {
                mainFX.showResultAlert(GameState.RESULT_PLAYER2_WIN); // Giả sử mình là P1 thì P2 thắng
            } else {
                mainFX.showResultAlert(GameState.RESULT_PLAYER1_WIN);
            }
            gameState.setGameOver(true);
        });
    }
}
