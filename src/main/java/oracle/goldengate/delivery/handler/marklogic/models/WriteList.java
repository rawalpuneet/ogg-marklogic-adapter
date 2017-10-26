package oracle.goldengate.delivery.handler.marklogic.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

import java.util.*;
import java.util.Hashtable;
import java.util.function.BooleanSupplier;

/**
 * Created by prawal on 1/23/17.
 */
public class WriteList {
  private List<WriteListItem> items = new ArrayList<WriteListItem>();

  public void add(WriteListItem item) {
    this.items.add(item);
  }

  public void commit(HandlerProperties handlerProperties) {

    if(this.items.size() > 0) {
      JSONDocumentManager docMgr = handlerProperties.getClient().newJSONDocumentManager();
      DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
      DocumentMetadataHandle.DocumentCollections coll = metadataHandle.getCollections();

      for (WriteListItem item : items) {

        boolean docExists = false;

        if(item.getOperation() == item.UPDATE && docMgr.exists(item.getUri()) != null) {
          docExists = true;
        }

        HashMap<String, Object> node = new HashMap<String,Object>();

        if(docExists == true) {
            node = updateNode(item, docMgr);
        } else if(item.getOperation() == item.UPDATE) {
          // skipping if update and doc doesn't exist
        } else {
          node = item.getMap();
        }

        if(docExists == false && item.getOperation() == item.UPDATE) {
            // skipping if update and doc doesn't exist
        } else {
          coll.addAll(item.getCollection());
          ObjectMapper mapper = new ObjectMapper();
          JsonNode jsonNode = mapper.convertValue(node, JsonNode.class);
          JacksonHandle handle = new JacksonHandle(jsonNode);
          docMgr.write(item.getUri(), metadataHandle, handle);
          coll.clear();
        }

      }
    }

  };

  private HashMap<String, Object> updateNode(WriteListItem item, JSONDocumentManager docMgr) {

    JacksonHandle handle = new JacksonHandle();
    docMgr.read(item.getUri(), handle);

    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> original = mapper.convertValue(handle.get(), HashMap.class);
    HashMap<String, Object> update = item.getMap();

    String key = null;
    Set<String> keys = original.keySet();
    Set<String> updateKeys = update.keySet();
    Iterator<String> itr = updateKeys.iterator();

    while (itr.hasNext()) {

      key = itr.next();
      if(keys.contains(key)) {
        original.put(key, update.get(key));
      }
    }

    return original;
  };

  public void clear() {
    items.clear();
  }

}
