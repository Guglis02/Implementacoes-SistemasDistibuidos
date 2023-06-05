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
        getRegistry();
        roomList = new ArrayList<String>();
        gui = new ServerGUI(this);
    }

    // RFA5
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    public void createRoom(String roomName) throws RemoteException {
        if (roomList.contains(roomName)) {
            throw new RemoteException("INVALIDNAME " + roomName + " already exists!");
        }

        try {
            RoomChat room = new RoomChat(roomName);
            getRegistry().rebind(roomName, room);
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
            getRegistry().unbind(roomName);
            roomList.remove(roomName);
            gui.RemoveRoomVisual(roomName);
        } catch(Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String name = "Servidor";
        
        try {
            ServerChat serverObject = new ServerChat();
            getRegistry().rebind(name, serverObject);
            
            System.out.println("The chat server is running...");
        } catch (Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }

    private static Registry getRegistry()
    {
        if (registry == null)
        {
            try {
                registry = LocateRegistry.createRegistry(2020);
            } catch (RemoteException e1) {
                try {
                    registry = LocateRegistry.getRegistry("localhost", 2020);
                }
                catch (RemoteException e2) {
                    System.out.println("Server Exception! " + e2.getMessage());
                }
            }
        }
        return registry;
    }
}