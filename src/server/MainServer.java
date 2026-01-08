package server;

import server.network.ServerSocketManager;

public class MainServer {
    public static void main(String[] args) {
        // Khởi động server quản lý socket trên port 12345 (hoặc port bạn chọn)
        int port = 12345;
        ServerSocketManager serverManager = new ServerSocketManager(port);
        serverManager.startServer();
    }
}