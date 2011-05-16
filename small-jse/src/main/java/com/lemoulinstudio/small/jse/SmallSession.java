package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.LocalService;
import com.lemoulinstudio.small.common.MessageSender;
import com.lemoulinstudio.small.common.RemoteService;
import java.nio.ByteBuffer;

/**
 * The class which represents a peer in a p2p communication in the Small system.
 *
 * @author Vincent Cantin
 */
public interface SmallSession {

  /**
   * Decodes an encoded message and interprets it via its corresponding function
   * on its targeted object.
   *
   * @param binaryMessage The message to be decoded and interpreted.
   */
  public void decodeAndExecute(ByteBuffer binaryMessage) throws Exception;

  /**
   * Sets the object which will be notified of the encoded binary messages to be sent.
   *
   * @param messageListener The object which will get notified of each
   * new binary messages to be sent.
   */
  public void setMessageSender(MessageSender messageSender);

  /**
   * Sets the caller object.
   *
   * This object will represent the source of the message each time a message
   * is being processed by the {@link #decodeAndExecute(java.nio.ByteBuffer)} method.
   *
   * The caller object is supposed to identify the host this session is communicating with.
   *
   * @param callerObject A reference to the caller object.
   */
  public void setCallerObject(Object callerObject);

  /**
   * Returns the caller object of this Small session.
   *
   * @return The caller object of this Small session.
   */
  public Object getCallerObject();

  /**
   * Binds a service.
   *
   * @param <T> The type of the local interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param localObject The local object that you want to expose to the remote host.
   *
   * @param localInterface The class object which represents
   * the local interface which is the super type of the local object.
   *
   * @see #unbind(com.lemoulinstudio.small.common.LocalService)
   *
   */
  public <T extends LocalService> void bind(T service, Class<T> serviceInterface);

  /**
   * Unbinds a service.
   *
   * After calling this function, the local object is no longer exposed to the remote host.
   * 
   * @param localObject The local object to be unbound.
   * 
   * @see #bind(com.lemoulinstudio.small.common.LocalService, Class<com.lemoulinstudio.small.common.LocalService>)
   */
  public void unbind(LocalService service);

  /**
   * Creates a proxy and binds it to a new Id.
   *
   * @param <T> The type of the remote interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param remoteInterface The class object of a remote interface which is
   * the super type of the remote object we want a proxy from.
   *
   * @return The proxy to a remote object.
   */
  public <T extends RemoteService> T createProxy(Class<T> serviceInterface);

  /**
   * Releases a proxy and its associated Id.
   *
   * @param <T> The type of the proxy.
   * 
   * @param proxy The proxy to be released.
   */
  public <T extends RemoteService> void releaseProxy(T proxy);

}
