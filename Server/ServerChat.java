package Server;

import java.util.ArrayList;

public class ServerChat implements IServerChat
{
    private  ArrayList<String> roomList; //RFA1

    public ServerChat() {
        roomList = new ArrayList<String>();
    }

    // RFA5
    public ArrayList<String> getRooms() {
        return roomList; 
    }

    public void createRoom(String roomName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createRoom'");
    }
}