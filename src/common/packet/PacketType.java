package common.packet;

public enum PacketType {
    JOIN,        // Khi người chơi mới kết nối
    START,       // Server báo đủ 2 người, trận đấu bắt đầu
    MOVE,        // Gửi tọa độ nước đi
    UPDATE,      // Server gửi lại vị trí vừa đánh cho cả 2 máy
    RESULT,      // Thông báo thắng/thua/hòa
    MESSAGE      // Thông báo hệ thống (ví dụ: đối thủ đã thoát)
}
