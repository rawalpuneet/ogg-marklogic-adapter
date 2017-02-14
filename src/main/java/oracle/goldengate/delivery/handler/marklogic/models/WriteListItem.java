package oracle.goldengate.delivery.handler.marklogic.models;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by prawal on 1/23/17.
 */
public class WriteListItem {
    private String uri;
    private Hashtable<String, Object> map = new Hashtable<String, Object>();
    // allowed values UPDATE OR INSERT
    private String operation = null;
    private String collection;
    public static final String UPDATE = "update";
    public static final String INSERT = "insert";

    public WriteListItem(String uri, Hashtable<String, Object> map, String operation) {
        this.uri = uri;
        this.map = map;
        this.operation = operation;
    }

    public WriteListItem(String uri, Hashtable<String, Object> map, String operation, String collection) {
        this.uri = uri;
        this.map = map;
        this.operation = operation;
        this.collection = collection;
    }

    public String getUri() {
        return this.uri;
    }

    public Hashtable<String,Object> getMap() {
        return this.map;
    }

    public String getOperation() {
       return this.operation;
    }

    public String getCollection() {
        return this.collection;
    }


}
