package oracle.goldengate.delivery.handler.marklogic.models;

import oracle.goldengate.datasource.meta.TableName;

import java.util.*;

/**
 * Created by prawal on 1/23/17.
 */
public class WriteListItem {

    private String uri;
    private String oldUri;

    private HashMap<String, Object> map = new HashMap<String, Object>();
    // allowed values UPDATE OR INSERT
    private String operation = null;
    private Collection<String> collection =  new ArrayList<String>();
    public static final String UPDATE = "update";
    public static final String INSERT = "insert";

    public WriteListItem(String uri, HashMap<String, Object> map, String operation) {
        this.uri = uri;
        this.map = map;
        this.operation = operation;
    }

    public WriteListItem(String uri, HashMap<String, Object> map, String operation, String collection) {
        this.uri = uri;
        this.map = map;
        this.operation = operation;
        this.collection.add(collection);
    }

    public WriteListItem(String uri, HashMap<String, Object> map, String operation, TableName table) {
        this.uri = uri;
        this.map = map;
        this.operation = operation;

        this.collection.add(table.getSchemaName().toLowerCase()  + "/" + table.getShortName().toLowerCase());
        this.collection.add(table.getSchemaName().toLowerCase());
    }

    public String getUri() {
        return this.uri;
    }

    public String getOldUri() {
        return this.oldUri;
    }

    public void setOldUri(String oldUri) {
        this.oldUri = oldUri;
    }

    public HashMap<String,Object> getMap() {
        return this.map;
    }

    public String getOperation() {
       return this.operation;
    }

    public Collection<String> getCollection() {
        return this.collection;
    }
}
