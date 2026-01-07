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
            if (socket != null && !socket.isClosed()) {
                return true;
            }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Packet receive() {
        if (socket == null || socket.isClosed()) return null;
        try {
            return (Packet) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // e.printStackTrace();
            close();
            return null;
        }
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }
}
