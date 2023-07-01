package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.lang.System;

// Classe que representa a thread que fica escutando as mensagens que chegam
public class CommunicationThread extends Thread
{
    private CausalMulticast causalMulticast;
    
    private String message;
    public Map<Integer, Integer> vectorClock = new HashMap<Integer, Integer>();
    public ArrayList<String> messageBuffer = new ArrayList<String>();

    public ArrayList<Integer> clientList = new ArrayList<Integer>();

    private int maxConnections = 2;

    public CommunicationThread(CausalMulticast causalMulticast)
    {
        this.causalMulticast = causalMulticast;
    }

    @Override
    public void run()
    {      
        while(true)
        {
            DatagramPacket multicastPacket = new DatagramPacket(new byte[1024], 1024);
            DatagramPacket unicastPacket = new DatagramPacket(new byte[1024], 1024);
            
            try {
                this.causalMulticast.multicastSocket.receive(multicastPacket);
                this.causalMulticast.unicastSocket.receive(unicastPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String multicastMsg = new String(multicastPacket.getData(), 0, multicastPacket.getLength());
            String unicastMsg = new String(unicastPacket.getData(), 0, unicastPacket.getLength());
            ParseMessage(multicastMsg);
            ParseMessage(unicastMsg);            
        }
    }

    public void SearchForUsers() throws IOException
    {
        SendSearchMessage(this.causalMulticast.clientPort.toString());
        DatagramPacket receivPacket = new DatagramPacket(new byte[1024], 1024);

        while (clientList.size() < maxConnections)
        {
            this.causalMulticast.multicastSocket.receive(receivPacket);

            String message = new String(receivPacket.getData(), 0, receivPacket.getLength());
            String splittedMessage[] = message.split("_");
            
            if (splittedMessage[0].equals("USRJOIN"))
            {
                Integer clientPort = Integer.parseInt(splittedMessage[1]);
                if (!clientList.contains(clientPort))
                {
                    clientList.add(clientPort);
                    this.vectorClock.put(clientPort, 0);
                    this.vectorClock = new TreeMap<Integer, Integer>(this.vectorClock);
                    
                    System.out.println(clientPort + " entrou no grupo.");
                }
                System.out.println("Usuarios atuais no grupo: " + clientList.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SendSearchMessage(this.causalMulticast.clientPort.toString());
            }
        }
    }

    private void SendSearchMessage(String message)
    {
        String searchMessage = String.format("USRJOIN_%s", message);
        DatagramPacket sendPacket = new DatagramPacket(searchMessage.getBytes(), searchMessage.length(), this.causalMulticast.multicastAddress, this.causalMulticast.multicastPort);
        try {
            this.causalMulticast.multicastSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<String, Integer> ExtractMessageVectorClock(String messageVectorClockString)
    {
        Map<String, Integer> messageVectorClock = new HashMap<String, Integer>();
        String[] pairs = messageVectorClockString.replaceAll("[{} ]", "").split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            messageVectorClock.put(keyValue[0], Integer.parseInt(keyValue[1]));
        }
        return messageVectorClock;
    }

    // As mensagens seguem o seguinte padrão:
    // Mensagem de join: USRJOIN_nome do usuário
    // Mensagem normal: USRMSG_nome do usuário_mensagem_relógio vetorial
    private void ParseMessage(String message)
    {
        String[] splittedMessage = message.split("_");

        if (splittedMessage[1].equals(this.causalMulticast.clientPort.toString()))
        {
            return;
        }
        
        if (splittedMessage[0].equals("USRJOIN"))
        {
            try {
                maxConnections++;
                SearchForUsers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (splittedMessage[0].equals("USRMSG"))
        {
            Map<String, Integer> messageVectorClock = ExtractMessageVectorClock(splittedMessage[3]);
            System.out.println("Relogio vetorial recebido: " + messageVectorClock.toString());        
            System.out.println("Relogio vetorial atual: " + this.vectorClock.toString());

            // Testar se a mensagem pode ser entregue
            // Se puder entrega, senão adiciona no buffer
            causalMulticast.client.deliver(splittedMessage[1] + " - " + splittedMessage[2]);
        }
    }
    
}