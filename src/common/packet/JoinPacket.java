package common.packet;

public class JoinPacket extends Packet {
    private String playerName;

    public JoinPacket(String playerName) {
        super(PacketType.JOIN);
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}