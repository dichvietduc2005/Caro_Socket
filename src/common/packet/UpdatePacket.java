package common.packet;

/**
 * Gói tin dùng để cập nhật lại toàn bộ bàn cờ cho Client
 * (Hữu ích khi reconnect hoặc đồng bộ dữ liệu)
 */
public class UpdatePacket extends Packet {
    private int[][] board;
    private int currentPlayer; // ID của người đến lượt đi tiếp theo

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
