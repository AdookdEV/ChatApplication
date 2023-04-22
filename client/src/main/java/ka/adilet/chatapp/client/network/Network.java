package ka.adilet.chatapp.client.network;


import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.io.*;
import java.net.Socket;

public class Network{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread thread;

    public Network(String address, int port) {
        try {
            this.socket = new Socket(address, port);
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(CommunicationMessage message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("[ERROR] Couldn't send message");
            System.err.println(e);
        }
    }

    public CommunicationMessage listen() {
        CommunicationMessage res=null;
        try {
            while (socket.isConnected()) {
                res = (CommunicationMessage)inputStream.readObject();
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public void stopConnection() {
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Network network = new Network("localhost", 1234);
        String data = "";
        CommunicationMessage message = new CommunicationMessage(MessageType.LOGIN, data);
        network.sendMessage(message);
        network.listen();
        network.stopConnection();
    }
}
