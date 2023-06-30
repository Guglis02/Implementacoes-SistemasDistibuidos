package CausalMulticast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class CausalMulticast {
    MulticastSocket socket;
    InetAddress group;
    ICausalMulticast client;

    public CausalMulticast(String ip, Integer port, ICausalMulticast client) throws IOException
    {   
        this.socket = new MulticastSocket(port);
        this.group = InetAddress.getByName(ip);
        this.socket.joinGroup(group);
        this.client = client;
    }
    
    public void mcsend(String msg, ICausalMulticast client)
    {

    }
}