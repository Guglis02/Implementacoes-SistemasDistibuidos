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
        System.out.println(msg);
    }

    public static void main(String[] args) {
        Client client = new Client(args[0], Integer.parseInt(args[1]));
        Scanner scanner = new Scanner(System.in);

        while(true){
            String msg = scanner.nextLine();
            client.causalMulticast.mcsend(msg, client);
        }        
    }
}
