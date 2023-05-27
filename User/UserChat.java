package User;

import java.util.ArrayList;

import Server.IServerChat;

public class UserChat implements IUserChat
{
    private IServerChat server;
    private ArrayList<String> roomList;

    public UserChat(IServerChat server) {
        this.server = server;
        this.roomList = server.getRooms();
    }

    public void deliverMsg(String senderName, String msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deliverMsg'");
    }
}