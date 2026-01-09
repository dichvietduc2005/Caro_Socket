package common.packet;

/**
 * Packet gửi từ Client khi yêu cầu cầu hòa
 */
public class DrawRequestPacket extends Packet {
    public DrawRequestPacket() {
        super(PacketType.DRAW_REQUEST);
    }
}
