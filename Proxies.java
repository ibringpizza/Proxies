package proxies;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Proxies{
    public static Map<String, ProxyClass> proxies = Collections.synchronizedMap(new HashMap<>());
    Sources s = new Sources(this);
    
    Comparator<ProxyClass> oldest = (ProxyClass p1, ProxyClass p2) -> {
                return p1.last_checked < p2.last_checked ? 1 : -1;
            };
    
    private static Proxies p;
    
    int thread_count = 10;
    
    public Proxies(int thread_count){
        this.thread_count = thread_count;
        
        s.addSource("sslproxies.org", "https://sslproxies.org", 585000, false, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                Arrays.asList(m.group(2).split("\n")).stream().forEach(ip -> proxies.add(new ProxyClass(ip)));
            else{
                System.out.println("regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("free-proxy-list.net", "https://free-proxy-list.net", 585000, false, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                Arrays.asList(m.group(2).split("\n")).stream().forEach(ip -> proxies.add(new ProxyClass(ip)));
            else{
                System.out.println("free-proxy-list.net - regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("us-proxy.org", "https://us-proxy.org", 585000, false, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                Arrays.asList(m.group(2).split("\n")).stream().forEach(ip -> proxies.add(new ProxyClass(ip)));
            else{
                System.out.println("regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("proxy-list.download", "https://www.proxy-list.download/api/v0/get?l=en&t=https", 86300000, true, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            JsonArray full = (JsonArray) j;
            JsonArray list = full.getObject(0).getArray("LISTA");
            for(int i = 0;i<list.size();i++){
                JsonObject proxy = list.getObject(i);
                proxies.add(new ProxyClass(String.format("%s:%s", proxy.getString("IP"), proxy.getString("PORT")), "HTTP"));
            }
            return proxies;
        });
        
        s.addSource("proxynova.com", "https://www.proxynova.com/proxy-server-list/anonymous-proxies/", 59000, false, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            String pattern = "<abbr title=\"([0-9\\\\.]+)\">.+?<td align=\"left\">\\n[ ]+([0-9]{2,5})\\n[ ]+<\\/td>";
            Pattern proxy = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = proxy.matcher(d);
            while(m.find())
                proxies.add(new ProxyClass(String.format("%s:%s", m.group(1), m.group(2)), "HTTP"));
            return proxies;
        });
        
        s.addSource("proxyscrape.com", "https://api.proxyscrape.com/?request=displayproxies&proxytype=https&timeout=5000&anonymity=all&ssl=all", 60000, false, (d, j) -> {
            List<ProxyClass> proxies = new ArrayList<>();
            Arrays.asList(d.split("\n")).stream().forEach(p -> proxies.add(new ProxyClass(p, "HTTP")));
            return proxies;
        });
        
        s.startAll();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Proxies.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(proxies.size());
        Checker check = new Checker(this.thread_count, this);
        Thread checkMain = new Thread(check);
        checkMain.start();
    }
    
    public ProxyClass getProxy(String... args){
        synchronized(proxies){
            //return proxies.remove(Collections.min(proxies.values()).proxy); //min for lowest ping. See compareTo() in Proxy
            ProxyClass temp = Collections.min(proxies.values());
            temp.last_used = System.currentTimeMillis();
            return temp;
        }
    }
    
    public ProxyClass getProxyCheck(){
        synchronized(proxies){
            return Collections.max(proxies.values(), oldest);
        }
    }
}
