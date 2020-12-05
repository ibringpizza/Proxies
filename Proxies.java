package proxies;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Proxies{
    public static HashMap<String, Set<String>> proxies = new HashMap<>();
    Sources s = new Sources(this);
    
    private static Proxies p;
    
    public Proxies(){
        s.addSource("sslproxies.org", "https://sslproxies.org", 585000, false, (d, j) -> {
            List<String> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                proxies = Arrays.asList(m.group(2).split("\n"));
            else{
                System.out.println("regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("free-proxy-list.net", "https://free-proxy-list.net", 585000, false, (d, j) -> {
            List<String> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                proxies = Arrays.asList(m.group(2).split("\n"));
            else{
                System.out.println("regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("us-proxy.org", "https://us-proxy.org", 585000, false, (d, j) -> {
            List<String> proxies = new ArrayList<>();
            String pattern = "(Updated at 20[0-9 \\-\\:a-z\\.]+)\n\n([0-9\\:\\.\n\\- A-Z]+)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(d);
            if(m.find())
                proxies = Arrays.asList(m.group(2).split("\n"));
            else{
                System.out.println("regex didn't find");
                return null;
            }
            return proxies;
        });
        
        s.addSource("proxy-list.download", "https://www.proxy-list.download/api/v0/get?l=en&t=https", 86300000, true, (d, j) -> {
            List<String> proxies = new ArrayList<>();
            JsonArray full = (JsonArray) j;
            JsonArray list = full.getObject(0).getArray("LISTA");
            for(int i = 0;i<list.size();i++){
                JsonObject proxy = list.getObject(i);
                proxies.add(proxy.getString("IP") + ":" + proxy.getString("PORT"));
            }
            return proxies;
        });
        
        s.addSource("proxynova.com", "https://www.proxynova.com/proxy-server-list/anonymous-proxies/", 59000, false, (d, j) -> {
            List<String> proxies = new ArrayList<>();
            String pattern = "<abbr title=\"([0-9\\\\.]+)\">.+?<td align=\"left\">\\n[ ]+([0-9]{2,5})\\n[ ]+<\\/td>";
            Pattern proxy = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = proxy.matcher(d);
            while(m.find())
                proxies.add(String.format("%s:%s", m.group(1), m.group(2)));
            return proxies;
        });
        s.startAll();
    }
    
    public static void main(String[] args) {
        p = new Proxies();
    }
}
