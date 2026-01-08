package common.packet;

public class MovePacket extends Packet {
    private int x;
    private int y;

    public MovePacket(int x, int y) {
        super(PacketType.MOVE);
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}