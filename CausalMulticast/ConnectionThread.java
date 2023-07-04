package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class ConnectionThread extends Thread {
    private CausalMulticast causalMulticast;

    private final CountDownLatch latch;

    private final int minConnections = 3;

    public ConnectionThread(CausalMulticast causalMulticast)
    {
        this.causalMulticast = causalMulticast;

        this.latch = new CountDownLatch(minConnections);
    }

    @Override
    public void run()
    {
        SendSearchMessage(this.causalMulticast.clientPort.toString());
        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);

        while (true)
        {
            try {
                this.causalMulticast.multicastSocket.receive(receivePacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String[] splitMessage = message.split("_");

            if (splitMessage[0].equals("USRJOIN"))
            {
                Integer clientPort = Integer.parseInt(splitMessage[1]);
                if (!causalMulticast.clientList.contains(clientPort))
                {
                    causalMulticast.clientList.add(clientPort);
                    causalMulticast.vectorClock.put(clientPort, 0);
                    causalMulticast.ReorderVectorClock();
                    latch.countDown();

                    System.out.println(clientPort + " entrou no grupo.");
                }
                else
                {
                    continue;
                }

                if (causalMulticast.clientList.size() < minConnections)
                {
                    causalMulticast.vectorClock.put(clientPort, Integer.parseInt(splitMessage[2]));

                    System.out.println("Usuarios atuais no grupo: " + causalMulticast.clientList);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                SendSearchMessage(this.causalMulticast.clientPort.toString(), this.causalMulticast.vectorClock.get(causalMulticast.clientPort));
            }
        }
    }

    public void WaitForUsers() throws InterruptedException {
        latch.await();
        System.out.println("Usuários conectados! Iniciando conversa.");
    }

    // Envia uma mensagem de busca para os outros clientes.
    private void SendSearchMessage(String message)
    {
        SendSearchMessage(message, 0);
    }

    // Envia uma mensagem de busca para os outros clientes.
    private void SendSearchMessage(String message, int messageCount)
    {
        String searchMessage = String.format("USRJOIN_%s_%d", message, messageCount);
        DatagramPacket sendPacket = new DatagramPacket(searchMessage.getBytes(), searchMessage.length(), this.causalMulticast.multicastAddress, CausalMulticast.multicastPort);
        try {
            this.causalMulticast.multicastSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
