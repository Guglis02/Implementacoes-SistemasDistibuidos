package Room;

import java.rmi.RemoteException;

import User.IUserChat;

public interface IRoomChat extends java.rmi.Remote {
    public void sendMsg(String usrName, String msg) throws RemoteException;

    public void joinRoom(String usrName, IUserChat user) throws RemoteException;

    public void leaveRoom(String usrName) throws RemoteException;

    public void closeRoom() throws RemoteException;

    public String getRoomName() throws RemoteException;
}