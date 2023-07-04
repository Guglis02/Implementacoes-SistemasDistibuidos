package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Classe que representa o middleware de comunicação.
public class CausalMulticast {
    static Integer multicastPort = 9000;
    MulticastSocket multicastSocket;
    InetAddress multicastAddress;

    DatagramSocket unicastSocket;
    InetAddress unicastAddress;

    ICausalMulticast client;
    Integer clientPort;
    CommunicationThread communicationThread;

    ConnectionThread connectionThread;

    Scanner scanner = new Scanner(System.in);

    ArrayList<String> delayedMessages = new ArrayList<>();

    public CopyOnWriteArrayList<Integer> clientList = new CopyOnWriteArrayList<>();

    public Map<Integer, Integer> vectorClock = Collections.synchronizedMap(new HashMap<>());

    public CausalMulticast(String ip, Integer port, ICausalMulticast client) throws IOException
    {   
        this.communicationThread = new CommunicationThread(this);
        this.connectionThread = new ConnectionThread(this);
        this.clientPort = port;
        this.client = client;

        // Setup do multicast para conexão com outros clientes.
        this.multicastSocket = new MulticastSocket(multicastPort);
        this.multicastAddress = InetAddress.getByName(ip);
        this.multicastSocket.joinGroup(multicastAddress);
        
        // Setup do unicast usado para enviar as mensagens.
        this.unicastSocket = new DatagramSocket(port);
        this.unicastAddress = InetAddress.getLocalHost();
        
        vectorClock.put(clientPort, 0);

        connectionThread.start();

        try {
            // Espera que três usuários estejam conectados.
            connectionThread.WaitForUsers();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.communicationThread.start();
    }
    
    // Método que recebe uma mensagem e a envia para os clientes.
    public void mcsend(String msg, ICausalMulticast client)
    {
        // Comando que esvazia o buffer de envio.
        if (msg.equals("/sendDelayed"))
        {
            SendDelayedMessages();
            return;
        }

        System.out.println("A mensagem: \"" + msg + "\" deve ser enviada para todos? (S/N) ");
        String sendToAllAnswer = scanner.nextLine();

        Map<Integer, Integer> vectorClockCopy = new HashMap<>(this.vectorClock);
        this.vectorClock.put(this.clientPort, this.vectorClock.get(this.clientPort) + 1);

        for (Integer user : clientList)
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
            else {
                delayedMessages.add(user.toString() + "_" + msg + "_" + vectorClockCopy);
            }
        }
    }

    public synchronized void ReorderVectorClock()
    {
        vectorClock = Collections.synchronizedMap(new TreeMap<>(vectorClock));
    }
    
    // Envia as mensagens salvas no buffer.
    private void SendDelayedMessages()
    {
        for (String message : delayedMessages)
        {
            String splittedMessage[] = message.split("_");
            String messageToSend = "USRMSG_" + this.clientPort + "_" + splittedMessage[1] + "_" + splittedMessage[2];
            DatagramPacket sendPacket = new DatagramPacket(messageToSend.getBytes(), messageToSend.length(), this.unicastAddress, Integer.parseInt(splittedMessage[0]));
            try {
                this.unicastSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        delayedMessages.clear();
    }
}