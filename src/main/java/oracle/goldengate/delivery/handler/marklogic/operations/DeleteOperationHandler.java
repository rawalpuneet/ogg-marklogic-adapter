package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

public class DeleteOperationHandler extends OperationHandler {


    public DeleteOperationHandler(HandlerProperties handlerProperties) {
        super(handlerProperties);
    }

    @Override
    public void process(TableMetaData tableMetaData, Op op) throws Exception {

        handlerProperties.deleteList.add(prepareKey(tableMetaData,op, true));
        handlerProperties.totalDeletes++;
    }

}
