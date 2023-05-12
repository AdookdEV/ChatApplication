package ka.adilet.chatapp.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class DAO {
    private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "admin";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "ChatAppDB";
    private static final String DRIVER_URL = DB_URL + DB_NAME;
    private final Connection conn;

    public DAO() {
        try {
            conn = DriverManager.getConnection(DRIVER_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode getAsArrayNode(ResultSet res) throws SQLException {
        ResultSetMetaData rsmd = res.getMetaData();
        ArrayNode rows = jsonMapper.createArrayNode();
        while (res.next()) {
            ObjectNode node = jsonMapper.createObjectNode();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (rsmd.getColumnTypeName(i) == "bool") {
                    node.put(rsmd.getColumnName(i), res.getBoolean(i));
                }
                else {
                    node.put(rsmd.getColumnName(i), res.getString(rsmd.getColumnName(i)));
                }
            }
            rows.add(node);
        }
        return rows;
    }

    public ArrayNode getChats(ArrayNode ids) {
        try {
            String sql = String.format("SELECT * FROM \"ChatRoom\" WHERE id in %s", ids.toString()
                    .replace('[', '(')
                    .replace(']', ')')
                    .replace("()", "(-1)"));
            Statement stmt = conn.createStatement();
            return getAsArrayNode((stmt.executeQuery(sql)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode getChat(Long id) {
        try {
            String sql = String.format("SELECT * FROM \"ChatRoom\" WHERE id = %s", id);
            Statement stmt = conn.createStatement();
            return getAsArrayNode((stmt.executeQuery(sql))).get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode getChatsByUserId(Long userId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM \"ChatRoom\" " +
                    "WHERE id in (SELECT chat_room_id FROM \"ChatRoomMember\" c WHERE c.member_id = ?);");
            pstmt.setLong(1, userId);
            return getAsArrayNode(pstmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode getMessagesByChatId(Long chatId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("""
                    SELECT m.id as id,\s
                           content,\s
                           sender_id,\s
                           concat(u.name, ' ', u.surname) sender_name,\s
                           chat_room_id, sent_time \s
                    FROM "Message" m INNER JOIN "User" u ON m.sender_id = u.id WHERE chat_room_id = ?;""");
            pstmt.setLong(1, chatId);
            return getAsArrayNode(pstmt.executeQuery());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode getChatMembers(Long chatRoomId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("""
                            SELECT
                                id, name, surname
                            FROM
                                "User"
                            WHERE
                                id in (SELECT member_id FROM "ChatRoomMember" WHERE chat_room_id = ?);
                        """);
            pstmt.setLong(1, chatRoomId);
            return getAsArrayNode(pstmt.executeQuery());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode getUsers(ArrayList<Long> ids, boolean all) {
        try {
            String sql = String.format(
                    "SELECT id, CONCAT(name, ' ', surname) as name FROM \"User\" WHERE id in %s OR %b;",
                    ids.toString().replace('[', '(')
                            .replace(']', ')')
                            .replace("()", "(-1)"),
                    all
            );
            PreparedStatement stmt = conn.prepareStatement(sql);
            return getAsArrayNode(stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode getUserByPhone(String phone) {
        try {
            Statement selectStmt = conn.createStatement();
            ResultSet res = selectStmt.executeQuery(
                    String.format(
                            "SELECT * FROM \"User\" WHERE phone_number='%s'",
                            phone));
            return getAsArrayNode(res).get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addUser(JsonNode user) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"User\" (phone_number, name, surname, password) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, user.get("phone_number").asText());
            pstmt.setString(2, user.get("name").asText());
            pstmt.setString(3, user.get("surname").asText());
            pstmt.setString(4, user.get("password").asText());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addChatMembers(ArrayNode members_id, Long chat_id) {
        StringBuilder sql = new StringBuilder(
                "INSERT INTO \"ChatRoomMember\" (chat_room_id, member_id) VALUES ");
        for (int i = 0; i < members_id.size(); i++) {
            sql.append(String.format("(%d,%d)", chat_id, members_id.get(i).asLong()));
            if (i != members_id.size() - 1) sql.append(", ");
            else sql.append(";\n");
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long addChat(JsonNode chat) {
        try {
            String chat_name = chat.get("name").asText();
            if (chat.get("is_private").asBoolean()) {
                PreparedStatement pstmt =  conn.prepareStatement("SELECT id FROM \"ChatRoom\" " +
                        "WHERE name LIKE ? AND name LIKE ? AND is_private;");
                pstmt.setString(1, "%" + chat_name.split(", ")[0] + "%");
                pstmt.setString(2, "%" + chat_name.split(", ")[1] + "%");
                ResultSet res = pstmt.executeQuery();
                if (res.next()) {
                    return res.getLong(1);
                }
            }
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO \"ChatRoom\" (name, is_private) VALUES (?, ?);");
            stmt.setString(1, chat.get("name").asText());
            stmt.setBoolean(2, chat.get("is_private").asBoolean());
            stmt.executeUpdate();
            ResultSet res = conn.createStatement().executeQuery("SELECT MAX(id) as id FROM \"ChatRoom\";");

            if (res.next()) {
                Long chat_id = res.getLong("id");
                addChatMembers((ArrayNode)chat.get("members"), chat_id);
                return chat_id;
            }
            return -1L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Timestamp convertStrDate(String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Timestamp(dateFormat.parse(date).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void addMessage(JsonNode messageNode) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"Message\" " +
                            "(content, sender_id, chat_room_id, sent_time) " +
                            "VALUES (?, ?, ?, ?)");
            pstmt.setString(1, messageNode.get("content").asText());
            pstmt.setLong(2, messageNode.get("sender_id").asLong());
            pstmt.setLong(3, messageNode.get("chat_room_id").asLong());
            pstmt.setTimestamp(4, convertStrDate(messageNode.get("sent_time").asText()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
