package server;

import common.packet.*;
import java.io.*;
import java.net.*;

/**
 * MockServer: Server giả lập để bạn tự test Client.
 * - Nhận JoinPacket -> Gửi StartPacket (Vào game)
 * - Nhận MovePacket -> Gửi lại đúng MovePacket đó (Hiện nước đi lên bàn cờ)
 */
public class MockServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("=== MOCK SERVER ĐANG CHẠY CỔNG 1234 ===");
            System.out.println("Dùng để test giao diện và kết nối Client");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client đã kết nối: " + socket.getInetAddress());

                // Xử lý mỗi client trong luồng riêng để không chặn client khác (nếu test nhiều
                // cái)
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Object packet = in.readObject();

                if (packet instanceof JoinPacket) {
                    JoinPacket jp = (JoinPacket) packet;
                    System.out.println("-> Nhận JOIN: " + jp.getPlayerName());

                    // Giả lập tìm thấy đối thủ ngay lập tức
                    StartPacket start = new StartPacket("Bot_Doi_Thu", "X");
                    out.writeObject(start);
                    out.flush();
                    System.out.println("<- Gửi START (Bạn là X)");

                } else if (packet instanceof MovePacket) {
                    MovePacket mp = (MovePacket) packet;
                    System.out.println("-> Nhận MOVE: " + mp.getX() + ", " + mp.getY());

                    // Server xác nhận nước đi hợp lệ -> Gửi lại cho Client cập nhật UI
                    out.writeObject(mp);
                    out.flush();
                    System.out.println("<- Echo MOVE về Client");

                    // (Optional) Bot đánh lại ngẫu nhiên nếu muốn, nhưng để test UI thì echo là đủ
                }
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Client đã ngắt kết nối.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
