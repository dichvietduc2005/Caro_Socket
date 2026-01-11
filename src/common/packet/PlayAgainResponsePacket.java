package common.packet;
public class PlayAgainResponsePacket extends Packet {
    private boolean accepted;
    public PlayAgainResponsePacket(boolean accepted) {
        super(PacketType.PLAY_AGAIN_RESPONSE);
        this.accepted = accepted;
    }
    public boolean isAccepted() { return accepted; }
}