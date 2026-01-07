package client.controller;

import client.model.GameState;
import client.ui.CaroClientFrame;

public class ClientController {

    private final GameState gameState;
    private final CaroClientFrame frame;

    public ClientController() {
        this.gameState = new GameState();
        this.frame = new CaroClientFrame(this, gameState);
    }

    public void showUI() {
        frame.setVisible(true);
    }

    public void onPlayNow(String playerName) {
        String p1 = (playerName == null || playerName.trim().isEmpty())
                ? "Player 1"
                : playerName.trim();
        String p2 = "Đối thủ";
        gameState.resetForNewGame(p1, p2);
        frame.initGameScreenFromState();
        frame.showGameScreen();
    }

    public void onLocalCellClicked(int row, int col) {
        if (gameState.isGameOver()) {
            return;
        }
        if (!gameState.isLocalPlayersTurn()) {
            return;
        }
        if (!gameState.canPlace(row, col)) {
            return;
        }

        gameState.placeMove(row, col);
        frame.updateBoardCell(row, col, gameState.getSymbolAt(row, col), true);

        int result = gameState.checkWin(row, col);
        if (result != GameState.RESULT_NONE) {
            gameState.setGameOver(true);
            frame.handleGameResult(result);
            return;
        }

        gameState.switchTurn();
        frame.updateTurnHighlight();
    }

    public void onTimeoutForCurrentPlayer() {
        gameState.setGameOver(true);
        int result = gameState.isPlayer1Turn()
                ? GameState.RESULT_PLAYER2_WIN
                : GameState.RESULT_PLAYER1_WIN;
        frame.handleGameResult(result);
    }
}
