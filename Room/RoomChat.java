package Room;

import java.util.Map;

import User.IUserChat;

public class RoomChat implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList; //RFA2

    public RoomChat(String roomName) {
        this.roomName = roomName;
        userList = new java.util.HashMap<String, IUserChat>();
    }

    public void sendMsg(String usrName, String msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMsg'");
    }

    public void joinRoom(String usrName, IUserChat user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'joinRoom'");
    }

    public void leaveRoom(String usrName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leaveRoom'");
    }

    public void closeRoom() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeRoom'");
    }

    public String getRoomName() {
        return roomName;
    }
}