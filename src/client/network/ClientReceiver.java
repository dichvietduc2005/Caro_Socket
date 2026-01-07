package client.network;

import client.controller.ClientController;
import common.packet.Packet;

public class ClientReceiver implements Runnable {
    private ClientController controller;
    private volatile boolean isRunning = true;

    public ClientReceiver(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        while (isRunning) {
            Packet packet = ClientSocket.getInstance().receive();
            if (packet != null) {
                controller.onPacketReceived(packet);
            } else {
                // Packet null nghĩa là mất kết nối hoặc lỗi
                System.out.println("Server disconnected.");
                stop();
            }
        }
    }

    public void stop() {
        isRunning = false;
        ClientSocket.getInstance().close();
    }
}
