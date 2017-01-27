package oracle.goldengate.delivery.handler.marklogic.models;

import com.marklogic.client.document.GenericDocumentManager;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

import java.util.ArrayList;

/**
 * Created by prawal on 1/23/17.
 */
public class DeleteList {
  protected ArrayList<String> keys = new ArrayList<String>();

  public void add(String key) {
    keys.add(key);
  }

  public ArrayList<String> getKeys() {
    return this.keys;
  }

  public void commit(HandlerProperties handlerProperties) {
    GenericDocumentManager docMgr = handlerProperties.getClient().newDocumentManager();
    for (String key : keys) {
      docMgr.delete(key);
    }
  }

  public void clear() {
    keys.clear();
  }

}
