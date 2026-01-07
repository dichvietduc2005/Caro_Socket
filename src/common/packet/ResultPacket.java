package common.packet;

public class ResultPacket extends Packet {
    // 1: Bạn thắng, 2: Bạn thua, 3: Hòa, 4: Đối thủ thoát
    private int resultCode;
    private String message;

    public ResultPacket(int resultCode, String message) {
        super(PacketType.RESULT);
        this.resultCode = resultCode;
        this.message = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }
}