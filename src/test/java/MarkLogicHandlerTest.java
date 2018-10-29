import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

import oracle.goldengate.delivery.handler.marklogic.*;
import oracle.goldengate.datasource.*;

import oracle.goldengate.datasource.meta.ColumnMetaData;
import oracle.goldengate.datasource.meta.DsMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.datasource.meta.TableName;

import oracle.goldengate.datasource.DsEvent;
import oracle.goldengate.datasource.DsTransaction;

import oracle.goldengate.util.DsMetric;

import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.datamovement.DeleteListener;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.client.io.InputStreamHandle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;



/**
 * Created by prawal on 1/18/17.
 */
public class MarkLogicHandlerTest {
  private MarkLogicHandler marklogicHandler;
  private DsEvent e;
  private DsTransaction dsTransaction;
  private TableName tableName;
  private DsMetaData dsMetaData;
  private TableMetaData tableMetaData;

  private DocumentManager getDocumentManager(HandlerProperties props) {
    // defaults to json
    if ("xml".equals(props.getFormat())) {
      return props.getClient().newXMLDocumentManager();
    } else {
      return props.getClient().newJSONDocumentManager();
    }
  }

  private ObjectMapper getObjectMapper(String format) {
    // defaults to json
    if ("xml".equals(format)) {
      return new XmlMapper();
    } else {
      return new ObjectMapper();
    }
  }

  private HashMap<String, Object> readDocument(String uri, HandlerProperties props)
    throws IOException {
    // need an assert that checks the document in the DB
    DocumentManager mgr = getDocumentManager(props);

    InputStreamHandle handle = new InputStreamHandle();
    mgr.read(uri, handle);

    ObjectMapper mapper = getObjectMapper(props.getFormat());
    return mapper.readValue(handle.get(), HashMap.class);
  }

  private void deleteTestCollection(HandlerProperties props) {
    DataMovementManager dmm = props.getClient().newDataMovementManager();
    QueryManager qm = props.getClient().newQueryManager();
    StructuredQueryBuilder sqb = qm.newStructuredQueryBuilder();
    StructuredQueryDefinition query = sqb.collection("ogg_test");

    QueryBatcher batcher = dmm.newQueryBatcher(query);
    batcher.withConsistentSnapshot()
           .onUrisReady(new DeleteListener());
    dmm.startJob(batcher);

    batcher.awaitCompletion();
    dmm.stopJob(batcher);
}

  @BeforeMethod
  public void init() throws Exception {
    marklogicHandler = new MarkLogicHandler();

    Properties props = new Properties();
    props.load(this.getClass().getResourceAsStream("/test.props"));

    marklogicHandler.setHost(props.getProperty("gg.handler.marklogic.host"));
    marklogicHandler.setDatabase(props.getProperty("gg.handler.marklogic.database"));
    marklogicHandler.setPort(props.getProperty("gg.handler.marklogic.port"));
    marklogicHandler.setUser(props.getProperty("gg.handler.marklogic.user"));
    marklogicHandler.setPassword(props.getProperty("gg.handler.marklogic.password"));
    marklogicHandler.setCollections(props.getProperty("gg.handler.marklogic.collections"));

    marklogicHandler.setState(DataSourceListener.State.READY);


    ArrayList<ColumnMetaData> columnMetaData = new ArrayList<>();

    columnMetaData.add(new ColumnMetaData("c1", 0));
    columnMetaData.add(new ColumnMetaData("c2", 1,true));
    columnMetaData.add(new ColumnMetaData("c3", 2));
    columnMetaData.add(new ColumnMetaData("c4", 3));
    columnMetaData.add(new ColumnMetaData("c5", 4));

    tableName = new TableName("ogg_test.new_table");

    tableMetaData = new TableMetaData(tableName, columnMetaData);

    dsMetaData = new DsMetaData();
    dsMetaData.setTableMetaData(tableMetaData);

    long i = 233;
    long j = 32323;

    GGTranID ggTranID = GGTranID.getID(i, j);

    dsTransaction = new DsTransaction(ggTranID);
    e = new DsEventManager.TxEvent(dsTransaction, ggTranID, dsMetaData, "Sample Transaction");

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    // clear out the ogg-test collection
    deleteTestCollection(marklogicHandler.getProperties());
  }

