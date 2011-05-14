package com.lemoulinstudio.small.jse;

import java.util.Map;
import com.lemoulinstudio.small.common.Remote;

/**
 * The super type of the config class generated by the annotation processor.
 *
 * @author Vincent Cantin
 */
public interface AbstractConfiguration {
  public Map<Class<? extends Remote>, Class> getRemoteClassToProxyClass();
  public RootDecoder getRootDecoder();
}
