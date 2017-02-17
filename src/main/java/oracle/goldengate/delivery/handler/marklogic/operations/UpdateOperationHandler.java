package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.models.WriteListItem;

public class UpdateOperationHandler extends OperationHandler {


    public UpdateOperationHandler(HandlerProperties handlerProperties) {
        super(handlerProperties);
    }

    @Override
    public void process(TableMetaData tableMetaData, Op op) throws Exception {

        WriteListItem item = new WriteListItem(
            prepareKey(tableMetaData,op, false),
            getDataMap(tableMetaData, op, false),
            WriteListItem.UPDATE,
            tableMetaData.getTableName());
        processOperation(item);

        handlerProperties.totalUpdates++;
    }
}
