package Server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ServerGUI {
    private JFrame frame;
    private JButton opButton, clButton;
    private JLabel label;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JScrollPane listScroller;

    public ServerGUI(ServerChat server)
    {
        frame = new JFrame("Server Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GenerateMainPane();
        SetButtonActions(server);
        frame.pack();
        frame.setVisible(true);
    }

    private void GenerateMainPane()
    {
        listModel = new DefaultListModel<String>();

        list = new JList<String>();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setModel(listModel);

        label = new JLabel("Number of Rooms: 0");
        label.setVerticalTextPosition(JLabel.TOP);
        label.setHorizontalTextPosition(JLabel.CENTER);

        frame.getContentPane().add(label, BorderLayout.NORTH);
        
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300,300));

        frame.getContentPane().add(listScroller, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 2, 0));
        opButton = new JButton("Open Room");
        clButton = new JButton("Close Room");
        buttonPanel.add(opButton);
        buttonPanel.add(clButton);

        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void SetButtonActions(ServerChat server) {
        opButton.addActionListener(a -> {
            String roomName = JOptionPane.showInputDialog(frame, "Room name:", "Create Room", JOptionPane.QUESTION_MESSAGE).strip();
            
            if (roomName == null || roomName.isEmpty()) {
                return;
            }
            
            try {
                server.createRoom(roomName);
            } catch (Exception e) {
                System.out.println("Server Exception! " + e.getMessage());
            }
        });

        clButton.addActionListener(a -> {
            for (String roomToClose : list.getSelectedValuesList()) {
                try {
                    server.closeRoom(roomToClose);                    
                } catch (Exception e) {
                    System.out.println("Server Exception! " + e.getMessage());
                }
            }
        });
    }

    public void CreateRoomVisual(String roomName) {
        listModel.addElement(roomName);
        label.setText("Number of Rooms: " + listModel.size());
    }

    public void RemoveRoomVisual(String roomName) {
        listModel.removeElement(roomName);
        label.setText("Number of Rooms: " + listModel.size());
    }
}
