package proxies;

import java.net.Proxy;
import java.net.Proxy.Type;

public class ProxyClass implements Comparable<ProxyClass> {
    public short ping = (short) (Math.pow(2, 15)-1);
    public String proxy;
    public String exitAddress = "";
    private double uptime;
    public int times_checked;
    public int times_up;
    public int failed_consecutive; //more than n, delete proxy
    public Type type;
    public String anon; //Anonymous, elite, transparent
    public String CO; //Country code
    public boolean dead = false;
    public long last_checked = 0;
    public long last_used = 0;
    
    public int compareTo(ProxyClass p){
        return p.ping < this.ping && System.currentTimeMillis() - p.last_checked < 30000 && System.currentTimeMillis() - p.last_used > 30000 ? 1 : -1; //checked less than 30 seconds ago, last used more than 30 seconds
    }
    
    public double getUptime(){
        return (times_up/times_checked)*100D;
    }
    
    public ProxyClass(String proxy, String... args){ //sets proxy, type, anon, CO. in that order
        this.proxy = proxy;
        type = Proxy.Type.valueOf("HTTP");
        for(int i = 0;i<args.length;i++){
            switch(i){
                case 0:
                    type = Proxy.Type.valueOf(args[i]);
                    break;
                case 1:
                    anon = args[i];
                    break;
                case 2:
                    CO = args[i];
                    break;
            }
        }
    }
    
    public String toString(){
        return String.format("%s %dms dead:%b lastChecked:%d", proxy, ping, dead, last_checked);
    }
}
