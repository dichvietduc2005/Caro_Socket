package common.packet;

public class MovePacket extends Packet {
    private int x;
    private int y;
    private String playerSymbol; // "X" hoặc "O"

    public MovePacket(int x, int y, String playerSymbol) {
        super(PacketType.MOVE); // Gắn nhãn là MOVE
        this.x = x;
        this.y = y;
        this.playerSymbol = playerSymbol;
    }

    // Getter và Setter cho x, y, playerSymbol...
    public int getX() { return x; }
    public int getY() { return y; }
    public String getPlayerSymbol() { return playerSymbol; }
}