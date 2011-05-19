package com.lemoulinstudio.small.mina;

import java.nio.ByteBuffer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * 
 * @author Vincent Cantin
 */
public class SmallRpcEncoder implements ProtocolEncoder {

  @Override
  public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
    ByteBuffer byteBuffer = (ByteBuffer) message;
    int contentLength = byteBuffer.capacity();
    
    IoBuffer packet = IoBuffer.allocate(4 + contentLength);
    
    packet.putInt(contentLength);
    packet.put(byteBuffer);
    packet.rewind();
    
    out.write(packet);
  }

  @Override
  public void dispose(IoSession session) throws Exception {
  }
  
}
