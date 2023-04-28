package ka.adilet.chatapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private ServerSocket socket;
    private boolean isRunning = true;
    private static final ConcurrentHashMap<Long, ClientHandler> clients = new ConcurrentHashMap<>();

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
        private Connection conn;
        private Long client_id;
        private final DAO db;

        public ClientHandler(Socket socket) {
            db = new DAO();
            conn = db.getConnection();
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

        private boolean validateUserData(JsonNode user, String password) {
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
            if (chatIds == null) return null;
            ArrayNode chats = jsonMapper.createArrayNode();
            for (JsonNode c_id : chatIds) {
                long chatId = c_id.get("chat_room_id").asLong();
                chats.add(getAsArrayNode(
                        st.executeQuery("SELECT * FROM \"ChatRoom\" WHERE id=" + chatId)).get(0));
            }
            for (int i = 0; i < chats.size(); i++) {
                long chatId = chats.get(i).get("id").asLong();
                ObjectNode chatObjectNode = (ObjectNode)chats.get(i);
                chatObjectNode.set("messages", getMessagesByChatId(chatId));
                if (chatObjectNode.get("is_private").asText().equals("f")) {
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
            String sql = String.format(
                    """
                            SELECT
                                m.id as id,
                            content,    sender_id,
                                chat_room_id,
                                sent_time,
                                concat(u.name, ' ', u.surname) as sender_name
                            FROM "Message" m INNER JOIN "User" u
                                     ON m.sender_id = u.id
                                     WHERE chat_room_id = %d;""",
                    chatId
            );
            return getAsArrayNode(st.executeQuery(sql));
        }

        private void deliverChatMessage(ObjectNode messageNode) {
            Long chatRoomId = messageNode.get("chat_room_id").asLong();
            String sql = String.format(
                        """
                            SELECT
                                id, name, surname
                            FROM
                                "User"
                            WHERE
                                id in (SELECT member_id FROM "ChatRoomMember" WHERE chat_room_id = %d);
                        """,
                    chatRoomId
                    );
            try {
                ArrayNode users =  getAsArrayNode(conn.prepareStatement(sql).executeQuery());
                System.out.println("Active clients count: " + clients.size() + ": ");
                assert users != null;
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

        private void handleGetUsers(CommunicationMessage cm) throws IOException, SQLException {
            System.out.printf("%s: get users request\n", getName());
            JsonNode jsonData = jsonMapper.readTree(cm.getBody());

            ArrayList<Long> ids = new ArrayList<>();
            boolean all = jsonData.get("all").asBoolean();
            for (JsonNode id : jsonData.get("ids")) {ids.add(id.asLong());}

            ObjectNode response = jsonMapper.createObjectNode();
            response.put("result", "OK");
            response.set("users",  db.getUsers(ids, all));
            sendMessage(new CommunicationMessage(MessageType.GET_USERS, response.toString()));
        }

        private void handleRegister(CommunicationMessage cm) throws JsonProcessingException {
            System.out.printf("%s: register request\n", getName());
            JsonNode userData = jsonMapper.readTree(cm.getBody());

            // Check if the user has already registered;
            if (db.getUserByPhone(userData.get("phone_number").asText()) != null) {
                sendMessage(new CommunicationMessage(
                        MessageType.AUTHORIZATION_RESULT,
                        "{\"result\": \"A user with this number already exists\"}"));
                return;
            }
            System.out.printf("%s: registered new user: %s\n", getName(), userData);
            db.addUser(userData);
            sendMessage(new CommunicationMessage(
                    MessageType.AUTHORIZATION_RESULT,
                    "{\"result\": \"OK\"}"));
        }

        private void handleChat(CommunicationMessage cm) throws SQLException, JsonProcessingException {
            System.out.printf("%s: chat request from user %d\n", getName(), this.client_id);
            ObjectNode messageNode = (ObjectNode) jsonMapper.readTree(cm.getBody());
            db.addMessage(messageNode);
            deliverChatMessage(messageNode);
        }

        private void handleNewChat(CommunicationMessage cm) throws JsonProcessingException {
            System.out.printf("%s: new chat request from %d\n", getName(), this.client_id);
            Long chat_id = db.addChat(jsonMapper.readTree(cm.getBody()));
            Boolean is_private = jsonMapper.readTree(cm.getBody()).get("is_private").asBoolean();
            ObjectNode response = jsonMapper.createObjectNode();
            ObjectNode chat = jsonMapper.createObjectNode();
            chat.put("id", chat_id);
            chat.put("is_private", is_private);
            chat.put("name", jsonMapper.readTree(cm.getBody()).get("name").asText());
            response.put("result", "OK");
            response.set("chat", chat);
            sendMessage(new CommunicationMessage(MessageType.NEW_CHAT, response.toString()));
            System.out.printf("%s: chat was created\n", getName());
        }

        private void handleLogin(CommunicationMessage cm) {
            System.out.printf("%s: login request\n", getName());
            try {
                String password = jsonMapper.readTree(cm.getBody()).get("password").asText();
                String phoneNumber = jsonMapper.readTree(cm.getBody()).get("phone_number").asText();

                JsonNode userNode = db.getUserByPhone(phoneNumber);
                System.out.println(userNode);
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
                        if (this.client_id != null) {
                            clients.remove(this.client_id);
                        }
                        break;
                    }
                    switch (clientMessage.getType()) {
                        case LOGIN -> handleLogin(clientMessage);
                        case REGISTER -> handleRegister(clientMessage);
                        case CHAT -> handleChat(clientMessage);
                        case GET_USERS -> handleGetUsers(clientMessage);
                        case NEW_CHAT -> handleNewChat(clientMessage);
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