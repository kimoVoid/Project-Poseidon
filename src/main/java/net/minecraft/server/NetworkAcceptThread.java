package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

class NetworkAcceptThread extends Thread {

    final MinecraftServer a;

    final NetworkListenThread b;

    NetworkAcceptThread(NetworkListenThread networklistenthread, String s, MinecraftServer minecraftserver) {
        super(s);
        this.b = networklistenthread;
        this.a = minecraftserver;
    }

    public void run() {
        while (this.b.b) {
            try {
                Socket socket = NetworkListenThread.a(this.b).accept();

                if (socket != null) {
                    /*
                    InetAddress inetaddress = socket.getInetAddress();

                    if (hashmap.containsKey(inetaddress) && !"127.0.0.1".equals(inetaddress.getHostAddress()) && System.currentTimeMillis() - ((Long) hashmap.get(inetaddress)).longValue() < 5000L) {
                        hashmap.put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                        socket.close();
                    } else {
                        hashmap.put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                        NetLoginHandler netloginhandler = new NetLoginHandler(this.a, socket, "Connection #" + NetworkListenThread.b(this.b));

                        NetworkListenThread.a(this.b, netloginhandler);
                    }
                     */

                    synchronized (this.b.i) {
                        InetAddress var3 = socket.getInetAddress();
                        if (this.b.i.containsKey(var3) && System.currentTimeMillis() - (Long)this.b.i.get(var3) < 5000L) {
                            this.b.i.put(var3, System.currentTimeMillis());
                            socket.close();
                            continue;
                        }

                        this.b.i.put(var3, System.currentTimeMillis());
                    }

                    NetLoginHandler netloginhandler = new NetLoginHandler(this.a, socket, "Connection #" + NetworkListenThread.b(this.b));
                    NetworkListenThread.a(this.b, netloginhandler);
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }
    }
}
