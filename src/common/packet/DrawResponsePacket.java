package common.packet;

/**
 * Packet phản hồi yêu cầu cầu hòa (accept hoặc reject)
 */
public class DrawResponsePacket extends Packet {
    private boolean accepted;

    public DrawResponsePacket(boolean accepted) {
        super(PacketType.DRAW_RESPONSE);
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
