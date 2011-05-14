package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.BindToLocalId;
import com.lemoulinstudio.small.common.BindToSharedId;
import com.lemoulinstudio.small.common.Local;
import com.lemoulinstudio.small.common.MessageListener;
import com.lemoulinstudio.small.common.Remote;
import com.lemoulinstudio.small.common.Singleton;
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
  public void decodeAndExecute(ByteBuffer binaryMessage);

  /**
   * Sets the object which will be notified of the encoded binary messages to be sent.
   *
   * @param messageListener The object which will get notified of each
   * new binary messages to be sent.
   */
  public void setMessageListener(MessageListener messageListener);

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
   * Registers a view factory.
   *
   * @param viewFactory the factory.
   */
  public void registerViewFactory(ViewFactory<?> viewFactory);

  /**
   * Registers the link between the <code>view</code> and the object
   * provided by its function {@link View#getViewedObject()}.
   *
   * The viewed object is normally a proxy which represents an object located on
   * a remote host. Small use the link between them to replace 1 object by its
   * view or vice-versa.
   *
   * @param view A local object that represent a view of a remote object.
   *
   * @see #unregisterView(com.lemoulinstudio.small.jse.View)
   */
  public void registerView(View<?> view);

  /**
   * Unregisters the link between the <code>view</code> and the object
   * provided by its function {@link View#getViewedObject()}.
   *
   * The viewed object is normally a proxy which represents an object located on
   * a remote host. Small use the link between them to replace 1 object by its
   * view or vice-versa.
   *
   * @param view A local object that represent a view of a remote object.
   * 
   * @see #registerView(com.lemoulinstudio.small.jse.View)
   */
  public void unregisterView(View<?> view);

  /**
   * Binds a local singleton object. Singletons are identified by their type, not by an Id.
   *
   * @param <T> The type of the local interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param localObject The local object that you want to expose to the remote host.
   *
   * @param localInterface The class object which represents
   * the local interface which is the super type of the local object.
   *
   * @see #unbind(com.lemoulinstudio.small.common.Singleton)
   *
   */
  public <T extends Singleton & Local> void bind(T localObject, Class<T> localInterface);

  /**
   * Binds a local object to a new shared Id.
   *
   * The Id is chosen incrementally, so it is important to pay attention to
   * the order of the calls.
   *
   * @param <T> The type of the local interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param localObject The local object that you want to expose to the remote host.
   *
   * @param localInterface The class object which represents
   * the local interface which is the super type of the local object.
   *
   * @see #unbind(com.lemoulinstudio.small.common.BindToSharedId)
   *
   */
  public <T extends BindToSharedId & Local> void bind(T localObject, Class<T> localInterface);

  /**
   * Binds a local object to a new local Id.
   * 
   * The Id is chosen incrementally, so it is important to pay attention to
   * the order of the calls.
   *
   * @param <T> The type of the local interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param localObject The local object that you want to expose to the remote host.
   *
   * @param localInterface The class object which represents
   * the local interface which is the super type of the local object.
   *
   * @see #unbind(com.lemoulinstudio.small.common.BindToLocalId)
   */
  public <T extends BindToLocalId & Local> void bind(T localObject, Class<T> localInterface);

  /**
   * Unbinds a local object and releases its associated Id.
   *
   * After calling this function, the local object is no longer exposed to the remote host.
   * 
   * @param localObject The local object to be unbound.
   */
  public <T extends Singleton & Local> void unbind(T localObject);

  /**
   * Unbinds a local object and releases its associated Id.
   *
   * After calling this function, the local object is no longer exposed to the remote host.
   *
   * @param localObject The local object to be unbound.
   */
  public <T extends BindToSharedId & Local> void unbind(T localObject);

  /**
   * Unbinds a local object and releases its associated Id.
   *
   * After calling this function, the local object is no longer exposed to the remote host.
   *
   * @param localObject The local object to be unbound.
   */
  public <T extends BindToLocalId & Local> void unbind(T localObject);

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
  public <T extends Singleton & Remote> T createProxy(Class<T> remoteInterface);

  /**
   * Creates a proxy and binds it to a new Id.
   *
   * The Id is chosen incrementally, so it is important to pay attention to
   * the order of the calls.
   *
   * It is a good practice to only use this function at the establishment
   * of the communication between the 2 hosts, in a minimalist way, and to
   * get the additional proxies via the messages from the remote host.
   *
   * @param <T> The type of the remote interface. Normally, an interface
   * generated by the Small annotation processor.
   *
   * @param remoteInterface The class object of a remote interface which is
   * the super type of the remote object we want a proxy from.
   *
   * @return The proxy to a remote object.
   */
  public <T extends BindToLocalId & Remote> T createProxy(Class<T> remoteInterface);

  /**
   * Releases a proxy and its associated Id.
   *
   * @param <T> The type of the proxy.
   * 
   * @param proxy The proxy to be released.
   */
  public <T extends Singleton & Remote> void releaseProxy(T proxy);

  /**
   * Releases a proxy and its associated Id.
   *
   * @param <T> The type of the proxy.
   *
   * @param proxy The proxy to be released.
   */
  public <T extends BindToSharedId & Remote> void releaseProxy(T proxy);

  /**
   * Releases a proxy and its associated Id.
   *
   * @param <T> The type of the proxy.
   *
   * @param proxy The proxy to be released.
   */
  public <T extends BindToLocalId & Remote> void releaseProxy(T proxy);
  
}