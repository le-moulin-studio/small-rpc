package com.lemoulinstudio.small.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Vincent Cantin
 */
public class SmallRpcCodecFactory implements ProtocolCodecFactory {
  
  private SmallRpcEncoder encoder = new SmallRpcEncoder();
  private SmallRpcDecoder decoder = new SmallRpcDecoder();

  @Override
  public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    return encoder;
  }

  @Override
  public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    return decoder;
  }
  
}
