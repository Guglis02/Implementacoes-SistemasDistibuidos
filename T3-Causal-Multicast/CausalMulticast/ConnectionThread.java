package CausalMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.CountDownLatch;

/**
 * Classe que representa uma thread de conexão.
 */
public class ConnectionThread extends Thread {
    private CausalMulticast causalMulticast;

    private final CountDownLatch latch;

    private final int minConnections = 3;

    /**
     * Construtor da classe ConnectionThread.
     * @param causalMulticast Uma instância de CausalMulticast.
     */
    public ConnectionThread(CausalMulticast causalMulticast)
    {
        this.causalMulticast = causalMulticast;

        this.latch = new CountDownLatch(minConnections);
    }

    /**
     * Método executado quando a thread é iniciada.
     */
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
                if (causalMulticast.clientList.contains(clientPort))
                {
                    continue;
                }
                                    
                causalMulticast.clientList.add(clientPort);
                causalMulticast.vectorClock.put(clientPort, Integer.parseInt(splitMessage[2]));
                causalMulticast.ReorderVectorClock();
                latch.countDown();

                System.out.println(clientPort + " entrou no grupo.");

                if (causalMulticast.clientList.size() < minConnections)
                {
                    causalMulticast.vectorClock.put(clientPort, Integer.parseInt(splitMessage[2]));

                    System.out.println("Usuários atuais no grupo: " + causalMulticast.clientList);
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

    /**
     * Aguarda até que o número mínimo de usuários esteja conectado.
     * @throws InterruptedException Se a thread for interrompida enquanto aguarda.
     */
    public void WaitForUsers() throws InterruptedException {
        latch.await();
        System.out.println("Usuários conectados! Iniciando conversa.");
    }

    /**
     * Envia uma mensagem de busca para os outros clientes.
     * @param message A mensagem a ser enviada.
     */
    private void SendSearchMessage(String message)
    {
        SendSearchMessage(message, 0);
    }

    /**
     * Envia uma mensagem de busca para os outros clientes.
     * @param message A mensagem a ser enviada.
     * @param messageCount O contador de mensagem.
     */
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
