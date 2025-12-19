package common.packet;

import java.io.Serializable;

// Serializable là bắt buộc để gửi đối tượng qua Socket
public abstract class Packet implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo đồng bộ giữa Client và Server
    private PacketType type;

    public Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }
}