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
        // Gửi thông tin bắt đầu game
        // Player 1: Đối thủ là Player 2, Bạn cầm X
        player1.sendPacket(new StartPacket("Player 2", "X")); 
        
        // Player 2: Đối thủ là Player 1, Bạn cầm O
        player2.sendPacket(new StartPacket("Player 1", "O"));
        
        System.out.println("Room " + roomId + " started.");
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
            
            // Có thể thêm logic đóng phòng hoặc reset game ở đây
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
}