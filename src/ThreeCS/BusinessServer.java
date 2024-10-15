package ThreeCS;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusinessServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/contacts_db";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Server started on port 9999");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                Request request = (Request) in.readObject();
                Response response = handleRequest(request);
                out.writeObject(response);
                out.flush();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private Response handleRequest(Request request) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String message = "";

            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);

                switch (request.getAction()) {
                    case "ADD":
                        stmt = conn.prepareStatement("INSERT INTO contacts (name, address, phone) VALUES (?, ?, ?)");
                        stmt.setString(1, request.getName());
                        stmt.setString(2, request.getAddress());
                        stmt.setString(3, request.getPhone());
                        int rowsAdded = stmt.executeUpdate();
                        message = rowsAdded > 0 ? "Contact added successfully." : "Failed to add contact.";
                        break;

                    case "UPDATE":
                        stmt = conn.prepareStatement("UPDATE contacts SET address = ?, phone = ? WHERE name = ?");
                        stmt.setString(1, request.getAddress());
                        stmt.setString(2, request.getPhone());
                        stmt.setString(3, request.getName());
                        int rowsUpdated = stmt.executeUpdate();
                        message = rowsUpdated > 0 ? "Contact updated successfully." : "Failed to update contact.";
                        break;

                    case "DELETE":
                        stmt = conn.prepareStatement("DELETE FROM contacts WHERE name = ?");
                        stmt.setString(1, request.getName());
                        int rowsDeleted = stmt.executeUpdate();
                        message = rowsDeleted > 0 ? "Contact deleted successfully." : "Failed to delete contact.";
                        break;

                    case "QUERY":
                        stmt = conn.prepareStatement("SELECT * FROM contacts WHERE name = ?");
                        stmt.setString(1, request.getName());
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            message = "Name: " + rs.getString("name") + ", Address: " + rs.getString("address") + ", Phone: " + rs.getString("phone");
                        } else {
                            message = "Contact not found.";
                        }
                        break;

                    case "LIST":
                        stmt = conn.prepareStatement("SELECT * FROM contacts");
                        rs = stmt.executeQuery();
                        List<String> contacts = new ArrayList<>();
                        while (rs.next()) {
                            contacts.add("Name: " + rs.getString("name") + ", Address: " + rs.getString("address") + ", Phone: " + rs.getString("phone"));
                        }
                        message = String.join("\n", contacts);
                        if (message.isEmpty()) {
                            message = "No contacts found.";
                        }
                        break;

                    default:
                        message = "Invalid action.";
                        break;
                }
            } catch (SQLException e) {
                message = "Database error: " + e.getMessage();
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return new Response(message);
        }
    }
}