package client.model;

import common.config.GameConfig;

public class GameState {

    public static final int EMPTY = 0;
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;

    public static final int RESULT_NONE = 0;
    public static final int RESULT_PLAYER1_WIN = 1;
    public static final int RESULT_PLAYER2_WIN = 2;
    public static final int RESULT_DRAW = 3;

    private final int boardSize;
    private final int[][] board;

    private String player1Name;
    private String player2Name;

    // Mặc định local là player1 (X), sẽ được cập nhật khi nhận StartPacket từ
    // server
    private boolean localIsPlayer1 = true;

    private boolean player1Turn;
    private boolean gameOver;

    private long player1RemainingMillis;
    private long player2RemainingMillis;

    private static final long INITIAL_TIME_MILLIS = 1L * 60L * 1000L;

    // Thông tin về chiến thắng
    private int winningLineRow = -1;
    private int winningLineCol = -1;
    private int winningLineDR = 0; // Direction row
    private int winningLineDC = 0; // Direction col

    public GameState() {
        this.boardSize = GameConfig.BOARD_SIZE;
        this.board = new int[boardSize][boardSize];
        resetForNewGame("Player 1", "Player 2");
    }

    public void resetForNewGame(String player1Name, String player2Name) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = EMPTY;
            }
        }
        this.player1Turn = true;
        this.gameOver = false;
        this.player1RemainingMillis = INITIAL_TIME_MILLIS;
        this.player2RemainingMillis = INITIAL_TIME_MILLIS;
    }

    /**
     * Cập nhật vai trò của người chơi local dựa trên symbol nhận từ server
     * 
     * @param yourSymbol "X" hoặc "O" - symbol của người chơi local
     */
    public void setLocalPlayerSymbol(String yourSymbol) {
        // Nếu nhận "X" thì local là player1, nếu nhận "O" thì local là player2
        this.localIsPlayer1 = "X".equalsIgnoreCase(yourSymbol);
    }

    /**
     * Lấy symbol của người chơi local (X hoặc O)
     */
    public char getLocalPlayerSymbol() {
        return localIsPlayer1 ? 'X' : 'O';
    }

    /**
     * Lấy symbol của đối thủ
     */
    public char getOpponentSymbol() {
        return localIsPlayer1 ? 'O' : 'X';
    }

    public int getBoardSize() {
        return boardSize;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public boolean isLocalPlayersTurn() {
        return localIsPlayer1 ? player1Turn : !player1Turn;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public long getPlayer1RemainingMillis() {
        return player1RemainingMillis;
    }

    public long getPlayer2RemainingMillis() {
        return player2RemainingMillis;
    }

    public void decrementCurrentPlayerTime(long millis) {
        if (gameOver) {
            return;
        }
        if (player1Turn) {
            player1RemainingMillis = Math.max(0, player1RemainingMillis - millis);
        } else {
            player2RemainingMillis = Math.max(0, player2RemainingMillis - millis);
        }
    }

    public boolean isCurrentPlayerOutOfTime() {
        if (player1Turn) {
            return player1RemainingMillis <= 0;
        }
        return player2RemainingMillis <= 0;
    }

    public boolean canPlace(int row, int col) {
        if (gameOver) {
            return false;
        }
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return false;
        }
        return board[row][col] == EMPTY;
    }

    public void placeMove(int row, int col) {
        int value = player1Turn ? PLAYER1 : PLAYER2;
        board[row][col] = value;
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public void switchTurn() {
        player1Turn = !player1Turn;
    }

    /**
     * Set trực tiếp lượt đi (dùng để đồng bộ với Server)
     */
    public void setPlayer1Turn(boolean isPlayer1Turn) {
        this.player1Turn = isPlayer1Turn;
    }

    public char getSymbolAt(int row, int col) {
        int v = board[row][col];
        if (v == PLAYER1) {
            return 'X';
        }
        if (v == PLAYER2) {
            return 'O';
        }
        return ' ';
    }

    public int checkWin(int lastRow, int lastCol) {
        int v = board[lastRow][lastCol];
        if (v == EMPTY) {
            return RESULT_NONE;
        }

        // Kiểm tra hàng (dr=1, dc=0)
        if (hasFiveInARow(lastRow, lastCol, 1, 0, v)) {
            winningLineRow = lastRow;
            winningLineCol = lastCol;
            winningLineDR = 1;
            winningLineDC = 0;
            return winnerFromValue(v);
        }
        // Kiểm tra cột (dr=0, dc=1)
        if (hasFiveInARow(lastRow, lastCol, 0, 1, v)) {
            winningLineRow = lastRow;
            winningLineCol = lastCol;
            winningLineDR = 0;
            winningLineDC = 1;
            return winnerFromValue(v);
        }
        // Kiểm tra đường chéo chính (dr=1, dc=1)
        if (hasFiveInARow(lastRow, lastCol, 1, 1, v)) {
            winningLineRow = lastRow;
            winningLineCol = lastCol;
            winningLineDR = 1;
            winningLineDC = 1;
            return winnerFromValue(v);
        }
        // Kiểm tra đường chéo phụ (dr=1, dc=-1)
        if (hasFiveInARow(lastRow, lastCol, 1, -1, v)) {
            winningLineRow = lastRow;
            winningLineCol = lastCol;
            winningLineDR = 1;
            winningLineDC = -1;
            return winnerFromValue(v);
        }

        if (isBoardFull()) {
            return RESULT_DRAW;
        }
        return RESULT_NONE;
    }

    private int winnerFromValue(int v) {
        return v == PLAYER1 ? RESULT_PLAYER1_WIN : RESULT_PLAYER2_WIN;
    }

    private boolean hasFiveInARow(int row, int col, int dr, int dc, int v) {
        int count = 1;

        int r = row + dr;
        int c = col + dc;
        while (inBounds(r, c) && board[r][c] == v) {
            count++;
            r += dr;
            c += dc;
        }

        r = row - dr;
        c = col - dc;
        while (inBounds(r, c) && board[r][c] == v) {
            count++;
            r -= dr;
            c -= dc;
        }

        return count >= 5;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < boardSize && c >= 0 && c < boardSize;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    // Getter cho thông tin đường chiến thắng
    public int getWinningLineRow() {
        return winningLineRow;
    }

    public int getWinningLineCol() {
        return winningLineCol;
    }

    public int getWinningLineDR() {
        return winningLineDR;
    }

    public int getWinningLineDC() {
        return winningLineDC;
    }
}
