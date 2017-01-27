package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;

public class TruncateOperationHandler extends OperationHandler {

    public TruncateOperationHandler(HandlerProperties handlerProperties) {
        super(handlerProperties);
    }

    @Override
    public void process(TableMetaData tableMetaData, Op op) throws Exception {
        handlerProperties.truncateList.add(tableMetaData.getTableName().getShortName().toLowerCase());
        handlerProperties.totalTruncates++;
    }

}
