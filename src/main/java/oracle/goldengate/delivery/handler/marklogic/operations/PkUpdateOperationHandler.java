package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.models.WriteListItem;

public class PkUpdateOperationHandler extends OperationHandler {

    public PkUpdateOperationHandler(HandlerProperties handlerProperties) {
        super(handlerProperties);
    }

    @Override
    public void process(TableMetaData tableMetaData, Op op) throws Exception {


        //Get previous key
        String olduri = prepareKey(tableMetaData,op,true);
        handlerProperties.deleteList.add(olduri);

        WriteListItem item = new WriteListItem(
            prepareKey(tableMetaData,op, false),
            getDataMap(tableMetaData, op, false),
            WriteListItem.INSERT,
            tableMetaData.getTableName().getShortName().toLowerCase());

        processOperation(item);

        handlerProperties.totalUpdates++;

    }

}