  @AfterMethod
  public void clear() throws Exception {
    deleteTestCollection(marklogicHandler.getProperties());
    marklogicHandler.destroy();
  }

  @Test
  public void testInsertJson() throws Exception {
    HandlerProperties props = marklogicHandler.getProperties();

    marklogicHandler.setFormat("json");

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);

    String uri = "/new_table/c81e728d9d4c2f636f067f89cc14862c.json";
    HashMap<String, Object> updated = readDocument(uri, props);

    assertEquals("testing", updated.get("c1"));
    assertEquals("2", updated.get("c2"));
    assertEquals("3", updated.get("c3"));
    assertEquals("2016-05-20 09:00:00", updated.get("c4"));
    assertEquals("6", updated.get("c5"));
  }

  @Test
  public void testInsertXml() throws Exception {
    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    marklogicHandler.setFormat("xml");

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);

    String uri = "/new_table/c81e728d9d4c2f636f067f89cc14862c.xml";
    HashMap<String, Object> updated = readDocument(uri, props);

    assertEquals("testing", updated.get("c1"));
    assertEquals("2", updated.get("c2"));
    assertEquals("3", updated.get("c3"));
    assertEquals("2016-05-20 09:00:00", updated.get("c4"));
    assertEquals("6", updated.get("c5"));
  }

  @Ignore
  public void testTransform() throws Exception {
    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    marklogicHandler.setFormat("xml");

    // need to install a transform to test with
    props.setTransformName("run-flow");
    props.setTransformParams("entity=Policy,flow=policy,flowType=input");

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing transform");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);

    String uri = "/new_table/c81e728d9d4c2f636f067f89cc14862c.xml";
    HashMap<String, Object> updated = readDocument(uri, props);

    assertEquals("testing transform", updated.get("c1"));
    assertEquals("2", updated.get("c2"));
    assertEquals("3", updated.get("c3"));
    assertEquals("2016-05-20 09:00:00", updated.get("c4"));
    assertEquals("6", updated.get("c5"));
  }

  @Test
  public void testUpdateJson() throws Exception {
    testInsertJson();

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName(null);

    marklogicHandler.setFormat("json");

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnComposite(new DsColumnAfterValue("puneet"), new DsColumnBeforeValue("testing"));
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("3"));
    columns[3] = new DsColumnComposite(new DsColumnAfterValue("new date"), new DsColumnBeforeValue("some date"));
    columns[4] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("6"));

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_UPDATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);

    String uri = "/new_table/c81e728d9d4c2f636f067f89cc14862c.json";
    HashMap<String, Object> updated = readDocument(uri, props);

    assertEquals("puneet", updated.get("c1"));
    assertEquals("2", updated.get("c2"));
    assertEquals("600", updated.get("c3"));
    assertEquals("new date", updated.get("c4"));
    assertEquals("600", updated.get("c5"));
  }

  @Test
  public void testUpdateXml() throws Exception {
    testInsertXml();

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    marklogicHandler.setFormat("xml");

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnComposite(new DsColumnAfterValue("puneet"), new DsColumnBeforeValue("testing"));
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("3"));
    columns[3] = new DsColumnComposite(new DsColumnAfterValue("new date 2"), new DsColumnBeforeValue("some date"));
    columns[4] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("6"));


    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_UPDATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);

    // assert that checks the document in the DB
    String uri = "/new_table/c81e728d9d4c2f636f067f89cc14862c.xml";
    HashMap<String, Object> updated = readDocument(uri, props);

    assertEquals("puneet", updated.get("c1"));
    assertEquals("2", updated.get("c2"));
    assertEquals("600", updated.get("c3"));
    assertEquals("new date 2", updated.get("c4"));
    assertEquals("600", updated.get("c5"));
  }


  @Ignore
  public void testTruncate() {
    DsColumn[] columns = new DsColumn[5];
    /*
    columns[0] = new DsColumnAfterValue("testNormal");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");
    */

    DsRecord dsRecord = new DsRecord(columns);
    DsOperation dsOperation = new DsOperation(tableName, tableMetaData, DsOperation.OpType.DO_TRUNCATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();
  }

  @Ignore
  public void testAuth() {
    String status = "digest";
    HandlerProperties handle = new HandlerProperties();
    String auth = "digest";

    handle.setAuth(auth);

    assertEquals("digest", handle.getAuth());
  }

}
