import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class ChatClient {
    private String IP;
    private PrintWriter writer;
    private BufferedReader reader;
    private JFrame mainFrame;
    private JTextField textField;
    private JTextArea textArea;
    private JButton button;
    private JPanel mainPanel;
    private JPanel bottomPanel;

    public void getServerIP(){
        
        while(true){
            boolean flag=true;
            String ip=JOptionPane.showInputDialog(null,
                "Enter Server IP","Server IP Address",JOptionPane.QUESTION_MESSAGE);
            if(ip==null) System.exit(0);
            if(!ip.isEmpty() && ip.matches(".*\\..*")){
                String[] list=ip.split("\\.");
                if(list.length==4){
                    for(String str:list){
                        try{
                            int num=Integer.parseInt(str);
                            if(num<0 || num>255) flag=false;
                        }catch(NumberFormatException e){
                            flag=false;
                            break;
                        }
                    }
                }
                else flag=false;
            }
            else flag=false;
            if(flag==true){
                IP=ip;
                break;
            }
            else{
                JOptionPane.showMessageDialog(mainFrame,
                "Enter Valid IP Address","Warning",JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void networkSetup() {
        String serverIP = IP;
        InetSocketAddress serverAddress = new InetSocketAddress(serverIP, 5000);
        try {
            SocketChannel clientChannel = SocketChannel.open(serverAddress);
            writer=new PrintWriter(Channels.newWriter(clientChannel,StandardCharsets.UTF_8));
            reader=new BufferedReader(Channels.newReader(clientChannel,StandardCharsets.UTF_8));
            SwingUtilities.invokeLater(()->{
                JOptionPane.showMessageDialog(mainFrame,"Information","COnnection Established",JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        new Thread(()->networkSetup()).start();

        SwingUtilities.invokeLater(()->{
            mainFrame=new JFrame("Chat Client");
            mainFrame.setSize(700,700);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event){
                    int result = JOptionPane.showConfirmDialog(mainFrame, "Do you really want to close !", "Closing Window",
                                JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) mainFrame.dispose();
                }    
            });

            textField=new JTextField();
            textField.setPreferredSize(new Dimension(500,75));
            textField.setFont(new Font("Times New Roman", Font.PLAIN, 20));
            textField.setForeground(Color.BLUE);

            button=new JButton("Send");
            button.setPreferredSize(new Dimension(80,50));
            button.addActionListener(event->{
                if(textField.getText().isEmpty()) return;
                String msg=textField.getText();
                writer.println(msg);
                writer.flush();
                textField.setText("");
            });

            bottomPanel=new JPanel();
            bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER,10,10));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            bottomPanel.add(textField);
            bottomPanel.add(button);

            JScrollPane scrollPane=getScrollPane();

            mainPanel=new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.add(scrollPane);
            mainPanel.add(bottomPanel);

            chatDisplayBoard();
            mainFrame.add(mainPanel,BorderLayout.CENTER);
            mainFrame.setVisible(true);
            mainFrame.setResizable(false);
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return scrollPane;
    }

    public void chatDisplayBoard(){
        ExecutorService executor=Executors.newSingleThreadExecutor();
        executor.submit(()->{
            String msg;
            try{
                while((msg=reader.readLine())!=null){
                    final String message=msg;
                    SwingUtilities.invokeLater(()->textArea.append(message+"\n"));
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            ChatClient client=new ChatClient();
            client.getServerIP();
            client.start();
        });
    }
}
