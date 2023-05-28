package Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import Room.RoomChat;

public class ServerChat implements IServerChat
{
    private ArrayList<String> roomList; //RFA1 RFA3
    private ServerGUI gui; 
    private static Registry registry;

    public ServerChat() {
        roomList = new ArrayList<String>();
        gui = new ServerGUI(this);
    }

    // RFA5
    public ArrayList<String> getRooms() {
        return roomList; 
    }

    public void createRoom(String roomName) throws RemoteException {
        if (roomList.contains(roomName)) {
            throw new RemoteException("INVALIDNAME " + roomName + " already exists!");
        }

        try {
            RoomChat room = new RoomChat(roomName);
            registry.rebind(roomName, room);

            roomList.add(roomName);
            gui.CreateRoomVisual(roomName);
        } catch(Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }

    public void closeRoom(String roomName) throws RemoteException {
        if (!roomList.contains(roomName)) {
            throw new RemoteException("INVALIDNAME " + roomName + " does not exist!");
        }

        try {
            registry.unbind(roomName);            
            roomList.remove(roomName);
            gui.RemoveRoomVisual(roomName);
        } catch(Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 2020;
        String name = "Servidor";
        
        try {
            ServerChat serverObject = new ServerChat();
            
            registry = LocateRegistry.createRegistry(port);
            registry.rebind(name, serverObject);
            
            System.out.println("The chat server is running...");
        } catch (Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }
}