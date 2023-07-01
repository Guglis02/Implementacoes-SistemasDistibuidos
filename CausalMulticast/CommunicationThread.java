package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.lang.System;

// Classe que representa a thread que fica escutando as mensagens que chegam.
public class CommunicationThread extends Thread
{
    private CausalMulticast causalMulticast;
    
    public Map<Integer, Integer> vectorClock = new HashMap<Integer, Integer>();
    public ArrayList<String> messageBuffer = new ArrayList<String>();

    public ArrayList<Integer> clientList = new ArrayList<Integer>();

    private int maxConnections = 3;

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
            TryToDeliverBufferedMessages();  
        }
    }

    // Fica ouvindo mensagens de busca de outros clientes enquanto emite sua mensagem de busca.
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
                    this.vectorClock = new TreeMap<Integer, Integer>(vectorClock);
                    
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

    // Envia uma mensagem de busca para os outros clientes.
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

    // Recebe um vector clock em formato de string e o transforma em um map.
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

    // Desmonta a mensagem recebida e age de acordo.
    // As mensagens seguem o seguinte padrão:
    // Mensagem de join: USRJOIN_port do usuario
    // Mensagem normal: USRMSG_port do usuario_mensagem_relógio vetorial
    private void ParseMessage(String message)
    {
        String[] splittedMessage = message.split("_");

        if (splittedMessage[1].equals(this.causalMulticast.clientPort.toString()))
        {
            return;
        }
        
        if (splittedMessage[0].equals("USRJOIN"))
        {
            return;
        }
        else if (splittedMessage[0].equals("USRMSG"))
        {
            Map<String, Integer> messageVectorClock = ExtractMessageVectorClock(splittedMessage[3]);
            System.out.println();
            System.out.println("Relogio vetorial recebido: " + messageVectorClock.toString());        
            System.out.println("Relogio vetorial do receptor: " + this.vectorClock.toString());

            if (CanDeliverMessage(messageVectorClock))
            {
                causalMulticast.client.deliver(splittedMessage[1] + " - " + splittedMessage[2]);
                this.vectorClock.put(Integer.parseInt(splittedMessage[1]), this.vectorClock.get(Integer.parseInt(splittedMessage[1])) + 1);      
            } else {
                System.out.println("Mensagem" + splittedMessage[2] + "adicionada ao buffer.");
                messageBuffer.add(message);
            }

            System.out.println();
            System.out.println("Relogio vetorial atual: " + this.vectorClock.toString());
            System.out.println("Mensagens no buffer de recebimento: " + messageBuffer.toString());
        }
    }

    // Verifica se a mensagem pode ser entregue de acordo com o algoritmo do vector clock.
    private Boolean CanDeliverMessage(Map<String, Integer> messageVectorClock)
    {
        for (Map.Entry<String, Integer> entry : messageVectorClock.entrySet())
        {
            if (entry.getValue() > this.vectorClock.get(Integer.parseInt(entry.getKey())))
            {
                return false;
            }
        }
        return true;
    }
    
    // Tenta entregar as mensagens que estão no buffer de recebimento.
    private void TryToDeliverBufferedMessages() {
        synchronized (this) {
            Iterator<String> iterator = messageBuffer.iterator();
            while (iterator.hasNext()) {
                String message = iterator.next();
                String[] splittedMessage = message.split("_");
                Map<String, Integer> messageVectorClock = ExtractMessageVectorClock(splittedMessage[3]);
                if (CanDeliverMessage(messageVectorClock)) {
                    causalMulticast.client.deliver(splittedMessage[1] + " - " + splittedMessage[2]);
                    this.vectorClock.put(Integer.parseInt(splittedMessage[1]), this.vectorClock.get(Integer.parseInt(splittedMessage[1])) + 1);
                    iterator.remove();
                }
            }
        }
    }
}