package Room;

import java.rmi.RemoteException;
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
        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "MESSAGE " + msg);
            } catch (Exception e) {
                System.out.println("Room Exception! " + e.getMessage());
            }
        }
    }

    public void joinRoom(String usrName, IUserChat user) {
        // if (userList.containsKey(usrName)) {
        //     throw new RemoteException("INVALIDNAME " + usrName + " already exists in this room!");
        // }
    
        userList.put(usrName, user);

        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "SERVER " + usrName + " joined the room!");
            } catch (Exception e) {
                System.out.println("Room Exception! " + e.getMessage());
            }
        }
    }

    public void leaveRoom(String usrName) {
        // if (!userList.containsKey(usrName)) {
        //     throw new RemoteException("ROOMALERT " + usrName + " does not exist in this room!");
        // }

        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "SERVER " + usrName + " left the room!");
            } catch (Exception e) {
                System.out.println("Room Exception! " + e.getMessage());
            }
        }

        userList.remove(usrName);
    }

    public void closeRoom() {
        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg("SERVER", "SERVER Room" + roomName + "was closed!");
                userList.remove(entry.getKey());
            } catch (Exception e) {
                System.out.println("Room Exception! " + e.getMessage());
            }
        }
    }

    public String getRoomName() {
        return roomName;
    }
}