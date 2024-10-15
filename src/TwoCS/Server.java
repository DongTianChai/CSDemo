package TwoCS;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static Map<String, Contact> contacts = new HashMap<>();

    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(9999); // 服务器监听端口
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress ipAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                System.out.println("Received: " + sentence);

                String response = handleRequest(sentence);
                sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String handleRequest(String request) {
        String[] parts = request.split(",");
        String command = parts[0].trim();

        switch (command) {
            case "ADD":
                String name = parts[1].trim();
                String address = parts[2].trim();
                String phone = parts[3].trim();
                contacts.put(name, new Contact(name, address, phone));
                return "Contact added successfully.";
            case "UPDATE":
                name = parts[1].trim();
                address = parts[2].trim();
                phone = parts[3].trim();
                if (contacts.containsKey(name)) {
                    contacts.put(name, new Contact(name, address, phone));
                    return "Contact updated successfully.";
                } else {
                    return "Contact not found.";
                }
            case "DELETE":
                name = parts[1].trim();
                if (contacts.remove(name) != null) {
                    return "Contact deleted successfully.";
                } else {
                    return "Contact not found.";
                }
            case "QUERY":
                name = parts[1].trim();
                Contact contact = contacts.get(name);
                if (contact == null) {
                    return "Contact not found.";
                } else {
                    return "Name: " + contact.getName() + ", Address: " + contact.getAddress() + ", Phone: " + contact.getPhone();
                }
            case "LIST":
                StringBuilder list = new StringBuilder();
                for (Contact c : contacts.values()) {
                    list.append("Name: ").append(c.getName()).append(", Address: ").append(c.getAddress()).append(", Phone: ").append(c.getPhone()).append("\n");
                }
                return list.toString();
            default:
                return "Invalid command.";
        }
    }
}

class Contact {
    private String name;
    private String address;
    private String phone;

    public Contact(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}