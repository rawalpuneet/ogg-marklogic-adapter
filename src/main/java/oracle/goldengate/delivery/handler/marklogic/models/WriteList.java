package oracle.goldengate.delivery.handler.marklogic.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

import java.util.ArrayList;
import java.util.List;

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
      coll.add(this.items.get(0).getCollection());
      for (WriteListItem item : items) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(item.getMap(), JsonNode.class);
        JacksonHandle handle = new JacksonHandle(jsonNode);
        docMgr.write(item.getUri(), metadataHandle, handle);
      }
    }

  };

  public void clear() {
    items.clear();
  }

}
