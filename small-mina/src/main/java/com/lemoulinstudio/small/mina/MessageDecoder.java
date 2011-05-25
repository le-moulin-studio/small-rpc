package com.lemoulinstudio.small.mina;

import java.nio.ByteBuffer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * 
 * @author Vincent Cantin
 */
class MessageDecoder implements ProtocolDecoder {

  private final AttributeKey CONTEXT = new AttributeKey(getClass(), "context");

  private Context getContext(IoSession session) {
    Context ctx = (Context) session.getAttribute(CONTEXT);

    if (ctx == null) {
      ctx = new Context();
      session.setAttribute(CONTEXT, ctx);
    }

    return ctx;
  }

  @Override
  public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
    getContext(session).decode(in, out);
  }

  @Override
  public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
  }

  @Override
  public void dispose(IoSession session) throws Exception {
    Context ctx = (Context) session.getAttribute(CONTEXT);

    if (ctx != null)
      session.removeAttribute(CONTEXT);
  }

  private static class Context {
    
    private static enum DecodingState {
      MessageSize,
      MessageContent
    }
    
    DecodingState decodingState = DecodingState.MessageSize;
    int messageLength = 0;
    int nbBytesToRead = 4;
    ByteBuffer outByteBuffer;

    public void decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
      
      while (in.hasRemaining()) {
        if (decodingState == DecodingState.MessageSize) {
          do {
            messageLength <<= 8;
            messageLength |= in.get() & 0xff;
            nbBytesToRead--;
            if (nbBytesToRead == 0) {
              decodingState = DecodingState.MessageContent;
              nbBytesToRead = messageLength;
              outByteBuffer = ByteBuffer.allocate(messageLength);
              break;
            }
          } while (in.hasRemaining());
        }

        if (decodingState == DecodingState.MessageContent) {
          int length = Math.min(nbBytesToRead, in.remaining());
          outByteBuffer.put(in.getSlice(length).buf());
          nbBytesToRead -= length;

          // If we receive the whole message content ..
          if (nbBytesToRead == 0) {
            // We send the buffer.
            outByteBuffer.rewind();
            out.write(outByteBuffer);
            outByteBuffer = null;

            // Re-init decoding state variable.
            decodingState = DecodingState.MessageSize;
            messageLength = 0;
            nbBytesToRead = 4;
          }
        }
      }
    }
  }
}
