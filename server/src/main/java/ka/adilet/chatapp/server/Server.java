package ka.adilet.chatapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

public class Server {
    public static String DB_USERNAME = "postgres";
    public static String DB_PASSWORD = "admin";
    public static String DB_URL = "jdbc:postgresql://localhost:5432/";
    public static String DB_NAME = "ChatAppDB";

    private ServerSocket socket;
    private boolean isRunning = true;
    private static ConcurrentHashMap<Long, ClientHandler> clients = new ConcurrentHashMap<>();

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
        private final Socket clientSocket;
        private BufferedWriter out;
        private BufferedReader in;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        private final Connection conn;
        private Long client_id;

        public ClientHandler(Socket socket) {
            try {
                conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USERNAME, DB_PASSWORD);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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

        private void sendMessage(CommunicationMessage cm) {
            try {
                outputStream.writeObject(cm);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean validateUserData(ObjectNode user, String password) {
            if (user == null)  {
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"no user found\"}"));
                return false;
            }
            if (!user.get("password").asText().equals(password)) {
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"incorrect password\"}"));
                return false;
            }
            return true;
        }

        private ArrayNode getAsArrayNode(ResultSet res) throws SQLException {
            ResultSetMetaData rsmd = res.getMetaData();
            ArrayNode rows = jsonMapper.createArrayNode();
            while (res.next()) {
                ObjectNode node = jsonMapper.createObjectNode();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    node.put(rsmd.getColumnName(i), res.getString(rsmd.getColumnName(i)));
                }
                rows.add(node);
            }
            return (rows.isEmpty()) ? null : rows;
        }

        private ArrayNode getChatsByUserId(Long userId) throws SQLException {
            Statement st = conn.createStatement();
            ArrayNode chatIds = getAsArrayNode(st.executeQuery(
                    String.format("SELECT chat_room_id FROM \"ChatRoomMember\"  WHERE member_id = %s", userId)));
            ArrayNode chats = jsonMapper.createArrayNode();
            for (JsonNode c_id : chatIds) {
                Long chatId = c_id.get("chat_room_id").asLong();
                chats.add(getAsArrayNode(
                        st.executeQuery("SELECT * FROM \"ChatRoom\" WHERE id=" + chatId)).get(0));
            }
            for (int i = 0; i < chats.size(); i++) {
                Long chatId = chats.get(i).get("id").asLong();
                ObjectNode chatObjectNode = (ObjectNode)chats.get(i);
                chatObjectNode.set("messages", getMessagesByChatId(chatId));
                if (chatObjectNode.get("is_private").asText() == "f") {
                    chatObjectNode.put("is_private", "false");
                } else {
                    chatObjectNode.put("is_private", "true");
                }
                chats.set(i, chatObjectNode);
            }
            return chats;
        }

        private ArrayNode getMessagesByChatId(Long chatId) throws SQLException {
            Statement st = conn.createStatement();
            return getAsArrayNode(st.executeQuery("SELECT * FROM \"Message\" WHERE chat_room_id = " + chatId));
        }

        private ResultSet getUserData(String phoneNumber) throws SQLException {
            Statement st = conn.createStatement();
            return st.executeQuery(
                    String.format(String.format("select * from \"User\" where \"phone_number\"='%s'", phoneNumber)));
        }

        private Timestamp convertStrDate(String date) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return new Timestamp(dateFormat.parse(date).getTime());
            } catch (ParseException e) {
                System.err.println(e);
                return null;
            }
        }

        private void saveChatMessageToDB(ObjectNode messageNode) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"Message\" " +
                            "(content, sender_id, chat_room_id, sent_time) " +
                            "VALUES (?, ?, ?, ?)");
            pstmt.setString(1, messageNode.get("content").asText());
            pstmt.setLong(2, messageNode.get("sender_id").asLong());
            pstmt.setLong(3, messageNode.get("chat_room_id").asLong());
            pstmt.setTimestamp(4, convertStrDate(messageNode.get("sent_time").asText()));
            pstmt.executeUpdate();
        }

        private void addUserToDB(String data) {
            JsonNode user;
            Connection conn;
            try {
                user = jsonMapper.readTree(data);
                System.out.println(user);
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

        private void deliverChatMessage(ObjectNode messageNode) {
            Long chatRoomId = messageNode.get("chat_room_id").asLong();
            String sql = String.format(
                    "SELECT\n" +
                            "    id, name, surname\n" +
                            "FROM\n" +
                            "    \"User\"\n" +
                            "WHERE\n" +
                            "    id in (SELECT member_id FROM \"ChatRoomMember\" WHERE chat_room_id = %d);",
                    chatRoomId
                    );
            try {
                ArrayNode users =  getAsArrayNode(conn.prepareStatement(sql).executeQuery());
                System.out.println("Active clients count: " + clients.size() + ": ");
                for (JsonNode user : users) {
                    Long userId = user.get("id").asLong();
                    if (!clients.containsKey(userId)) continue;
                    if (clients.get(userId) == this) continue;
                    System.out.println("User id: " + userId);
                    clients.get(userId).sendMessage(new CommunicationMessage(
                            MessageType.CHAT,
                            messageNode.toString()
                    ));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void handleRegister(CommunicationMessage cm) {
            System.out.printf("%s: register request\n", getName());
            addUserToDB(cm.getBody());
            sendMessage(new CommunicationMessage(
                    MessageType.AUTHORIZATION_RESULT,
                    "{\"result\": \"OK\"}"));
        }

        private void handleChat(CommunicationMessage cm) {
            System.out.printf("%s: chat request from %d\n", getName(), this.client_id);
            try {
                ObjectNode messageNode = (ObjectNode) jsonMapper.readTree(cm.getBody());
                saveChatMessageToDB(messageNode);
                deliverChatMessage(messageNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void handleLogin(CommunicationMessage cm) {
            System.out.printf("%s: login request\n", getName());
            try {
                String password = jsonMapper.readTree(cm.getBody()).get("password").asText();
                String phoneNumber = jsonMapper.readTree(cm.getBody()).get("phone_number").asText();
                ArrayNode users = getAsArrayNode(getUserData(phoneNumber));
                ObjectNode userNode = (users == null) ? null : (ObjectNode)users.get(0);
                if (!validateUserData(userNode, password)) return;
                ObjectNode response = jsonMapper.createObjectNode();
                response.put("result", "OK");
                response.set("user", userNode);
                response.set("chats", getChatsByUserId(userNode.get("id").asLong()));
                clients.put(userNode.get("id").asLong(), this);
                this.client_id = userNode.get("id").asLong();
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        response.toString()));
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
                        System.out.printf("%s disconnected\n", getName());
                        clients.remove(this.client_id);
                        break;
                    }
                    switch (clientMessage.getType()) {
                        case LOGIN -> handleLogin(clientMessage);
                        case REGISTER -> handleRegister(clientMessage);
                        case CHAT -> handleChat(clientMessage);
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