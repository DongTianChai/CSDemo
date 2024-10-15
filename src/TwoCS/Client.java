package TwoCS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client extends JFrame {
    private JTextField queryField;
    private JTextArea responseArea;
    private DatagramSocket clientSocket;

    public Client() {
        setTitle("个人通讯录系统");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 查询联系人面板
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new GridLayout(2, 2));
        queryPanel.add(new JLabel("用于查询的姓名"));
        queryField = new JTextField();
        queryPanel.add(queryField);
        JButton queryButton = new JButton("查询");
        queryPanel.add(queryButton);

        // 显示所有联系人面板
        JButton listButton = new JButton("显示所有");
        JPanel listPanel = new JPanel();
        listPanel.add(listButton);

        // 响应区域
        responseArea = new JTextArea(10, 50);
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);

        // 添加到主窗口
        add(queryPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮事件处理
        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.WEST);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(Client.this, "Enter Name:");
                String address = JOptionPane.showInputDialog(Client.this, "Enter Address:");
                String phone = JOptionPane.showInputDialog(Client.this, "Enter Phone:");

                if (name != null && !name.isEmpty() && address != null && !address.isEmpty() && phone != null && !phone.isEmpty()) {
                    String request = "ADD," + name + "," + address + "," + phone;
                    sendRequest(request);
                } else {
                    JOptionPane.showMessageDialog(Client.this, "Please enter all fields.");
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(Client.this, "Enter Name to Update:");
                if (name != null && !name.isEmpty()) {
                    String address = JOptionPane.showInputDialog(Client.this, "Enter New Address:");
                    String phone = JOptionPane.showInputDialog(Client.this, "Enter New Phone:");

                    if (address != null && !address.isEmpty() && phone != null && !phone.isEmpty()) {
                        String request = "UPDATE," + name + "," + address + "," + phone;
                        sendRequest(request);
                    } else {
                        JOptionPane.showMessageDialog(Client.this, "Please enter all fields.");
                    }
                } else {
                    JOptionPane.showMessageDialog(Client.this, "Please enter a name to update.");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(Client.this, "Enter Name to Delete:");
                if (name != null && !name.isEmpty()) {
                    String request = "DELETE," + name;
                    sendRequest(request);
                } else {
                    JOptionPane.showMessageDialog(Client.this, "Please enter a name to delete.");
                }
            }
        });

        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = queryField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(Client.this, "Please enter a name to query.");
                    return;
                }

                String request = "QUERY," + name;
                sendRequest(request);
            }
        });

        listButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String request = "LIST";
                sendRequest(request);
            }
        });

        // 初始化客户端套接字
        try {
            clientSocket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize socket.");
            System.exit(1);
        }

        setVisible(true);
    }

    private void sendRequest(String request) {
        try {
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1"); // 服务器IP地址
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 9999);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            responseArea.append(response + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error communicating with the server.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}