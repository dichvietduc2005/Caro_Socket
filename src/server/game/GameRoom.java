package server.game;

import common.packet.*;
import server.network.ClientHandler;

public class GameRoom {
    private String roomId;
    private ClientHandler player1; // Đi trước (X)
    private ClientHandler player2; // Đi sau (O)
    private GameBoard gameBoard;
    private int currentTurn; // 1 hoặc 2

    // Biến cờ để kiểm tra game đã kết thúc chưa
    private boolean isGameOver = false;

    public GameRoom(String roomId, ClientHandler p1, ClientHandler p2) {
        this.roomId = roomId;
        this.player1 = p1;
        this.player2 = p2;
        this.gameBoard = new GameBoard();
        this.currentTurn = 1; // Mặc định người 1 đi trước (X)
        this.isGameOver = false;

        // Gán ID nội bộ
        p1.setPlayerID(1);
        p2.setPlayerID(2);
    }

    public boolean bothPlayersReady() {
        return player1.isNameSet() && player2.isNameSet();
    }

    public void startGame() {
        // Gửi thông tin bắt đầu game
        // Constructor StartPacket(opponentName, yourSymbol)
        player1.sendPacket(new StartPacket(player2.getPlayerName(), "X"));
        player2.sendPacket(new StartPacket(player1.getPlayerName(), "O"));

        System.out.println("Room " + roomId + " started: "
                + player1.getPlayerName() + " (X) vs " + player2.getPlayerName() + " (O)");
    }

    public synchronized void processMove(ClientHandler sender, int x, int y) {
        // Nếu game đã kết thúc hoặc không phải lượt thì bỏ qua
        if (isGameOver || sender.getPlayerID() != currentTurn) {
            return;
        }

        // Thực hiện nước đi
        boolean moved = gameBoard.makeMove(x, y, currentTurn);
        if (!moved) {
            return;
        }

        // Kiểm tra thắng thua
        boolean isWin = gameBoard.checkWin(x, y, currentTurn);

        // Đổi lượt
        switchTurn();

        // Gửi cập nhật bàn cờ
        UpdatePacket updatePacket = new UpdatePacket(gameBoard.getMap(), currentTurn);
        player1.sendPacket(updatePacket);
        player2.sendPacket(updatePacket);

        // Xử lý kết quả trận đấu nếu có người thắng
        if (isWin) {
            isGameOver = true; // Đánh dấu game kết thúc

            ClientHandler winner = sender;
            ClientHandler loser = (sender == player1) ? player2 : player1;

            winner.sendPacket(new ResultPacket(1, "Bạn đã chiến thắng!"));
            loser.sendPacket(new ResultPacket(2, "Bạn đã thua cuộc!"));
            return;
        }

        // Kiểm tra hòa (bàn cờ đầy)
        if (checkDraw()) {
            isGameOver = true; // Đánh dấu game kết thúc

            player1.sendPacket(new ResultPacket(3, "Trận đấu hòa - bàn cờ đã đầy!"));
            player2.sendPacket(new ResultPacket(3, "Trận đấu hòa - bàn cờ đã đầy!"));
            System.out.println("Room " + roomId + ": Draw - board is full.");
        }
    }

    // --- PHƯƠNG THỨC BỊ THIẾU CỦA BẠN ---
    public void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
    }
    // ------------------------------------

    public void handleDisconnect(ClientHandler client) {
        // Nếu game đã kết thúc (thắng/thua/hòa) thì việc thoát là bình thường -> Không báo lỗi
        if (isGameOver) {
            ClientHandler remaining = (client == player1) ? player2 : player1;
            if (remaining != null) {
                remaining.sendPacket(new MessagePacket("Đối thủ đã rời phòng.", true));
            }
            return;
        }

        // Nếu game ĐANG CHƠI mà thoát -> Xử thua
        ClientHandler remaining = (client == player1) ? player2 : player1;
        if (remaining != null) {
            isGameOver = true;
            // Code 4: Đối thủ thoát
            remaining.sendPacket(new ResultPacket(4, "Đối thủ đã thoát trận. Bạn thắng!"));
        }
    }

    public void handlePlayAgainRequest(ClientHandler sender) {
        ClientHandler opponent = (sender == player1) ? player2 : player1;
        if (opponent != null) {
            opponent.sendPacket(new PlayAgainRequestPacket());
            sender.sendMessage("Đã gửi yêu cầu chơi lại. Đang chờ đối thủ...");
        }
    }

    public void handlePlayAgainResponse(ClientHandler sender, boolean accepted) {
        ClientHandler opponent = (sender == player1) ? player2 : player1;

        if (opponent != null) {
            opponent.sendPacket(new PlayAgainResponsePacket(accepted));
        }

        if (accepted) {
            resetGame();
        }
    }

    private void resetGame() {
        // 1. Reset bàn cờ logic
        if (this.gameBoard != null) {
            this.gameBoard.reset();
        }

        // 2. Reset trạng thái phòng
        this.currentTurn = 1;
        this.isGameOver = false;

        // 3. Gửi gói tin START game mới
        if (player1 != null && player2 != null) {
            player1.sendPacket(new StartPacket(player2.getPlayerName(), "X"));
            player2.sendPacket(new StartPacket(player1.getPlayerName(), "O"));

            System.out.println("Game restarted in room " + roomId);
        }
    }

    public synchronized void handleSurrender(ClientHandler sender) {
        isGameOver = true; // Đánh dấu kết thúc

        ClientHandler winner = (sender == player1) ? player2 : player1;
        sender.sendPacket(new ResultPacket(2, "Bạn đã xin thua!"));
        winner.sendPacket(new ResultPacket(1, "Đối thủ đã xin thua. Bạn thắng!"));

        System.out.println("Room " + roomId + ": " + sender.getPlayerName() + " surrendered.");
    }

    public synchronized void handleDrawRequest(ClientHandler sender) {
        ClientHandler receiver = (sender == player1) ? player2 : player1;
        receiver.sendPacket(new DrawRequestPacket());
        System.out.println("Room " + roomId + ": " + sender.getPlayerName() + " requested a draw.");
    }

    public synchronized void handleDrawResponse(ClientHandler sender, boolean accepted) {
        ClientHandler requester = (sender == player1) ? player2 : player1;

        if (accepted) {
            isGameOver = true; // Đánh dấu kết thúc
            player1.sendPacket(new ResultPacket(3, "Trận đấu hòa!"));
            player2.sendPacket(new ResultPacket(3, "Trận đấu hòa!"));
            System.out.println("Room " + roomId + ": Draw accepted.");
        } else {
            requester.sendPacket(new MessagePacket("Đối thủ đã từ chối yêu cầu cầu hòa.", true));
            System.out.println("Room " + roomId + ": Draw rejected by " + sender.getPlayerName());
        }
    }

    private boolean checkDraw() {
        return gameBoard.isFull();
    }
}