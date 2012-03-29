package limone.websocket.server;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Most utterly basic WebSocket handler ever.  Just used as a proof of concept.  Shamelessly ripped off from http://jfarcand.wordpress.com/.
 * @author Michael Laccetti
 */
public class BasicWebSocketHandler extends WebSocketHandler {
  protected static final Logger log = LoggerFactory.getLogger(BasicWebSocketHandler.class);
  protected AtmosphereResource r;
  
  public BasicWebSocketHandler() {
    log.debug("Basic WebSocket handler coming online.");
  }
  
  
  @Override
  public void onTextMessage(WebSocket webSocket, String message) {
    log.debug("Received message: {}", message);

    AtmosphereResource r = webSocket.resource();
    Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());

    if (message != null && message.indexOf("message") != -1) {
      b.broadcast(message.substring("message=".length()));
    }
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    log.debug("WebSocket client connected.");
    
    // Accept the handshake by suspending the response.
    r = webSocket.resource();
    // Create a Broadcaster based on the path
    Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());
    r.setBroadcaster(b);
    r.addEventListener(new WebSocketEventListenerAdapter());
    r.suspend(-1);
    
    // For testing purposes, we'll start a broadcast as soon as we've been poked
    new Thread() {
      @Override
      public void run() {
        try {
          // We're going to wait a while just because we want to add some space in the logs,
          // not because it is necessary.
          sleep(2000);
        } catch (InterruptedException e) {
          // we really don't care...
        }
        BasicWebSocketHandler.this.broadcastMessage("You connected, I see you!");
      }
    }.start();
  }

  @Override
  public void onClose(WebSocket webSocket) {
    log.debug("WebSocket client disconnected.");
    super.onClose(webSocket);
  }


  /**
   * Retrieve the {@link Broadcaster} based on the request's path info.
   * 
   * @param pathInfo
   * @return the {@link Broadcaster} based on the request's path info.
   */
  private Broadcaster lookupBroadcaster(String pathInfo) {
    String[] decodedPath = pathInfo.split("/");
    Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
    return b;
  }

  /**
   * Used to send a message to whatever is connected on the other side.
   * 
   * @param message
   *          The message to send.
   */
  public void broadcastMessage(String message) {
    log.debug("Sending message: {}", message);
    r.getBroadcaster().broadcast(message);
  }
}