package client.network;

import common.config.GameConfig;
import common.packet.Packet;
import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocket() {}

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public boolean connect() {
        try {
            // Nếu socket còn sống thì dùng lại
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                return true;
            }
            // Tạo mới hoàn toàn
            socket = new Socket(GameConfig.SERVER_IP, GameConfig.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server: " + socket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void send(Packet packet) {
        if (socket == null || socket.isClosed()) return;
        try {
            out.writeObject(packet);
            out.flush();
            out.reset(); // QUAN TRỌNG: Thêm reset() để tránh lỗi cache object khi gửi nhiều lần
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Packet receive() {
        if (socket == null || socket.isClosed()) return null;
        try {
            return (Packet) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Khi socket đóng, hàm này sẽ ném lỗi -> Trả về null để Receiver dừng lại
            close();
            return null;
        }
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // QUAN TRỌNG: Reset về null để lần sau connect() sẽ new lại từ đầu
            socket = null;
            out = null;
            in = null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}