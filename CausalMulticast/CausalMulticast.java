package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CausalMulticast {
    static Integer multicastPort = 9000;
    MulticastSocket multicastSocket;
    InetAddress multicastAddress;

    DatagramSocket unicastSocket;
    InetAddress unicastAddress;

    ICausalMulticast client;
    Integer clientPort;
    CommunicationThread communicationThread;

    Scanner scanner = new Scanner(System.in);

    public CausalMulticast(String ip, Integer port, ICausalMulticast client) throws IOException
    {   
        this.communicationThread = new CommunicationThread(this);
        this.clientPort = port;
        this.client = client;
        this.multicastSocket = new MulticastSocket(multicastPort);
        this.multicastAddress = InetAddress.getByName(ip);
        this.multicastSocket.joinGroup(multicastAddress);
        
        this.unicastSocket = new DatagramSocket(port);
        this.unicastAddress = InetAddress.getLocalHost();
        
        communicationThread.vectorClock.put(clientPort, 0);
        
        try {
            this.communicationThread.SearchForUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }      

        this.communicationThread.start();
    }
    
    public void mcsend(String msg, ICausalMulticast client)
    {
        System.out.println("A mensagem: \"" + msg + "\" deve ser enviada para todos? (S/N) ");
        String sendToAllAnswer = scanner.nextLine();

        Map<Integer, Integer> vectorClockCopy = new HashMap<Integer, Integer>(this.communicationThread.vectorClock);
        this.communicationThread.vectorClock.put(this.clientPort, this.communicationThread.vectorClock.get(this.clientPort) + 1);

        for (Integer user : communicationThread.clientList)
        {
            String sendAnswer = null;
            if (sendToAllAnswer.equals("N"))
            {
                System.out.println("A mensagem: \"" + msg + "\" deve ser enviada para " + user + "? (S/N) ");
                sendAnswer = scanner.nextLine();
            }
            if (sendToAllAnswer.equals("S") || sendAnswer.equals("S"))
            {
                String message = "USRMSG_" + this.clientPort + "_" + msg + "_" + vectorClockCopy;
                DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), this.unicastAddress, user);
                try {
                    this.unicastSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}