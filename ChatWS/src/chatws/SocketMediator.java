package chatws;

import java.io.IOException;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import weblogic.websocket.ClosingMessage;
import weblogic.websocket.WebSocketAdapter;
import weblogic.websocket.WebSocketConnection;
import weblogic.websocket.WebSocketListener;
import weblogic.websocket.annotation.WebSocket;


@WebSocket (
    pathPatterns = {"/chat"},
    timeout = 600,
    maxConnections = 1000)

public class SocketMediator extends WebSocketAdapter implements WebSocketListener {
    
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();

    public SocketMediator() {
        super();
    }
    
    @Override
    public void onMessage(WebSocketConnection connection, String msg) {
        // Determine operation
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> msgData = mapper.readValue(msg, Map.class);
            String operation = msgData.get("operation");
            // Get User
            int userId = connection.hashCode();
            User user = users.get(userId);            
            switch (operation) {
                case "register":
                    String username = msgData.get("username");
                    user.setUsername(username);
                    this.systemBroadcast(username + " has joined the chat", userId);
                    break;
                case "message":
                    String message = msgData.get("message");
                    for(WebSocketConnection conn : getWebSocketContext().getWebSocketConnections()) {
                        conn.send("{" +
                            "\"message\": \"" + message + "\", " +
                            "\"user\": \"" + user.getUsername() + "\", " +
                            "\"colour\": \"" + user.getColour() + "\"" +
                        "}");
                    }
                    break;
            }
        } catch (Exception e) {
            System.out.println("Exception");
        }
    }
    
    @Override public void onOpen(WebSocketConnection connection) {
        int userId = connection.hashCode();
        User user = new User(userId);
        users.put(userId, user);

        System.out.println("Client connection open");
    }
    
    public void systemBroadcast (String message, Integer userId) {
        try {
            for(WebSocketConnection conn : getWebSocketContext().getWebSocketConnections()) {
                if (conn.hashCode() != userId) {
                    conn.send("{\"type\":\"system\", \"message\":\"" + message + "\"}");
                }
            }
        } catch (IOException e) {
            System.out.println("Broadcast Exception");
        }
    }
    
    @Override public void onClose(WebSocketConnection connection, ClosingMessage closingMessage) {
        int userId = connection.hashCode();
        User user = users.get(userId);
        String username = user.getUsername();
        this.systemBroadcast(username + " has left the chat", userId);
        users.remove(userId);
    }
    
    @Override public void onTimeout(WebSocketConnection connection) {
        int userId = connection.hashCode();
        User user = users.get(userId);
        String username = user.getUsername();
        this.systemBroadcast(username + " has left the chat", userId);
        users.remove(userId);
    }
}
