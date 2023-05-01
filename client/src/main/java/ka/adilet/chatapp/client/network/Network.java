package ka.adilet.chatapp.client.network;


import javafx.beans.property.SimpleBooleanProperty;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class Network {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final String address;
    private final int port;
    private boolean tryToReconnect = true;
    private final Thread heartbeatThread;
    private final long heartbeatDelayMillis = 1000;
    public final SimpleBooleanProperty isConnected = new SimpleBooleanProperty(true);

    public Network(String address, int port) {
        this.port = port;
        this.address = address;
        isConnected.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("[INFO] Connected to server");
            }
        });
        heartbeatThread = new Thread(() -> {
            while (tryToReconnect) {
                //send a test signal
                try {
                    outputStream.writeObject(new CommunicationMessage(MessageType.CHECK_CONNECTION, ""));
                    outputStream.flush();
                    Thread.sleep(heartbeatDelayMillis);
                } catch (InterruptedException e) {
                     tryToReconnect = false;
                } catch (IOException | NullPointerException e) {
                    isConnected.set(false);
                    try {
                        Thread.sleep(heartbeatDelayMillis);
                        System.err.println("[ERROR] Couldn't connect to server. Trying again...");
                        connect(address, port);
                    } catch (IOException ignored) {

                    } catch (InterruptedException ex) {
                        tryToReconnect = false;
                    }
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    public void connect(String address, int port) throws IOException {
        try {
            this.socket = new Socket(address, port);
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            isConnected.set(true);
        } catch (UnknownHostException e) {
            System.err.println("Unknown Host");
        }
    }

    public void sendMessage(CommunicationMessage message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("[ERROR] Couldn't send message");
        }
    }

    public CommunicationMessage listen() {
        try {
            CommunicationMessage res;
            res = (CommunicationMessage)inputStream.readObject();
            return res;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERROR] No connection with server");
        }
        return null;
    }

    public void stopConnection() {
        tryToReconnect = false;
        if (this.socket == null) return;
        try {
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Network network = new Network("localhost", 1234);
        String data = "";
        CommunicationMessage message = new CommunicationMessage(MessageType.LOGIN, data);
        network.sendMessage(message);
        network.listen();
        network.stopConnection();
    }
}
