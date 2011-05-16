package com.lemoulinstudio.small.apt.generator;

import com.lemoulinstudio.small.jse.SmallSessionImpl;
import com.lemoulinstudio.small.jse.AbstractConfiguration;
import com.lemoulinstudio.small.jse.Proxy;
import com.lemoulinstudio.small.jse.RootDecoder;

/**
 *
 * @author Vincent Cantin
 */
public class JseCodeGenerator extends JavaCodeGenerator {

  public JseCodeGenerator() {
    super(SmallSessionImpl.class,
            AbstractConfiguration.class,
            Proxy.class,
            RootDecoder.class,
            "",
            "");
  }

}
