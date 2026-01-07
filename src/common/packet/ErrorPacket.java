package common.packet;

public class ErrorPacket extends Packet {
    private String errorMessage;

    public ErrorPacket(String errorMessage) {
        super(PacketType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
