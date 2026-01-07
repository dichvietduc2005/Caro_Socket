package common.packet;

public class StartPacket extends Packet {
    private String opponentName;
    private String yourSymbol; // "X" hoặc "O"

    public StartPacket(String opponentName, String yourSymbol) {
        super(PacketType.START);
        this.opponentName = opponentName;
        this.yourSymbol = yourSymbol;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public String getYourSymbol() {
        return yourSymbol;
    }
}
