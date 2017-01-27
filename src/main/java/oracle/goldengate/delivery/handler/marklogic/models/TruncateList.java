package oracle.goldengate.delivery.handler.marklogic.models;

import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.query.DeleteQueryDefinition;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

import java.util.ArrayList;

/**
 * Created by prawal on 1/23/17.
 */
public class TruncateList {
  protected ArrayList<String> tables = new ArrayList<String>();

  public void add(String key) {
    //System.out.println("Collection added to delete " + key);
    tables.add(key);
  }

  public ArrayList<String> getKeys() {
    return this.tables;
  }

  public void commit(HandlerProperties handlerProperties) {
    QueryManager queryMgr = handlerProperties.getClient().newQueryManager();
    DeleteQueryDefinition dm = queryMgr.newDeleteDefinition();

    if(this.tables.size() > 0) {
      dm.setCollections(tables.toArray(new String[tables.size()]));
      queryMgr.delete(dm);
    }
  }

  public void clear() {
    tables.clear();
  }



}
