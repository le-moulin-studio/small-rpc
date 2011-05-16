package com.lemoulinstudio.small.jse;

import java.nio.ByteBuffer;

/**
 * An interface used by a Small session to output the encoded function calls.
 *
 * You can receive the encoded data of the function calls made via Small by
 * using an instance that implements this interface and by registering it in
 * the Small session via the setMessageOutputListener() function.
 *
 * @author Vincent Cantin
 */
public interface MessageSender {

  /**
   * Notifies that a Small session wants the binary message specified in argument
   * to be sent to his destination.
   *
   * @param binaryMessage The binary message that have to be sent to his destination.
   */
  public void sendMessage(ByteBuffer binaryMessage);
  
}
