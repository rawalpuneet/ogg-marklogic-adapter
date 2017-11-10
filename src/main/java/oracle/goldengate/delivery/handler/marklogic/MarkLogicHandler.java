package oracle.goldengate.delivery.handler.marklogic;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import oracle.goldengate.datasource.*;
import oracle.goldengate.datasource.GGDataSource.Status;
import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.DsMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.operations.OperationHandler;
import oracle.goldengate.delivery.handler.marklogic.util.DBOperationFactory;
import oracle.goldengate.util.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkLogicHandler extends AbstractHandler {

    final private static Logger logger = LoggerFactory.getLogger(MarkLogicHandler.class);

    private HandlerProperties handlerProperties;

    private DBOperationFactory dbOperationFactory;

    public MarkLogicHandler() {
        super();
        handlerProperties = new HandlerProperties();
        dbOperationFactory = new DBOperationFactory();
    }

    @Override
    public void init(DsConfiguration arg0, DsMetaData arg1) {
        super.init(arg0, arg1);

        dbOperationFactory.init(handlerProperties);

        try {
            initMarkLogicClient();
        } catch (Exception e) {
            logger.error( "Unable to connect to marklogic instance. Configured Server address list " + handlerProperties.getHost(), e);
            throw new ConfigException("Unable to connect to marklogic instance. Configured Server address list ", e);
        }
    }

    @Override
    public Status operationAdded(DsEvent e, DsTransaction tx, DsOperation dsOperation) {

        Status status = super.operationAdded(e, tx, dsOperation);

        Op op = new Op(dsOperation, getMetaData().getTableMetaData(dsOperation.getTableName()), getConfig());
        TableMetaData tableMetaData = getMetaData().getTableMetaData(op.getTableName());

        /**
         * Get the instance of incoming operation type from DBOperationFactory
         * */
        OperationHandler operationHandler = dbOperationFactory.getInstance(dsOperation.getOperationType());
        if (operationHandler != null) {
            try {
                operationHandler.process(tableMetaData, op);

                /** Increment the total number of operations */
                handlerProperties.totalOperations++;
            } catch (Exception e1) {
                status = Status.ABEND;
                logger.error("Unable to process operation.", e1);
            }
        } else {
            status = Status.ABEND;
            logger.error("Unable to instantiate operation handler for " + dsOperation.getOperationType().toString());
        }

        return status;
    }

    @Override
    public Status transactionBegin(DsEvent e, DsTransaction tx) {
        return super.transactionBegin(e, tx);
    }

    @Override
    public Status transactionCommit(DsEvent e, DsTransaction tx) {
        Status status = super.transactionCommit(e, tx);

        try{
            handlerProperties.writeList.commit(handlerProperties);
            handlerProperties.deleteList.commit(handlerProperties);
            handlerProperties.truncateList.commit(handlerProperties);

            handlerProperties.writeList.clear();
            handlerProperties.deleteList.clear();
            handlerProperties.truncateList.clear();


        }catch(Exception ex){
            logger.error("Error flushing records ", ex);
            status = Status.ABEND;
        }


        /**TODO: Add steps for rollback */

        handlerProperties.totalTxns++;

        return status;
    }

    @Override
    public String reportStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(":- Status report: mode=").append(getMode());
        sb.append(", transactions=").append(handlerProperties.totalTxns);
        sb.append(", operations=").append(handlerProperties.totalOperations);
        sb.append(", inserts=").append(handlerProperties.totalInserts);
        sb.append(", updates=").append(handlerProperties.totalUpdates);
        sb.append(", deletes=").append(handlerProperties.totalDeletes);
        sb.append(", truncates=").append(handlerProperties.totalTruncates);
        return sb.toString();
    }

    @Override
    public void destroy() {
        handlerProperties.getClient().release();
        super.destroy();
    }

    private void initMarkLogicClient() throws Exception {

        DatabaseClient client = DatabaseClientFactory.newClient(
            handlerProperties.getHost(), Integer.parseInt(handlerProperties.getPort()),
            handlerProperties.getDatabase(),
            handlerProperties.getUser(), handlerProperties.getPassword(),
           DatabaseClientFactory.Authentication.valueOf(handlerProperties.getAuth().toUpperCase()));



        this.handlerProperties.setClient(client);
    }

    public void setUser(String user) {
        handlerProperties.setUser(user);
    }
    public void setPassword(String pass) {
        handlerProperties.setPassword(pass);
    }
    public void setPort(String port) {
        handlerProperties.setPort(port);
    }
    public void setDatabase(String database) {
        handlerProperties.setDatabase(database);
    }
    public void setFormat(String format) {
        handlerProperties.setFormat(format.toLowerCase());
    }
    public void setHost(String host) {
        handlerProperties.setHost(host);
    }

    public void setAuth(String auth) {
        handlerProperties.setAuth(auth);
    }

    public void setTransformName(String transformName) {
        handlerProperties.setTransformName(transformName);
    }

    public void setTransformParams(String transformParams) {
        handlerProperties.setTransformParams(transformParams);
    }

    public HandlerProperties getProperties() {
      return this.handlerProperties;
    }
}
