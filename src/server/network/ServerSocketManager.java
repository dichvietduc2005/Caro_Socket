package server.network;

import server.game.GameRoom;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ServerSocketManager {
    private int port;
    private ClientHandler waitingClient = null; // Người chơi đang chờ ghép cặp

    public ServerSocketManager(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            while (true) {
                // Chấp nhận kết nối mới
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                // Tạo Handler cho client này
                ClientHandler clientHandler = new ClientHandler(socket);
                
                // Logic ghép cặp (Matchmaking)
                matchMake(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void matchMake(ClientHandler clientHandler) {
        if (waitingClient == null) {
            // Chưa có ai chờ, client này sẽ chờ
            waitingClient = clientHandler;
            waitingClient.sendMessage("Waiting for another player...");
        } else {
            // Đã có người chờ, ghép cặp ngay
            String roomId = UUID.randomUUID().toString();
            System.out.println("Creating room " + roomId);

            // Tạo phòng chơi mới: waitingClient là Player 1 (X), clientHandler là Player 2 (O)
            GameRoom room = new GameRoom(roomId, waitingClient, clientHandler);
            
            // Gán phòng cho cả 2 client
            waitingClient.setGameRoom(room);
            clientHandler.setGameRoom(room);

            // Bắt đầu luồng lắng nghe của cả 2
            new Thread(waitingClient).start();
            new Thread(clientHandler).start();

            // Khởi động game
            room.startGame();

            // Reset người chờ
            waitingClient = null;
        }
    }
}