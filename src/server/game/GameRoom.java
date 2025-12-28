package server.game;


import server.network.ClientHandler;

public class GameRoom {
    private String roomId;
    private ClientHandler player1; // Chủ phòng (X)
    private ClientHandler player2; // Khách (O)
    private GameBoard gameBoard;
    private int currentTurn; // 1 hoặc 2

    public GameRoom(String roomId, ClientHandler p1, ClientHandler p2) {
        this.roomId = roomId;
        this.player1 = p1;
        this.player2 = p2;
        this.gameBoard = new GameBoard();
        this.currentTurn = 1; // Mặc định người 1 đi trước
    }


    public String getRoomId() {
        return roomId;
    }

    public ClientHandler getPlayer1() { return player1; }
    public ClientHandler getPlayer2() { return player2; }

    public int getCurrentTurn() {
        return currentTurn;
    }


    public void switchTurn() {
        if (currentTurn == 1) currentTurn = 2;
        else currentTurn = 1;
    }

    public GameBoard getGameBoard() {
        return this.gameBoard;
    }
}