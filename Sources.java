package proxies;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sources {
    
    Proxies p;
    
    public Sources(Proxies p){
        this.p = p;
    }
    
    public interface Parse {
        public List<String> process(String t, Object obj);
    }
    
    public class Source{
        int interval;
        String url;
        boolean go = false;
        Parse parse;
        boolean json = false;

        public Source(String url, int interval, boolean json, Parse parse){
            this.interval = interval;
            this.url = url;
            this.parse = parse;
            this.json = json;
        }

        private String getDoc(){
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(this.url);
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException ex) {
                Logger.getLogger(Proxies.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return sb.toString();
        }
        
        private Object getJSON(String doc){
            Object obj = null;
            try {
                obj = JsonParser.any().from(doc);
            } catch (JsonParserException ex) {
                Logger.getLogger(Sources.class.getName()).log(Level.SEVERE, null, ex);
            }
            return obj;
        }

        public void scan(){
            new Thread(){
                @Override
                public void run(){
                    while(go){
                        String doc = getDoc();
                        Object obj = null;
                        List<String> proxies;
                        if(json) obj = getJSON(doc);
                        proxies = parse.process(doc, obj);
                        addProxies(proxies);
//                        System.out.println("Thread-" + Thread.currentThread().getId());
//                        System.out.println(proxies);
//                        System.out.println(p.proxies.get("all").size());
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Sources.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }.start();
        }

        public void setGo(boolean go){
            this.go = go;
        }
    }
    
    protected final LinkedHashMap<String, Source> sources = new LinkedHashMap<>();
    
    public void addSource(String name, String url, int interval, boolean json, Parse p){
        addSource(name, new Source(url, interval, json, p));
    }
    
    public void addSource(String name, Source s){
        sources.put(name, s);
    }
    
    public void addProxies(List<String> list){
        synchronized(p.proxies){
            for(String e : list){
                String[] parts = e.split("\\|");
                String key = parts.length > 1 ? parts[1] : "all";
                String proxy = parts[0];
                if(p.proxies.containsKey(key)){
                    p.proxies.get(key).add(proxy);
                }else{
                    p.proxies.put(key, new HashSet<>());
                    p.proxies.get(key).add(proxy);
                }
            }
        }
    }
    
    public void startAll(){
        for(Entry<String, Source> e : sources.entrySet()){
            e.getValue().setGo(true);
            e.getValue().scan();
        }
    }
}
