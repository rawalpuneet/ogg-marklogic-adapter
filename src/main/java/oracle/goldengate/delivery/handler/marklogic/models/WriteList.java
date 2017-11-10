package oracle.goldengate.delivery.handler.marklogic.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.marklogic.client.document.ServerTransform;

import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.io.Format;


import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

import java.util.*;
import java.util.Hashtable;
import java.util.function.BooleanSupplier;

/**
 * Created by prawal on 1/23/17.
 */
public class WriteList {

  private static String UPDATE_OPTION = "{operation:\"update\"}";
  private static String INSERT_OPTION = "{operation:\"insert\"}";

  private List<WriteListItem> items = new ArrayList<WriteListItem>();

  public void add(WriteListItem item) {
    this.items.add(item);
  }

  private DocumentManager getDocumentManager(HandlerProperties handlerProperties) {
    // defaults to json
    if ("xml".equals(handlerProperties.getFormat())) {
      return handlerProperties.getClient().newXMLDocumentManager();
    } else {
      return handlerProperties.getClient().newJSONDocumentManager();
    }
  }

  private ObjectMapper getObjectMapper(HandlerProperties handlerProperties) {
    // defaults to json
    if ("xml".equals(handlerProperties.getFormat())) {
      return new XmlMapper();
    } else {
      return new ObjectMapper();
    }
  }

  public void commit(HandlerProperties handlerProperties) throws JsonProcessingException {

    if(this.items.size() > 0) {
      DocumentManager docMgr = getDocumentManager(handlerProperties);
      DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
      DocumentMetadataHandle.DocumentCollections coll = metadataHandle.getCollections();

      for (WriteListItem item : items) {

          // assume updates vs inserts are handled in the transform
          if (handlerProperties.getTransformName() != null) {
            ServerTransform transform = getTransform(handlerProperties);
            if (item.getOperation() == WriteListItem.UPDATE) {
              transform.addParameter("options", UPDATE_OPTION);
            } else {
              transform.addParameter("options", INSERT_OPTION);
            }

            docMgr.setWriteTransform(transform);

            ObjectMapper mapper = getObjectMapper(handlerProperties);

            ObjectWriter writer = mapper.writer();
            if (handlerProperties.getRootName() != null) {
              writer = writer.withRootName(handlerProperties.getRootName());
            }

            HashMap<String, Object> node = item.getMap();
            StringHandle handle = new StringHandle(writer.writeValueAsString(node));
            docMgr.write(item.getUri(), metadataHandle, handle);
          } else {
            boolean docExists = false;

            if(item.getOperation() == WriteListItem.UPDATE && docMgr.exists(item.getUri()) != null) {
              docExists = true;
            }

            HashMap<String, Object> node = new HashMap<String,Object>();

            if(docExists == true) {
                node = updateNode(item, docMgr, handlerProperties);
            } else if(item.getOperation() == WriteListItem.UPDATE) {
              // skipping if update and doc doesn't exist
            } else {
              node = item.getMap();
            }

            if(docExists == false && item.getOperation() == WriteListItem.UPDATE) {
              // skipping if update and doc doesn't exist
            } else {
              coll.addAll(item.getCollection());

              ObjectMapper mapper = getObjectMapper(handlerProperties);

              ObjectWriter writer = mapper.writer();
              if (handlerProperties.getRootName() != null) {
                writer = writer.withRootName(handlerProperties.getRootName());
              }

              StringHandle handle = new StringHandle(writer.writeValueAsString(node));

              docMgr.write(item.getUri(), metadataHandle, handle);

              coll.clear();
            }
          }
      }
    }
  };

  private ServerTransform getTransform(HandlerProperties handlerProperties) {
    ServerTransform transform = new ServerTransform(handlerProperties.getTransformName());

    HashMap<String, String> params = handlerProperties.getTransformParams();
    for (String param : params.keySet()) {
      transform.addParameter(param, params.get(param));
    }

    return transform;
  }

  private HashMap<String, Object> updateNode(WriteListItem item, DocumentManager docMgr, HandlerProperties handlerProperties) {

    // need to figure out what to do with XML

    // also need to figure out what to do with data that is in an envelope: if a transform was called,
    // how can we update that data?

    // if the update is supposed to have all fields, why do we need to merge? can't we just replace?

    JacksonHandle handle = new JacksonHandle();
    docMgr.read(item.getUri(), handle);

    ObjectMapper mapper = getObjectMapper(handlerProperties);
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
