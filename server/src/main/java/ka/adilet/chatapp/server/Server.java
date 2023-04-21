package ka.adilet.chatapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

public class Server {
    private static String DB_USERNAME = "postgres";
    private static String DB_PASSWORD = "admin";
    private static String DB_URL = "jdbc:postgresql://localhost:5432/";
    private static String DB_NAME = "ChatAppDB";

    private ServerSocket socket;
    private boolean isRunning = true;

    public void start(int port) throws IOException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        socket = new ServerSocket(port);
        int cnt = 0;
        while (isRunning) {
            ClientHandler ch = new ClientHandler(socket.accept());
            ch.setName("Client handler #" + (++cnt));
            ch.start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedWriter out;
        private BufferedReader in;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private ObjectMapper jsonMapper = new ObjectMapper();

        private static ConcurrentHashMap<Integer, ClientHandler> clients;

        public ClientHandler(Socket socket) {
            clientSocket = socket;
        }

        private void initWritersReaders() throws IOException {
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream((clientSocket.getInputStream()));
        }

        private void close() {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendTextMessage(String text) {
            try {
                out.write(text + "\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage(CommunicationMessage cm) {
            try {
                outputStream.writeObject(cm);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleLogin(CommunicationMessage cm) {
            System.out.println(cm.getBody());
            CommunicationMessage message = new CommunicationMessage(
                    MessageType.AUTHORIZATION_RESULT,
                    "{\"status\": \"OK\"}");
            sendMessage(message);
        }

        private void handleRegister(CommunicationMessage cm) {
            addUserToDB(cm.getBody());
            CommunicationMessage message = new CommunicationMessage(
                    MessageType.AUTHORIZATION_RESULT,
                    "{\"status\": \"OK\"}");
            sendMessage(message);
        }

        private void getUserFromDB() {
            // TODO: 19.04.2023
        }

        private void addUserToDB(String data) {
            JsonNode user;
            Connection conn;
            try {
                user = jsonMapper.readTree(data);
                conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USERNAME, DB_PASSWORD);
                Statement st = conn.createStatement();
                String sqlStmt = String.format("INSERT INTO \"User\" " +
                                "(\"phone_number\", \"name\", \"surname\", \"password\") " +
                                "VALUES ('%s', '%s', '%s', '%s');",
                        user.get("phone_number").asText(),
                        user.get("name").asText(),
                        user.get("surname").asText(),
                        user.get("password").asText());
                st.execute(sqlStmt);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                initWritersReaders();
                while (!clientSocket.isClosed()) {
                    CommunicationMessage clientMessage;
                    try {
                        clientMessage = (CommunicationMessage)inputStream.readObject();
                    } catch (EOFException | SocketException e) {
                        System.out.println("Client disconnected");
                        break;
                    }
                    if (clientMessage.getType() == MessageType.LOGIN) {
                        handleLogin(clientMessage);
                    }
                    else if (clientMessage.getType() == MessageType.REGISTER) {
                        handleRegister(clientMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(1234);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}