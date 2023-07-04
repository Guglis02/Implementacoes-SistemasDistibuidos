package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.lang.System;
import java.util.concurrent.CountDownLatch;

// Classe que representa a thread que fica escutando as mensagens que chegam.
public class CommunicationThread extends Thread
{
    private CausalMulticast causalMulticast;

    public ArrayList<String> messageBuffer = new ArrayList<>();

    public CommunicationThread(CausalMulticast causalMulticast)
    {
        this.causalMulticast = causalMulticast;
    }

    @Override
    public void run()
    {
        while(true)
        {
            DatagramPacket unicastPacket = new DatagramPacket(new byte[1024], 1024);
            
            try {
                this.causalMulticast.unicastSocket.receive(unicastPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String unicastMsg = new String(unicastPacket.getData(), 0, unicastPacket.getLength());
            ParseMessage(unicastMsg);          
            TryToDeliverBufferedMessages();  
        }
    }

    // Recebe um vector clock em formato de string e o transforma em um map.
    Map<String, Integer> ExtractMessageVectorClock(String messageVectorClockString)
    {
        Map<String, Integer> messageVectorClock = new HashMap<>();
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
        String[] splitMessage = message.split("_");

        if (splitMessage[1].equals(this.causalMulticast.clientPort.toString()))
        {
            return;
        }

        if (splitMessage[0].equals("USRMSG"))
        {
            Map<String, Integer> messageVectorClock = ExtractMessageVectorClock(splitMessage[3]);
            System.out.println();
            System.out.println("Relogio vetorial recebido: " + messageVectorClock.toString());        
            System.out.println("Relogio vetorial do receptor: " + causalMulticast.vectorClock.toString());

            if (CanDeliverMessage(messageVectorClock))
            {
                causalMulticast.client.deliver(splitMessage[1] + " - " + splitMessage[2]);
                causalMulticast.vectorClock.put(Integer.parseInt(splitMessage[1]), causalMulticast.vectorClock.get(Integer.parseInt(splitMessage[1])) + 1);
            } else {
                System.out.println("Mensagem " + splitMessage[2] + " adicionada ao buffer.");
                messageBuffer.add(message);
            }

            System.out.println();
            System.out.println("Relogio vetorial atual: " + causalMulticast.vectorClock.toString());
            System.out.println("Mensagens no buffer de recebimento: " + messageBuffer.toString());
        }
    }

    // Verifica se a mensagem pode ser entregue de acordo com o algoritmo do vector clock.
    private Boolean CanDeliverMessage(Map<String, Integer> messageVectorClock)
    {
        for (Map.Entry<String, Integer> entry : messageVectorClock.entrySet())
        {
            if (entry.getValue() > causalMulticast.vectorClock.get(Integer.parseInt(entry.getKey())))
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
                String[] splitMessage = message.split("_");
                Map<String, Integer> messageVectorClock = ExtractMessageVectorClock(splitMessage[3]);
                if (CanDeliverMessage(messageVectorClock)) {
                    causalMulticast.client.deliver(splitMessage[1] + " - " + splitMessage[2]);
                    causalMulticast.vectorClock.put(Integer.parseInt(splitMessage[1]), causalMulticast.vectorClock.get(Integer.parseInt(splitMessage[1])) + 1);
                    iterator.remove();
                }
            }
        }
    }
}