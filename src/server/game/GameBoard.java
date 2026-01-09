package server.game;

public class GameBoard {
    public static final int SIZE = 20;
    private int[][] map;

    public GameBoard() {
        this.map = new int[SIZE][SIZE];
    }

    public void reset() {
        this.map = new int[SIZE][SIZE];
    }

    public int[][] getMap() {
        return map;
    }

    public boolean makeMove(int x, int y, int playerID) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
            return false;
        if (map[x][y] != 0)
            return false;

        map[x][y] = playerID;
        return true;
    }

    public boolean checkWin(int x, int y, int playerID) {
        return checkDirection(x, y, playerID, 1, 0)
                || checkDirection(x, y, playerID, 0, 1)
                || checkDirection(x, y, playerID, 1, 1)
                || checkDirection(x, y, playerID, 1, -1);
    }

    private boolean checkDirection(int x, int y, int playerID, int deltaX, int deltaY) {
        int count = 1;
        int i = 1;
        while (isValid(x + i * deltaX, y + i * deltaY) && map[x + i * deltaX][y + i * deltaY] == playerID) {
            count++;
            i++;
        }

        i = 1;
        while (isValid(x - i * deltaX, y - i * deltaY) && map[x - i * deltaX][y - i * deltaY] == playerID) {
            count++;
            i++;
        }

        return count >= 5;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    /**
     * Kiểm tra bàn cờ đã đầy chưa (dùng để xác định hòa)
     */
    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (map[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        GameBoard board = new GameBoard();

        System.out.println("--- Test 1: Check Horizontal Win ---");
        board.makeMove(10, 10, 1);
        board.makeMove(10, 11, 1);
        board.makeMove(10, 12, 1);
        board.makeMove(10, 13, 1);

        boolean moved = board.makeMove(10, 14, 1);
        boolean isWin = board.checkWin(10, 14, 1);

        System.out.println("Move 5 success: " + moved);
        System.out.println("CheckWin Result: " + (isWin ? "WIN" : "NOT WIN"));

        System.out.println("\n--- Test 2: Check Continue Game ---");
        board.reset();
        board.makeMove(5, 5, 2);
        boolean winCheck = board.checkWin(5, 5, 2);
        System.out.println("CheckWin Result: " + (winCheck ? "WIN" : "CONTINUE"));
    }
}