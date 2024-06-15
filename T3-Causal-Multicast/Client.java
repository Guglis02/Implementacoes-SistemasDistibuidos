import CausalMulticast.ICausalMulticast;
import CausalMulticast.CausalMulticast;

import java.util.Scanner;

public class Client implements ICausalMulticast{
    CausalMulticast causalMulticast;

    public Client(String ip, Integer port)
    {
        try {
            this.causalMulticast = new CausalMulticast(ip, port, this);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void deliver(String msg){
        System.out.println("Mensagem recebida: " + msg);
    }

    public static void main(String[] args) {
        Client client;
        
        client = new Client("228.0.0.1", Integer.parseInt(args[0]));

        Scanner scanner = new Scanner(System.in);

        while(true)
        {
            String msg = scanner.nextLine();
            client.causalMulticast.mcsend(msg, client);
        }        
    }
}
