package ka.adilet.chatapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

public class Server {
    public static String DB_USERNAME = "postgres";
    public static String DB_PASSWORD = "admin";
    public static String DB_URL = "jdbc:postgresql://localhost:5432/";
    public static String DB_NAME = "ChatAppDB";

    private ServerSocket socket;
    private boolean isRunning = true;

    public void start(int port) throws IOException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        socket = new ServerSocket(port);
        int cnt = 0;
        while (isRunning) {
            ClientHandler ch = new ClientHandler(socket.accept());
            ch.setName("Client #" + (++cnt));
            System.out.println(ch.getName() +" connected");
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
            try {
                String password = jsonMapper.readTree(cm.getBody()).get("password").asText();
                String phoneNumber = jsonMapper.readTree(cm.getBody()).get("phone_number").asText();
                Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USERNAME, DB_PASSWORD);
                Statement st = conn.createStatement();
                ResultSet resultSet = st.executeQuery(
                        String.format(String.format("select * from \"User\" where \"phone_number\"='%s'", phoneNumber)));
                ResultSetMetaData rsmd = resultSet.getMetaData();
                // there is no user with provided password
                if (!resultSet.next())  {
                    sendMessage(new CommunicationMessage(
                            MessageType.AUTHORIZATION_RESULT,
                            "{\"result\": \"no user found\"}"));
                    return;
                }
                // password is incorrect
                if (!resultSet.getString("password").equals(password)) {
                    sendMessage(new CommunicationMessage(
                            MessageType.AUTHORIZATION_RESULT,
                            "{\"result\": \"incorrect password\"}"));
                    return;
                }
                // Received data is correct, used is logged in
                ObjectNode userNode = jsonMapper.createObjectNode();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                   userNode.put(rsmd.getColumnName(i), resultSet.getString(rsmd.getColumnName(i)));
                }
                System.out.println(userNode);
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"OK\"}"));
            } catch (Exception e) {
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"server error\"}"));
                throw new RuntimeException(e);
            }
        }

        private void handleRegister(CommunicationMessage cm) {
            addUserToDB(cm.getBody());
            sendMessage(new CommunicationMessage(
                    MessageType.AUTHORIZATION_RESULT,
                    "{\"result\": \"OK\"}"));
        }

        private void addUserToDB(String data) {
            JsonNode user;
            Connection conn;
            try {
                user = jsonMapper.readTree(data);
                conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USERNAME, DB_PASSWORD);


                // Check if the user has already registered;
                Statement selectStmt = conn.createStatement();
                ResultSet res = selectStmt.executeQuery(
                        String.format(
                                "SELECT \"phone_number\" FROM \"User\" WHERE \"phone_number\"='%s'",
                                user.get("phone_number").asText()));
                if (res.next()) {
                    sendMessage(new CommunicationMessage(
                            MessageType.AUTHORIZATION_RESULT,
                            "{\"result\": \"A user with this number already exists\"}"));
                    return;
                }
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO \"User\" " +
                        "(\"phone_number\", \"name\", \"surname\", \"password\") VALUES (?, ?, ?, ?);");
                insertStmt.setString(1, user.get("phone_number").asText());
                insertStmt.setString(2, user.get("name").asText());
                if (user.get("surname").asText().length() > 0) {
                    insertStmt.setString(3, user.get("surname").asText());
                } else {
                    insertStmt.setNull(3, Types.VARCHAR);
                }
                insertStmt.setString(4, user.get("password").asText());
                insertStmt.executeUpdate();
                conn.close();
            } catch (Exception e) {
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"server error\"}"));
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


    public static void main(String[] args) throws SQLException {
        Server server = new Server();
        try {
            server.start(1234);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}