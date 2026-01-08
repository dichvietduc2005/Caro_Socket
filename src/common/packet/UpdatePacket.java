package common.packet;

public class UpdatePacket extends Packet {
    private int[][] board;
    private int currentPlayer; // ID của người đến lượt đi tiếp theo

    // Constructor này phải khớp với tham số truyền vào từ GameRoom
    public UpdatePacket(int[][] board, int currentPlayer) {
        super(PacketType.UPDATE);
        this.board = board;
        this.currentPlayer = currentPlayer;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }
}