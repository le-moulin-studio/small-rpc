package com.lemoulinstudio.small.mina;

import com.lemoulinstudio.small.AbstractConfiguration;
import com.lemoulinstudio.small.LocalService;
import com.lemoulinstudio.small.MessageSender;
import com.lemoulinstudio.small.SmallSession;
import com.lemoulinstudio.small.SmallSessionImpl;
import java.nio.ByteBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author Vincent Cantin
 */
public final class SmallRpcHandler extends IoHandlerAdapter {
  
  private final AbstractConfiguration configuration;
  private final SmallRpcSessionListener sessionListener;
  private final LocalService[] initialServices;

  private final AttributeKey SMALL_SESSION = new AttributeKey(getClass(), "smallSession");

  public SmallRpcHandler(AbstractConfiguration configuration, LocalService ... initialServices) {
    this(configuration, new SmallRpcSessionListenerAdapter(), initialServices);
  }

  public SmallRpcHandler(AbstractConfiguration configuration,
          SmallRpcSessionListener sessionListener,
          LocalService ... initialServices) {
    this.configuration = configuration;
    this.sessionListener = sessionListener;
    this.initialServices = initialServices;
  }

  @Override
  public void sessionCreated(final IoSession session) throws Exception {
    SmallSession smallSession = new SmallSessionImpl(configuration);
    
    smallSession.setMessageSender(new MessageSender() {
      @Override
      public void sendMessage(ByteBuffer binaryMessage) {
        session.write(binaryMessage);
      }
    });
    
    for (LocalService service : initialServices)
      smallSession.bind(service, (Class<LocalService>) service.getClass().getInterfaces()[0]);
    
    session.setAttribute(SMALL_SESSION, smallSession);
    
    //smallSession.setCallerObject(...);
    sessionListener.sessionCreated(smallSession);
  }

  @Override
  public void sessionOpened(IoSession session) throws Exception {
    final SmallSession smallSession = getSmallSession(session);
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        sessionListener.sessionOpened(smallSession);
      }
    }).start();
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    final SmallSession smallSession = getSmallSession(session);
    session.removeAttribute(SMALL_SESSION);
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        sessionListener.sessionClosed(smallSession);
      }
    }).start();
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    System.err.println(cause.toString());
    session.close(false);
  }
  
  private SmallSession getSmallSession(IoSession session) {
    return (SmallSession) session.getAttribute(SMALL_SESSION);
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    getSmallSession(session).decodeAndExecute((ByteBuffer) message);
  }
  
}
