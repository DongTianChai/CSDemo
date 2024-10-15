package ThreeCS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends JFrame {
    private JTextField queryField;
    private JTextArea responseArea;
    private Socket socket;

    public Client() {
        setTitle("个人通讯录系统");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 查询联系人面板
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new GridLayout(2, 2));
        queryPanel.add(new JLabel("用于查询的姓名:"));
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
                    sendRequest("ADD", name, address, phone);
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
                        sendRequest("UPDATE", name, address, phone);
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
                    sendRequest("DELETE", name, "", "");
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

                sendRequest("QUERY", name, "", "");
            }
        });

        listButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest("LIST", "", "", "");
            }
        });

        // 连接到服务器
        connectToServer();

        setVisible(true);
    }

    private void connectToServer() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("127.0.0.1", 9999);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the server.");
            System.exit(1);
        }
    }

    private void sendRequest(String action, String name, String address, String phone) {
        if (socket == null || socket.isClosed()) {
            connectToServer();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // 发送请求
            Request request = new Request(action, name, address, phone);
            out.writeObject(request);
            out.flush();

            // 接收响应
            Response response = (Response) in.readObject();
            responseArea.append(response.getMessage() + "\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error communicating with the server.");
            connectToServer(); // 尝试重新连接
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}