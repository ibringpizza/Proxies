package proxies;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Proxies p = new Proxies(100);
        System.out.println("Created Proxies");
        while(true){
            System.out.println(p.getProxy());
            System.out.println("in loop");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Proxies.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
