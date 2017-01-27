package oracle.goldengate.delivery.handler.marklogic.util;

import oracle.goldengate.datasource.DsOperation.OpType;
import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.operations.*;

public class DBOperationFactory {

    public OperationHandler insertOperationHandler;
    public OperationHandler updateOperationHandler;
    public OperationHandler deleteOperationHandler;
    public OperationHandler pkUpdateOperationHandler;
    public OperationHandler truncateOperationHandler;

    public void init(HandlerProperties handlerProperties) {
        insertOperationHandler = new InsertOperationHandler(handlerProperties);
        updateOperationHandler = new UpdateOperationHandler(handlerProperties);
        deleteOperationHandler = new DeleteOperationHandler(handlerProperties);
        pkUpdateOperationHandler = new PkUpdateOperationHandler(handlerProperties);
        truncateOperationHandler = new  TruncateOperationHandler(handlerProperties);
    }

    public OperationHandler getInstance(OpType opType) {
        if (opType.isInsert()) {
            return insertOperationHandler;
        }

        if (opType.isPkUpdate()) {
            return pkUpdateOperationHandler;
        }
        
        if (opType.isUpdate()) {
            return updateOperationHandler;
        }

        if (opType.isDelete()) {
            return deleteOperationHandler;
        }
        
        if(opType.isTruncate()) {
            return truncateOperationHandler;
        }

        return null;
    }

}
