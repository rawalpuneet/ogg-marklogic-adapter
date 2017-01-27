package oracle.goldengate.delivery.handler.marklogic;

import oracle.goldengate.datasource.*;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;
import java.util.Properties;

import oracle.goldengate.datasource.conf.DsHandler;
import oracle.goldengate.datasource.factory.DataSourceFactory;
import oracle.goldengate.util.PropertyWrapper;
/**
 * Created by prawal on 1/25/17.
 */
public class Tester {

  public static void main(String args[]) throws IOException {
      TestSub t = new TestSub();
      t.init();
  }

}

class TestSub {
  private HandlerFactory handlerFactory;
  private DataSourceListener listener;
  private PropertyWrapper propSetter;



  public void init() throws IOException {
    Properties props = new Properties();
    InputStream input = new FileInputStream("dirprm/test.props");
    props.load(input);


    String handlerType = "oracle.goldengate.delivery.handler.marklogic.MarkLogicHandler";
    //System.out.println(DsHandler.HandlerFactories.isValid(handlerType));
    GenericHandlerFactory ghf1 = new GenericHandlerFactory();
    ghf1.setListenerClassName(handlerType);
    ghf1.setDescription("Generic Handler Factory for custom handlers");
    this.handlerFactory = ghf1;


    this.listener = handlerFactory.instantiateHandler();
    this.listener.setName("marklogic");
    this.propSetter = new PropertyWrapper(this.listener);
    this.propSetter.setProperties(props);
  }

}



