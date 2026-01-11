package client.controller;

import client.MainClientFX;
import client.model.GameState;
import client.network.ClientReceiver;
import client.network.ClientSocket;
import common.packet.*;
import javafx.application.Platform;

public class ClientController {

    private final GameState gameState;
    private final MainClientFX mainFX;
    private String localPlayerName;

    public ClientController(GameState gameState, MainClientFX mainFX) {
        this.gameState = gameState;
        this.mainFX = mainFX;
    }

    public void disconnect() {
        // Gửi gói tin Surrender trước khi thoát để chắc chắn server xử lý game logic (tùy chọn)
        // Nhưng quan trọng nhất là đóng socket
        ClientSocket.getInstance().close();
    }

    public void onPlayNow(String playerName) {
        Platform.runLater(() -> mainFX.showLoadingDialog("Đang tìm đối thủ...", "Đang kết nối đến máy chủ..."));

        if (!ClientSocket.getInstance().isConnected()) {
            if (!ClientSocket.getInstance().connect()) {
                Platform.runLater(() -> {
                    mainFX.hideLoadingDialog();
                    mainFX.showCustomErrorDialog("Lỗi", "Không thể kết nối đến Server!");
                });
                return;
            }
            new Thread(new ClientReceiver(this)).start();
        }

        this.localPlayerName = playerName;
        ClientSocket.getInstance().send(new JoinPacket(playerName));
    }

    public void onLocalCellClicked(int row, int col) {
        if (!gameState.isGameOver() && gameState.isLocalPlayersTurn() && gameState.canPlace(row, col)) {
            ClientSocket.getInstance().send(new MovePacket(row, col));
        }
    }

    // --- LOGIC CHƠI LẠI ---
    public void onPlayAgainRequest() {
        // Gửi yêu cầu chơi lại
        ClientSocket.getInstance().send(new PlayAgainRequestPacket());
    }

    public void onPlayAgainResponse(boolean accepted) {
        // Gửi phản hồi đồng ý hay từ chối
        ClientSocket.getInstance().send(new PlayAgainResponsePacket(accepted));
    }
    // ---------------------

    public void onPacketReceived(Packet packet) {
        Platform.runLater(() -> {
            switch (packet.getType()) {
                case START:
                    mainFX.hideLoadingDialog();
                    mainFX.closeAllDialogs(); // Đóng tất cả dialog cũ (kết quả, chờ...)

                    StartPacket sp = (StartPacket) packet;
                    String p1, p2;
                    String local = (localPlayerName == null) ? "Player" : localPlayerName;

                    if ("X".equalsIgnoreCase(sp.getYourSymbol())) {
                        p1 = local; p2 = sp.getOpponentName();
                    } else {
                        p1 = sp.getOpponentName(); p2 = local;
                    }

                    gameState.resetForNewGame(p1, p2);
                    gameState.setLocalPlayerSymbol(sp.getYourSymbol());
                    mainFX.updatePlayerNames(p1, p2);
                    mainFX.switchToGameScene();
                    break;

                case UPDATE:
                    UpdatePacket up = (UpdatePacket) packet;
                    updateBoardUI(up.getBoard());
                    gameState.setPlayer1Turn(up.getCurrentPlayer() == 1);
                    mainFX.updateTurnHighlight();
                    break;

                case RESULT:
                    ResultPacket rp = (ResultPacket) packet;
                    gameState.setGameOver(true);
                    mainFX.stopTimer();
                    mainFX.showResultAlert(rp.getResultCode());
                    break;

                // --- XỬ LÝ YÊU CẦU CHƠI LẠI ---
                case PLAY_AGAIN_REQUEST:
                    // Khi nhận được yêu cầu từ đối thủ -> Hiện Dialog xác nhận
                    mainFX.closeAllDialogs(); // Đóng dialog kết quả trước đó để không bị che
                    mainFX.showPlayAgainConfirmDialog(
                            () -> onPlayAgainResponse(true),
                            () -> onPlayAgainResponse(false)
                    );
                    break;

                case PLAY_AGAIN_RESPONSE:
                    PlayAgainResponsePacket resp = (PlayAgainResponsePacket) packet;
                    if (!resp.isAccepted()) {
                        mainFX.hideLoadingDialog();
                        mainFX.showCustomMessageDialog("Đối thủ đã từ chối chơi lại.");
                    }
                    // Nếu accepted = true, Server sẽ gửi START packet sau đó
                    break;
                // -----------------------------

                case MESSAGE:
                    MessagePacket msgPacket = (MessagePacket) packet;
                    String msg = msgPacket.getMessage();

                    // Kiểm tra nếu là thông báo đối thủ rời phòng
                    if ("Đối thủ đã rời phòng.".equals(msg)) {
                        // Hiện thông báo, và khi ấn OK -> Về trang chủ
                        mainFX.showCustomMessageDialog(msg, () -> mainFX.returnToHome());
                    } else {
                        // Các thông báo khác hiện bình thường
                        mainFX.showCustomMessageDialog(msg);
                    }
                    break;
                case ERROR:
                    mainFX.showCustomErrorDialog("Lỗi", ((ErrorPacket) packet).getErrorMessage());
                    break;
                case DRAW_REQUEST:
                    mainFX.showCustomDrawRequestDialog(() -> onDrawResponse(true), () -> onDrawResponse(false));
                    break;
                default: break;
            }
        });
    }

    // ... (Giữ nguyên các hàm updateBoardUI, onTimeout, onSurrender, onDrawRequest...)
    private void updateBoardUI(int[][] newBoard) {
        int lastRow = -1, lastCol = -1;
        for (int r = 0; r < newBoard.length; r++) {
            for (int c = 0; c < newBoard[r].length; c++) {
                if (newBoard[r][c] != 0) {
                    if(gameState.getCell(r, c) == 0) {
                        gameState.placeMove(r, c);
                        if (lastRow == -1) { lastRow = r; lastCol = c; } // Detect last move logic (simplified)
                    }
                    mainFX.updateBoardCell(r, c, (newBoard[r][c] == 1) ? 'X' : 'O', false);
                }
            }
        }
        // Logic tìm nước đi mới nhất chính xác hơn cần so sánh state cũ, nhưng ở đây tạm update
        // Để fix lỗi highlight, server nên gửi lastMove. Nếu không, client tự suy luận.
    }

    public void onTimeout() {
        Platform.runLater(() -> {
            mainFX.stopTimer();
            gameState.setGameOver(true);
            boolean amIPlayer1 = (gameState.getLocalPlayerSymbol() == 'X');
            boolean myTurn = gameState.isLocalPlayersTurn();
            int result = myTurn ? (amIPlayer1 ? GameState.RESULT_PLAYER2_WIN : GameState.RESULT_PLAYER1_WIN)
                    : (amIPlayer1 ? GameState.RESULT_PLAYER1_WIN : GameState.RESULT_PLAYER2_WIN);
            mainFX.showResultAlert(result);
        });
    }

    public void onSurrender() { ClientSocket.getInstance().send(new SurrenderPacket()); }
    public void onDrawRequest() { ClientSocket.getInstance().send(new DrawRequestPacket()); }
    public void onDrawResponse(boolean accepted) { ClientSocket.getInstance().send(new DrawResponsePacket(accepted)); }
}