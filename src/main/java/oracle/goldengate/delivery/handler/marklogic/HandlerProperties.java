package oracle.goldengate.delivery.handler.marklogic;

import com.marklogic.client.io.*;
import com.marklogic.client.DatabaseClient;
import oracle.goldengate.delivery.handler.marklogic.models.DeleteList;
import oracle.goldengate.delivery.handler.marklogic.models.TruncateList;
import oracle.goldengate.delivery.handler.marklogic.models.WriteList;
import com.marklogic.client.DatabaseClientFactory.Authentication;

public class HandlerProperties {

    private DatabaseClient client;
    private String database = "Documents";
    private String host = "localhost";
    private String port = "8000";
    private String user = "admin";
    private String password = "admin";
    private String format = "json";
    private String auth = "digest";


    public Long totalInserts = 0L;
    public Long totalUpdates = 0L;
    public Long totalDeletes =  0L;
    public Long totalTruncates =  0L;
    public Long totalTxns = 0L;
    public Long totalOperations = 0L;

    public WriteList writeList = new WriteList();
    public DeleteList deleteList = new DeleteList();
    public TruncateList truncateList = new TruncateList();

    public DatabaseClient getClient() {
        return client;
    }
    public void setClient(DatabaseClient client) {
        this.client = client;
    }


    public String getDatabase() {
        return database;
    }
    public void setDatabase(String database) {
        this.database = database;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getAuth() {
        return auth;
    }
}
