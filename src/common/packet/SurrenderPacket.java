package common.packet;

/**
 * Packet gửi từ Client khi xin thua
 */
public class SurrenderPacket extends Packet {
    public SurrenderPacket() {
        super(PacketType.SURRENDER);
    }
}
