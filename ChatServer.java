import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class ChatServer {
    private ArrayList<PrintWriter> clientList = new ArrayList<>();
    private List<SocketChannel> clientChannels=Collections.synchronizedList(new ArrayList<>());
    private JTextArea textArea;
    private JFrame frame;
    private JTextField textField;
    private JButton attention;
    private JPanel bottomPanel;
    private JPanel mainPanel;
    private volatile boolean flag = false;
    private ServerSocketChannel serverChannel;

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        new Thread(() -> networkingSetup()).start();

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Chat Server");
            frame.setSize(700, 700);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    int result = JOptionPane.showConfirmDialog(frame, "Do you really want to close !", "Closing Window",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        flag = true;
                        try{
                            if(serverChannel !=null && serverChannel.isOpen()){
                                serverChannel.close();
                            }
                            for(PrintWriter writer:clientList){
                                writer.close();
                            }
                            clientList.clear();
                            synchronized(clientChannels){
                                for(SocketChannel channel:clientChannels){
                                    if(channel.isOpen()) channel.close();
                                }
                            }
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        frame.dispose();
                    }
                }
            });

            JScrollPane scrollPane = getScrollPane();

            textField = new JTextField();
            textField.setPreferredSize(new Dimension(500, 80));
            textField.setFont(new Font("Times New Roman", Font.BOLD, 20));
            textField.setForeground(Color.RED);
            textField.setCaretColor(Color.BLUE);
            textField.requestFocus();

            attention = new JButton("Attention");
            attention.setPreferredSize(new Dimension(100, 50));
            attention.setFont(new Font("Times New Roman", Font.BOLD, 16));
            attention.setForeground(Color.RED);
            attention.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String msg = textField.getText();
                    if (msg.isEmpty())
                        return;
                    for (PrintWriter writer : clientList) {
                        writer.println(msg);
                        writer.flush();
                    }
                }
            });

            bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            bottomPanel.add(textField);
            bottomPanel.add(attention);
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(scrollPane);
            mainPanel.add(bottomPanel);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
            frame.setVisible(true);
            frame.setResizable(false);
        });
    }

    public JScrollPane getScrollPane() {
        textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(600, 500));
        textArea.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        textArea.setForeground(Color.BLUE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    public void networkingSetup() {
        InetSocketAddress serverAddress;
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            serverAddress = new InetSocketAddress(5000);
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(serverAddress);
            while (!flag && serverChannel.isOpen()) {
                SocketChannel clientChannel = serverChannel.accept();
                clientChannels.add(clientChannel);
                PrintWriter writer = new PrintWriter(Channels.newWriter(clientChannel, StandardCharsets.UTF_8));
                clientList.add(writer);
                executorService.submit(new clientHandler(clientChannel));
            }
        } catch (IOException e) {
            if(!flag) e.printStackTrace();
        }finally{
            executorService.shutdown();
        } 
    }

    class clientHandler implements Runnable {

        SocketChannel clientChannel;
        BufferedReader reader;

        public clientHandler(SocketChannel cliSocketChannel) {
            this.clientChannel = cliSocketChannel;
            reader = new BufferedReader(Channels.newReader(this.clientChannel, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    final String msg = message;
                    SwingUtilities.invokeLater(() -> textArea.append(msg + "\n"));
                    sendMessages(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessages(String message) {
        for(PrintWriter writer : clientList) {
            writer.println(message);
            writer.flush();
        }
    }
}
