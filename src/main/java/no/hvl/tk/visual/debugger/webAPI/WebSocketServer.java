package no.hvl.tk.visual.debugger.webAPI;

import org.glassfish.tyrus.server.Server;

import java.util.HashMap;


public class WebSocketServer {

    public static void runServer() {
        final Server server = new Server("localhost", 8080, "", new HashMap<>(), WebSocketEndpoint.class);
        try {
            server.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}