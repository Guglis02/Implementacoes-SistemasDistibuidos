package User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class UserGUI {
    private JFrame frame;
    private JTextPane messagePane;
    private JTextField textField;
    private JButton sendButton;
    private JPanel textFieldPane;
    private JComponent messagesPane;
    private DefaultListModel<String> roomListElement;
    private JButton refreshButton;
    private JButton createButton;
    private JButton joinButton;
    private JButton leaveButton;
    private JList<String> roomListVisual;
    private String selectedRoom;

    public UserGUI(UserChat user)
    {
        frame = new JFrame("Join some room to choose a nickname");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GenerateMainPane(user);
        SetButtonActions(user);

        user.GetRoomsFromServer();        
        UpdateRoomList(user.getRoomList());   
        
        frame.setVisible(true);
    }    
    
    private void GenerateMainPane(UserChat user) {      
        // Barra de input de texto e botão de enviar
        textFieldPane = new JPanel(new BorderLayout());  
        sendButton = new JButton("Send");
        textField = new JTextField(50);
        textField.setEditable(false);
        textFieldPane.add(textField, BorderLayout.CENTER);
        textFieldPane.add(sendButton, BorderLayout.EAST);

        // Painel de mensagens
        JLabel messagesLabel = new JLabel("<html><b>Messages:</b></html>");
        messagesLabel.setForeground(Color.DARK_GRAY);
        messagesLabel.setHorizontalAlignment(JLabel.LEFT);

        messagePane = new JTextPane();
        messagePane.setEditable(false);
        messagePane.setContentType("text");

        messagesPane = new JPanel(new BorderLayout());
        messagesPane.add(new JScrollPane(messagePane), BorderLayout.CENTER);
        messagesPane.setPreferredSize(new Dimension(400, 300));
        messagesPane.add(messagesLabel, BorderLayout.NORTH);

        // Painel de salas
        roomListElement = new DefaultListModel<String>();
        roomListVisual = new JList<String>(roomListElement);
        roomListVisual.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomListVisual.setLayoutOrientation(JList.VERTICAL);
        roomListVisual.setVisibleRowCount(-1);  

        JLabel roomListLabel = new JLabel("<html><b>Rooms:</b></html>");
        roomListLabel.setForeground(Color.DARK_GRAY);
        roomListLabel.setHorizontalAlignment(JLabel.LEFT);
        JScrollPane roomListPane = new JScrollPane(roomListVisual);
        roomListPane.setPreferredSize(new Dimension(200, 300));

        refreshButton = new JButton("Refresh");
        createButton = new JButton("Create");
        joinButton = new JButton("Join");
        joinButton.setEnabled(false);
        leaveButton = new JButton("Leave");
        leaveButton.setEnabled(false);

        JPanel ButtonPanel = new JPanel();
        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        ButtonPanel.setLayout(new GridLayout(2, 2));
        ButtonPanel.add(refreshButton);
        ButtonPanel.add(createButton);
        ButtonPanel.add(joinButton);
        ButtonPanel.add(leaveButton);

        JPanel roomListContainer = new JPanel();
        roomListContainer.setLayout(new BorderLayout());
        roomListContainer.add(roomListLabel, BorderLayout.NORTH);
        roomListContainer.add(roomListPane, BorderLayout.CENTER);
        roomListContainer.add(ButtonPanel, BorderLayout.SOUTH);

        // Painel principal
        frame.getContentPane().add(messagesPane, BorderLayout.CENTER);
        frame.getContentPane().add(textFieldPane, BorderLayout.SOUTH);
        frame.getContentPane().add(roomListContainer, BorderLayout.WEST);
        
        // Define estilo de mensagens do servidor e de usuários
        setMessageStyles();
                        
        frame.pack();
    }
    
    private void setMessageStyles() {
        Style userMessageStyle = messagePane.addStyle("Message", null);
        StyleConstants.setForeground(userMessageStyle, Color.BLACK);
        StyleConstants.setFontFamily(userMessageStyle, "Arial");
        StyleConstants.setFontSize(userMessageStyle, 12);

        Style systemMessageStyle = messagePane.addStyle("System", null);
        StyleConstants.setForeground(systemMessageStyle, Color.GREEN);
        StyleConstants.setFontFamily(systemMessageStyle, "Arial");
        StyleConstants.setFontSize(systemMessageStyle, 12);
    }
    
    public void UpdateRoomList(ArrayList<String> roomList)
    {
        roomListElement.clear();
        for (String r : roomList) {
            roomListElement.addElement(r);
        }   
    }

    private void SetButtonActions(UserChat user) {
        // Listener da troca de sala selecionada
        roomListVisual.addListSelectionListener(new ListSelectionListener() 
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting()) {
                    if (roomListVisual.getSelectedIndex() != -1) {
                        selectedRoom = roomListVisual.getSelectedValue();
                        joinButton.setEnabled(true);
                    } else {
                        selectedRoom = null;
                        joinButton.setEnabled(false);
                    }
                }
            }
        });

        // Listener do botão e do campo de enviar mensagem
        sendButton.addActionListener(e -> {
            String message = textField.getText();
            if (!message.isEmpty()) {
                user.sendMessage(message);
                textField.setText("");
            }
        });

        textField.addActionListener(e -> {
            String message = textField.getText();
            if (!message.isEmpty()) {
                user.sendMessage(message);
                textField.setText("");
            }
        });

        // Botão de atualizar lista de salas
        refreshButton.addActionListener(e -> {
            user.GetRoomsFromServer();
            UpdateRoomList(user.getRoomList());
        });

        // Botão de criar sala
        createButton.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(frame, "Room name:", "Create Room", JOptionPane.QUESTION_MESSAGE).strip();

            if (roomName != null && !roomName.isEmpty()) {
                try {
                    user.tryCreateRoom(roomName);
                    user.GetRoomsFromServer();
                    UpdateRoomList(user.getRoomList());
                } catch (Exception err) {
                    String message = err.getMessage();
                    if (message.contains("INVALIDNAME"))
                        JOptionPane.showMessageDialog(frame, "This name is already in use", "Error creating room", JOptionPane.ERROR_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(frame, "Error creating room");
                    System.out.println("Client Exception! " + message);
                }
            }
        });

        joinButton.addActionListener(e -> {
            if (selectedRoom.equals(user.getCurrentRoomName())) {
                JOptionPane.showMessageDialog(frame, "You are already in this room");
                return;
            }

            while (user.getUsrName().isEmpty()) {
                user.setUsrName(JOptionPane.showInputDialog(frame, "Choose a nickname:", "Join Room", JOptionPane.QUESTION_MESSAGE).strip());
            }

            user.leaveRoom();

            try {
                user.tryJoinRoom(selectedRoom);
            } catch (Exception err) {
                user.setUsrName("");
                frame.setTitle("Join some room to choose a nickname");
                String message = err.getMessage();
                if (message.contains("INVALIDNAME")) {
                    JOptionPane.showMessageDialog(frame, "This name is already in use", "Error joining room", JOptionPane.ERROR_MESSAGE);
                } else
                    JOptionPane.showMessageDialog(frame, "Error joining room");
            }
            
            leaveButton.setEnabled(true);
            textField.setEditable(true);
            frame.setTitle(user.getUsrName() + " - " + user.getCurrentRoomName());
        });

        leaveButton.addActionListener(e -> {
            user.leaveRoom();
            leaveButton.setEnabled(false);
            textField.setEditable(false);
        });
    }

    public void addWindowListener(WindowListener exitListener) {
        frame.addWindowListener(exitListener);
    }

    public void ShowMessage(String senderName, String msg) { 
        StyledDocument document = messagePane.getStyledDocument();

        try {
            if (msg.startsWith("MESSAGE")) {
                document.insertString(document.getLength(), senderName + ": " + msg.substring(8) + "\n", messagePane.getStyle("Message"));
            } else if (msg.startsWith("SERVER")) {
                document.insertString(document.getLength(), senderName + ": " +  msg.substring(7) + "\n", messagePane.getStyle("System"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
