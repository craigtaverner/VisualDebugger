package no.hvl.tk.visual.debugger.webAPI.endpoint;

import com.google.common.collect.Sets;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.debugging.NodeDebugVisualizer;
import no.hvl.tk.visual.debugger.debugging.concurrency.CounterBasedLock;
import no.hvl.tk.visual.debugger.debugging.visualization.DebuggingInfoVisualizer;
import no.hvl.tk.visual.debugger.domain.ODObject;
import no.hvl.tk.visual.debugger.util.DiagramToXMLConverter;

@ServerEndpoint("/debug")
public class WebSocketEndpoint {
    private static final Logger LOGGER = Logger.getInstance(WebSocketEndpoint.class);
    // One gets one instance of this class per session/client.

    @OnOpen
    public static void onOpen(final Session session) {
        LOGGER.info(String.format("Websocket session with id \"%s\" opened.", session.getId()));
        SharedState.addWebsocketClient(session);
    }

    @OnClose
    public static void onClose(final Session session) {
        LOGGER.info(String.format("Websocket session with id \"%s\" closed.", session.getId()));
        SharedState.removeWebsocketClient(session);
    }

    @OnMessage
    public static String handleTextMessage(final String objectId) {
        LOGGER.debug(String.format("New websocket message with content \"%s\" received.", objectId));

        final DebuggingInfoVisualizer debuggingInfoVisualizer = SharedState.getDebugListener()
                                                                           .getDebuggingInfoVisualizer();
        final Pair<ODObject, JavaValue> debugNodeAndObjectForObjectId = debuggingInfoVisualizer.getDebugNodeAndObjectForObjectId(objectId);
        if (debugNodeAndObjectForObjectId != null) {
            return loadChildren(debuggingInfoVisualizer, debugNodeAndObjectForObjectId);
        }
        return String.format("Object with id \"%s\" not found", objectId);
    }

    private static String loadChildren(
            final DebuggingInfoVisualizer debuggingInfoVisualizer,
            final Pair<ODObject, JavaValue> debugNodeAndObjectForObjectId) {
        final ODObject parent = debugNodeAndObjectForObjectId.getFirst();
        final JavaValue debugNode = debugNodeAndObjectForObjectId.getSecond();

        final WebsocketDebuggingInfoCollector collector = new WebsocketDebuggingInfoCollector(debuggingInfoVisualizer);
        // Add parent to object diagram.
        collector.addObjectAndCorrespondingDebugNode(parent, debugNode);

        // Load children
        final CounterBasedLock lock = new CounterBasedLock();
        new Thread(() -> {
            final NodeDebugVisualizer nodeDebugger = new NodeDebugVisualizer(
                    collector,
                    1,
                    lock,
                    parent,
                    "",
                    Sets.newHashSet());
            nodeDebugger.exploreObjectChildren(debugNode, nodeDebugger);
        }).start();

        // Wait for the node debug visualizer to have finished.
        lock.lock();
        return DiagramToXMLConverter.toXml(collector.getCurrentDiagram());
    }
}