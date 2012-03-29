package limone.websocket.client;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public final class WebsocketClient {
  protected static final Logger log      = LoggerFactory.getLogger(WebsocketClient.class);

  private WebSocket             ws;
  protected AtomicBoolean       complete = new AtomicBoolean(false);

  public static void main(String[] args) {
    WebsocketClient c = new WebsocketClient();
    if (c.connect("ws://localhost:80/aahce/sample")) {
      c.sendMessage("We have connection!");

      // Sit and wait until the server says something back.
      while (!c.isComplete()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // whatever
        }
      }
    }

    c.disconnect();
    System.exit(0);
  }

  public WebsocketClient() {
    log.debug("Instantiating Websocket Client.");
    complete.set(Boolean.FALSE);
  }

  public boolean connect(String wsUrl) {
    log.debug("Connecting to {}.", wsUrl);

    AsyncHttpClient c = new AsyncHttpClient();
    try {
      ws = c.prepareGet(wsUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketTextListener() {
        @Override
        public void onOpen(WebSocket websocket) {
          log.debug("Socket opened.");
        }

        @Override
        public void onClose(WebSocket websocket) {
          log.debug("Socket closed.");
        }

        @Override
        public void onError(Throwable t) {
          log.error("Error.", t);
        }

        @Override
        public void onMessage(String message) {
          log.debug("Message from server: {}", message);
          WebsocketClient.this.setComplete(true);
        }

        @Override
        public void onFragment(String fragment, boolean last) {
          // empty
        }
      }).build()).get();
      return true;
    } catch (Exception ex) {
      log.error("Could not connect to WS server.", ex);
      return false;
    }
  }

  public Boolean isComplete() {
    return complete.get();
  }
  
  public void setComplete(boolean state) {
    complete.set(state);
  }

  public void sendMessage(String message) {
    log.debug("Sending a message to the server: {}", message);
    ws.sendTextMessage(message);
  }

  public void disconnect() {
    log.debug("Disconnecting.");
    if (ws != null && ws.isOpen()) {
      ws.close();
    }
  }
}