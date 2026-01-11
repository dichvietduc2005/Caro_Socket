package server.network;

import common.packet.*;
import server.game.GameRoom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameRoom gameRoom;
    private ServerSocketManager serverManager;
    private int playerID; // 1 hoặc 2 (được set bởi GameRoom)
    private String playerName = "Player"; // Tên người chơi (nhận từ JoinPacket)
    private boolean nameSet = false; // Cờ để kiểm tra đã nhận JoinPacket chưa

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            // Lưu ý: Tạo OutputStream trước InputStream để tránh deadlock
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public void setServerManager(ServerSocketManager serverManager) {
        this.serverManager = serverManager;
    }

    public void setPlayerID(int id) {
        this.playerID = id;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isNameSet() {
        return nameSet;
    }

    // Gửi packet xuống Client
    public synchronized void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
            out.reset(); // Tránh cache object cũ
        } catch (IOException e) {
            System.out.println("Error sending packet to player " + playerID);
        }
    }

    // Gửi thông báo dạng chuỗi (dùng MessagePacket nếu có, hoặc custom)
    public void sendMessage(String msg) {
        // Gửi MessagePacket về client với isSystemMessage = true
        sendPacket(new MessagePacket(msg, true));
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof Packet) {
                    Packet packet = (Packet) obj;
                    handlePacket(packet);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected.");
            if (gameRoom != null) {
                gameRoom.handleDisconnect(this);
            }
        }
    }

    private void handlePacket(Packet packet) {
        PacketType type = packet.getType();

        // Xử lý JoinPacket
        if (type == PacketType.JOIN) {
            if (packet instanceof JoinPacket) {
                JoinPacket joinPacket = (JoinPacket) packet;
                this.playerName = joinPacket.getPlayerName();
                this.nameSet = true;
                System.out.println("Player joined: " + playerName);
                if (serverManager != null) {
                    serverManager.onClientJoinPacketReceived(this);
                }
            }
            return;
        }

        // Xử lý MovePacket
        if (type == PacketType.MOVE) {
            if (packet instanceof MovePacket) {
                MovePacket move = (MovePacket) packet;
                gameRoom.processMove(this, move.getX(), move.getY());
            }
            return;
        }

        // Xử lý SurrenderPacket
        if (type == PacketType.SURRENDER) {
            if (gameRoom != null) {
                gameRoom.handleSurrender(this);
            }
            return;
        }

        // Xử lý DrawRequestPacket
        if (type == PacketType.DRAW_REQUEST) {
            if (gameRoom != null) {
                gameRoom.handleDrawRequest(this);
            }
            return;
        }

        // Xử lý DrawResponsePacket
        if (type == PacketType.DRAW_RESPONSE) {
            if (packet instanceof DrawResponsePacket && gameRoom != null) {
                DrawResponsePacket response = (DrawResponsePacket) packet;
                gameRoom.handleDrawResponse(this, response.isAccepted());
            }
            return;
        }

        // --- CẬP NHẬT MỚI: Xử lý Yêu cầu Chơi lại ---
        if (type == PacketType.PLAY_AGAIN_REQUEST) {
            if (gameRoom != null) {
                gameRoom.handlePlayAgainRequest(this);
            }
            return;
        }

        if (type == PacketType.PLAY_AGAIN_RESPONSE) {
            if (packet instanceof PlayAgainResponsePacket && gameRoom != null) {
                PlayAgainResponsePacket response = (PlayAgainResponsePacket) packet;
                gameRoom.handlePlayAgainResponse(this, response.isAccepted());
            }
            return;
        }
        // ---------------------------------------------
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}