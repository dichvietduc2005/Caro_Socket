package common.packet;

public class MessagePacket extends Packet {
    private String message;
    private boolean isSystemMessage;

    public MessagePacket(String message, boolean isSystemMessage) {
        super(PacketType.MESSAGE);
        this.message = message;
        this.isSystemMessage = isSystemMessage;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSystemMessage() {
        return isSystemMessage;
    }
}
