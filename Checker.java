package proxies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Checker implements Runnable {
    String IP_CHECK_SITE = "http://api.ipify.org/";
    int TIMEOUT = 4000;
    int threadCount = 10;
    Proxies p;
    
    public Checker(int threadCount, Proxies p){
        this.threadCount = threadCount;
        this.p = p;
    }
    
    public Checker(int threadCount, int timeout, Proxies p){
        this.TIMEOUT = timeout;
        this.threadCount = threadCount;
        this.p = p;
    }

    @Override
    public void run() {
        Thread[] threads = new Thread[threadCount];
        for (int c = 0; c < threadCount; c++) {
            Thread t = new Thread(new Check(this));
            t.start();
            threads[c] = t;
        }
        try {
            for (Thread t : threads) {
                if (t != null) {
                    t.join();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Checker.class.getName()).log(Level.SEVERE, null, ex);
        }
//        while(true){
//            Collection<ProxyClass> s = p.proxies.values();
//            synchronized(p.proxies){
//                Iterator i = s.iterator();
//                Thread[] threads = new Thread[threadCount];
//                int count = 0;
//                while(i.hasNext()){
//                    ProxyClass p = (ProxyClass) i.next();
//                    System.out.println("Checking " + p);
//                    if(System.currentTimeMillis() - p.last_checked >= 300000 && !p.dead){ //can delete dead
//                        Check c = new Check(p);
//                        Thread t = new Thread(c);
//                        t.start();
//                        threads[count] = t;
//                        count++;
//                    }
//                    if(count == threadCount-1){
//                        for(Thread t : threads){
//                            try {
//                                if(t != null)
//                                    t.join();
//                            } catch (InterruptedException ex) {
//                                Logger.getLogger(Checker.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                        count = 0;
//                    }
//                }
//            }
//        }
    }
    
    class Check implements Runnable {
//        public Check(ProxyClass p){
//            this.p = p;
//            proxy = new Proxy(p.type, new InetSocketAddress(p.proxy.split(":")[0], Integer.parseInt(p.proxy.split(":")[1])));
//            //System.out.println("Check " + p);
//        }
        Checker c;
        public Check(Checker c){
            this.c = c;
        }
        
        public void run() {
            //System.out.println("Run Check " + p);
            while(true){
                ProxyClass checkProxy = c.p.getProxyCheck();
                //System.out.println(Thread.currentThread() + " " + checkProxy);
                checkProxy.last_checked = System.currentTimeMillis();
                Proxy proxy = proxy = new Proxy(checkProxy.type, new InetSocketAddress(checkProxy.proxy.split(":")[0], Integer.parseInt(checkProxy.proxy.split(":")[1])));
                try {
                    URL url = new URL(IP_CHECK_SITE);
                    long start = System.currentTimeMillis();
                    HttpURLConnection connect = (HttpURLConnection) url.openConnection(proxy);
                    connect.setReadTimeout(TIMEOUT);
                    connect.setConnectTimeout(TIMEOUT);
                    connect.setUseCaches(false);
                    BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                    long finish = System.currentTimeMillis();
                    checkProxy.last_checked = System.currentTimeMillis();
                    checkProxy.ping = (short) (finish - start);
                    String res = br.readLine();
                    checkProxy.exitAddress = res;
                    System.out.println("Worked: " + checkProxy);
                    return;
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Checker.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    //System.out.println(Thread.currentThread() + " " + ex);
                    //System.out.println("connection timeout");
                }
                checkProxy.dead = true;
            }
        }
    }
    
}
