package User;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import Room.IRoomChat;
import Server.IServerChat;

public class UserChat implements IUserChat
{
    private String usrName;

    private IServerChat server;
    private IRoomChat room;

    private UserGUI gui;

    private ArrayList<String> roomList;

    public UserChat(IServerChat server) {
        this.server = server;
        this.usrName = "";
        this.roomList = new ArrayList<String>();
        gui = new UserGUI(this);
        GetRoomsFromServer();

        // Add a WindowListener to the UserGUI instance
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leaveRoom();
            }
        };
        gui.addWindowListener(exitListener);
    }

    public void deliverMsg(String senderName, String msg) {
        gui.ShowMessage(senderName, msg);
    }

    public String getUsrName() {
        return usrName;
    }

    public void setUsrName(String usrName) {
        if (usrName == null)
        {
            return;
        }
        this.usrName = usrName;
    }

    public ArrayList<String> getRoomList() {
        return roomList;
    }

    public String getCurrentRoomName() {
        if (room == null) {
            return "";
        }
        try {
            return room.getRoomName();
        } catch (RemoteException e) {
            System.err.println("Client exception! " + e.toString());
            return "";
        }
    }

    public void GetRoomsFromServer()
    {        
        try {
            this.roomList = server.getRooms(); // RFA4 RFA5
        } catch (Exception e) {
            System.err.println("Client exception! " + e.toString());
        }
    }

    public void tryJoinRoom(String selectedRoom) throws Exception {
        if (selectedRoom == null) {
            return;
        }

        Registry registry = LocateRegistry.getRegistry("localhost", 2020);
        room = (IRoomChat) registry.lookup(selectedRoom);
        System.out.println("EU N AGUENTO MAIS");
        room.joinRoom(usrName, this);
    }

    public void leaveRoom() {
        if (room == null) {
            return;
        }

        try {
            room.leaveRoom(usrName);
            usrName = null;
            room = null;
        } catch (RemoteException e) {
            System.err.println("Client exception! " + e.toString());
        }
    }

    public void sendMessage(String message) {
        try {
            room.sendMsg(usrName, message);
        } catch (RemoteException e) {
            System.err.println("Client exception! " + e.toString());
        }
    }

    public void tryCreateRoom(String roomName) throws Exception {           
        server.createRoom(roomName);
    }

    public static void main(String[] args) {
        int port = 2020;
        
        try {
            System.out.println("Connecting to server...");
            Registry registry = LocateRegistry.getRegistry("localhost", port);
            IServerChat server = (IServerChat) registry.lookup("Servidor");
            new UserChat(server);
            System.out.println("Connected to server on port " + port + "!");
        } catch (Exception e) {
            System.err.println("Client exception! " + e.toString());
        }
    }
}