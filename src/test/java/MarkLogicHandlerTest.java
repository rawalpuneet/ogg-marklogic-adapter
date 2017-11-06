
import oracle.goldengate.delivery.handler.marklogic.*;
import oracle.goldengate.datasource.*;

import oracle.goldengate.datasource.meta.ColumnMetaData;
import oracle.goldengate.datasource.meta.DsMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.datasource.meta.TableName;

import oracle.goldengate.datasource.DsEvent;
import oracle.goldengate.datasource.DsTransaction;

import oracle.goldengate.util.DsMetric;
import org.junit.Ignore;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;




import java.util.ArrayList;



/**
 * Created by prawal on 1/18/17.
 */
public class MarkLogicHandlerTest {
  private MarkLogicHandler marklogicHandler;
  private DsEvent e;
  private DsTransaction dsTransaction;
  private TableName tableName;
  private DsMetaData dsMetaData;

  @BeforeClass
  public void init() {
    marklogicHandler = new MarkLogicHandler();

    marklogicHandler.setState(DataSourceListener.State.READY);


    ArrayList<ColumnMetaData> columnMetaData = new ArrayList<>();

    columnMetaData.add(new ColumnMetaData("c1", 0));
    columnMetaData.add(new ColumnMetaData("c2", 1,true));
    columnMetaData.add(new ColumnMetaData("c3", 2));
    columnMetaData.add(new ColumnMetaData("c4", 3));
    columnMetaData.add(new ColumnMetaData("c5", 4));

    tableName = new TableName("ogg_test.new_table");

    TableMetaData tableMetaData = new TableMetaData(tableName, columnMetaData);

    dsMetaData = new DsMetaData();
    dsMetaData.setTableMetaData(tableMetaData);

    long i = 233;
    long j = 32323;

    GGTranID ggTranID = GGTranID.getID(i, j);

    dsTransaction = new DsTransaction(ggTranID);
    e = new DsEventManager.TxEvent(dsTransaction, dsMetaData, "Sample Transaction");

  }

  @Test
  public void testInsertJson(){

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();

    marklogicHandler.setDatabase("ml-ipas-STAGING");
    marklogicHandler.setPort("8023");
    marklogicHandler.setPassword("admin-jkerr");
    marklogicHandler.setFormat("json");

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();

    // need an assert that checks the document in the DB
  }

  @Test
  public void testInsertXml(){

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();

    marklogicHandler.setDatabase("ml-ipas-STAGING");
    marklogicHandler.setPort("8023");
    marklogicHandler.setPassword("admin-jkerr");
    marklogicHandler.setFormat("xml");

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();

    // we need an assert that tests that document inserted is XML
  }

  @Test
  public void testTransform(){

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();
    marklogicHandler.setDatabase("ml-ipas-STAGING");
    marklogicHandler.setPort("8023");
    marklogicHandler.setPassword("admin-jkerr");
    marklogicHandler.setFormat("xml");

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    // need to install a transform to test with
    props.setTransformName("run-flow");
    props.setTransformParams("entity=Policy,flow=policy,flowType=input");

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnAfterValue("testing");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_INSERT, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();

    // need an assert that checks the document in the DB
  }

  @Test
  public void testUpdateJson(){

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();

    marklogicHandler.setDatabase("ml-ipas-STAGING");
    marklogicHandler.setPort("8023");
    marklogicHandler.setPassword("admin-jkerr");
    marklogicHandler.setFormat("json");

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName(null);

    // need to install a transform to test with
    props.setTransformName(null);
    props.setTransformParams(null);

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnComposite(new DsColumnAfterValue("puneet"), new DsColumnBeforeValue("testing"));
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("3"));
    columns[3] = new DsColumnComposite(new DsColumnAfterValue("new date"), new DsColumnBeforeValue("some date"));
    columns[4] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("6"));

    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_UPDATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();

    // need an assert that checks the document in the DB
  }

  @Test
  public void testUpdateXml(){

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();

    marklogicHandler.setDatabase("ml-ipas-STAGING");
    marklogicHandler.setPort("8023");
    marklogicHandler.setPassword("admin-jkerr");
    marklogicHandler.setFormat("xml");

    HandlerProperties props = marklogicHandler.getProperties();
    props.setRootName("root");

    // need to install a transform to test with
    props.setTransformName(null);
    props.setTransformParams(null);

    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);

    DsColumn[] columns = new DsColumn[5];
    columns[0] = new DsColumnComposite(new DsColumnAfterValue("puneet"), new DsColumnBeforeValue("testing"));
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("3"));
    columns[3] = new DsColumnComposite(new DsColumnAfterValue("new date"), new DsColumnBeforeValue("some date"));
    columns[4] = new DsColumnComposite(new DsColumnAfterValue("600"), new DsColumnBeforeValue("6"));


    DsRecord dsRecord = new DsRecord(columns);

    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_UPDATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();

    // need an assert that checks the document in the DB
  }


  @Ignore
  public void testTruncate() {

    DataSourceConfig ds = new DataSourceConfig();
    DsMetric dms = new DsMetric();
    marklogicHandler.setHandlerMetric(dms);
    marklogicHandler.init(ds, dsMetaData);



    DsColumn[] columns = new DsColumn[5];
    /*
    columns[0] = new DsColumnAfterValue("testNormal");
    columns[1] = new DsColumnAfterValue("2");
    columns[2] = new DsColumnAfterValue("3");
    columns[3] = new DsColumnAfterValue("2016-05-20 09:00:00");
    columns[4] = new DsColumnAfterValue("6");
    */

    DsRecord dsRecord = new DsRecord(columns);
    DsOperation dsOperation = new DsOperation(tableName, DsOperation.OpType.DO_TRUNCATE, "2016-05-13 19:15:15.010",0l, 0l, dsRecord);
    GGDataSource.Status status = marklogicHandler.operationAdded(e, dsTransaction, dsOperation);
    marklogicHandler.transactionCommit(e, dsTransaction);
    Assert.assertEquals(GGDataSource.Status.OK, status);
    marklogicHandler.destroy();
  }

  @Ignore
  public void testAuth() {
    String status = "digest";
    HandlerProperties handle = new HandlerProperties();
    String auth = "digest";

    handle.setAuth(auth);

    Assert.assertEquals("digest", handle.getAuth());
  }

}
