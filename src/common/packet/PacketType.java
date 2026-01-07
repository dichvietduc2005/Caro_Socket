package common.packet;

public enum PacketType {
    JOIN, // Khi người chơi mới kết nối
    START, // Server báo đủ 2 người, trận đấu bắt đầu
    MOVE, // Gửi tọa độ nước đi (Single move)
    UPDATE, // Đồng bộ toàn bộ bàn cờ (Full board sync)
    RESULT, // Thông báo thắng/thua/hòa
    MESSAGE, // Thông báo hệ thống (ví dụ: đối thủ đã thoát)
    ERROR // Thông báo lỗi
}
