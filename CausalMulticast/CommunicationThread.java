package CausalMulticast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;

import javax.xml.crypto.Data;

public class CommunicationThread extends Thread
{
    CausalMulticast causalMulticast;

    ArrayList<String> clientList;

    public CommunicationThread(CausalMulticast causalMulticast, MulticastSocket socket)
    {
        this.causalMulticast = causalMulticast;
    }

    @Override
    public void run()
    {
        clientList = new ArrayList<String>();
        DatagramPacket receivPacket = new DatagramPacket(new byte[1024], 1024);

        while(true)
        {
            try {
                this.causalMulticast.socket.receive(receivPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String msg = new String(receivPacket.getData(), 0, receivPacket.getLength());
            ParseMessage(msg);
        }
    }

    // As mensagens seguem o seguinte padrão:
    // Mensagem de join: USRJOIN_nome do usuário
    // Mensagem normal: USRMSG_
    private void ParseMessage(String message)
    {

    }
    
}