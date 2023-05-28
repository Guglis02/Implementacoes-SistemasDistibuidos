package Server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import Room.RoomChat;

public class ServerChat implements IServerChat
{
    private  ArrayList<String> roomList; //RFA1 RFA3

    public ServerChat() {
        roomList = new ArrayList<String>();
    }

    // RFA5
    public ArrayList<String> getRooms() {
        return roomList; 
    }

    public void createRoom(String roomName) {
        // if (roomList.contains(roomName)) {
        //     throw new RemoteException("INVALIDNAME " + roomName + " already exists!");
        // }

        try {
            roomList.add(roomName);
        } catch(Exception e) {
            System.out.println("Server Exception! " + e.getMessage());
        }
    }
}