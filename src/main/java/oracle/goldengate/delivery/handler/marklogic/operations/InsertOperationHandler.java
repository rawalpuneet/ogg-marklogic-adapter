package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.models.WriteListItem;
import oracle.goldengate.datasource.conf.DsHandler;


public class InsertOperationHandler extends OperationHandler {

    public InsertOperationHandler(HandlerProperties handlerProperties) {
        super(handlerProperties);
    }

    @Override
    public void process(TableMetaData tableMetaData, Op op) throws Exception {
        WriteListItem item = new WriteListItem(
                            prepareKey(tableMetaData,op, false),
                            getDataMap(tableMetaData, op, false),
                            WriteListItem.INSERT,
                            tableMetaData.getTableName().getShortName().toLowerCase());

        processOperation(item);
        handlerProperties.totalInserts++;
    }

}
