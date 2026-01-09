package server.game;

import common.packet.*;
import server.network.ClientHandler;

public class GameRoom {
    private String roomId;
    private ClientHandler player1; // Đi trước (X)
    private ClientHandler player2; // Đi sau (O)
    private GameBoard gameBoard;
    private int currentTurn; // 1 hoặc 2

    public GameRoom(String roomId, ClientHandler p1, ClientHandler p2) {
        this.roomId = roomId;
        this.player1 = p1;
        this.player2 = p2;
        this.gameBoard = new GameBoard();
        this.currentTurn = 1; // Mặc định người 1 đi trước (X)

        // Gán ID nội bộ
        p1.setPlayerID(1);
        p2.setPlayerID(2);
    }

    public void startGame() {
        // Gửi thông tin bắt đầu game với tên thật của đối thủ
        // Player 1: Đối thủ là Player 2, Bạn cầm X
        player1.sendPacket(new StartPacket(player2.getPlayerName(), "X"));

        // Player 2: Đối thủ là Player 1, Bạn cầm O
        player2.sendPacket(new StartPacket(player1.getPlayerName(), "O"));

        System.out.println("Room " + roomId + " started: "
                + player1.getPlayerName() + " (X) vs " + player2.getPlayerName() + " (O)");
    }

    public synchronized void processMove(ClientHandler sender, int x, int y) {
        // 1. Kiểm tra lượt đi
        if (sender.getPlayerID() != currentTurn) {
            return; // Không phải lượt của người này
        }

        // 2. Thực hiện nước đi trên bàn cờ logic
        boolean moved = gameBoard.makeMove(x, y, currentTurn);
        if (!moved) {
            return; // Nước đi không hợp lệ (đã có quân hoặc ngoài bàn cờ)
        }

        // 3. Kiểm tra thắng thua TRƯỚC khi đổi lượt
        boolean isWin = gameBoard.checkWin(x, y, currentTurn);

        // 4. Đổi lượt (để báo cho client biết ai đi tiếp)
        switchTurn();

        // 5. Gửi cập nhật bàn cờ cho CẢ 2 người chơi
        // Lúc này UpdatePacket đã khớp constructor (int[][], int)
        UpdatePacket updatePacket = new UpdatePacket(gameBoard.getMap(), currentTurn);
        player1.sendPacket(updatePacket);
        player2.sendPacket(updatePacket);

        // 6. Xử lý kết quả trận đấu nếu có người thắng
        if (isWin) {
            ClientHandler winner = sender;
            ClientHandler loser = (sender == player1) ? player2 : player1;

            // Gửi gói tin kết quả (Code 1: Thắng, Code 2: Thua)
            winner.sendPacket(new ResultPacket(1, "Bạn đã chiến thắng!"));
            loser.sendPacket(new ResultPacket(2, "Bạn đã thua cuộc!"));
            return;
        }

        // 7. Kiểm tra hòa (bàn cờ đầy)
        if (checkDraw()) {
            player1.sendPacket(new ResultPacket(3, "Trận đấu hòa - bàn cờ đã đầy!"));
            player2.sendPacket(new ResultPacket(3, "Trận đấu hòa - bàn cờ đã đầy!"));
            System.out.println("Room " + roomId + ": Draw - board is full.");
        }
    }

    public void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
    }

    public void handleDisconnect(ClientHandler client) {
        ClientHandler remaining = (client == player1) ? player2 : player1;
        // Code 4: Đối thủ thoát
        remaining.sendPacket(new ResultPacket(4, "Đối thủ đã thoát trận. Bạn thắng!"));
    }

    /**
     * Xử lý khi một người chơi xin thua
     */
    public synchronized void handleSurrender(ClientHandler sender) {
        ClientHandler winner = (sender == player1) ? player2 : player1;

        // Người xin thua nhận kết quả thua (code 2)
        sender.sendPacket(new ResultPacket(2, "Bạn đã xin thua!"));

        // Đối thủ nhận kết quả thắng (code 1)
        winner.sendPacket(new ResultPacket(1, "Đối thủ đã xin thua. Bạn thắng!"));

        System.out.println("Room " + roomId + ": " + sender.getPlayerName() + " surrendered.");
    }

    /**
     * Xử lý khi một người chơi yêu cầu cầu hòa
     */
    public synchronized void handleDrawRequest(ClientHandler sender) {
        ClientHandler receiver = (sender == player1) ? player2 : player1;

        // Gửi thông báo cho đối thủ về yêu cầu cầu hòa
        receiver.sendPacket(new DrawRequestPacket());

        System.out.println("Room " + roomId + ": " + sender.getPlayerName() + " requested a draw.");
    }

    /**
     * Xử lý phản hồi cầu hòa
     */
    public synchronized void handleDrawResponse(ClientHandler sender, boolean accepted) {
        ClientHandler requester = (sender == player1) ? player2 : player1;

        if (accepted) {
            // Cả hai đều hòa (code 3)
            player1.sendPacket(new ResultPacket(3, "Trận đấu hòa!"));
            player2.sendPacket(new ResultPacket(3, "Trận đấu hòa!"));
            System.out.println("Room " + roomId + ": Draw accepted.");
        } else {
            // Thông báo yêu cầu hòa bị từ chối
            requester.sendPacket(new MessagePacket("Đối thủ đã từ chối yêu cầu cầu hòa.", true));
            System.out.println("Room " + roomId + ": Draw rejected by " + sender.getPlayerName());
        }
    }

    /**
     * Kiểm tra bàn cờ đầy (hòa)
     */
    private boolean checkDraw() {
        return gameBoard.isFull();
    }
}