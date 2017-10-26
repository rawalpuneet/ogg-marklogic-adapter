package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.DsColumn;
import oracle.goldengate.datasource.adapt.Col;
import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.ColumnMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;

import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.models.WriteListItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class OperationHandler {

    protected HandlerProperties handlerProperties = null;

    public OperationHandler(HandlerProperties handlerProperties) {
        this.handlerProperties = handlerProperties;
    }

    public abstract void process(TableMetaData tableMetaData, Op op) throws Exception;

    /**
     * @param tableMetaData
     *            - Table meta data
     * @param op
     *            - The current operation.
     * @param useBeforeValues
     *            - If true before values will be used, else after values will
     *            be used.
     * @return void
     */
    protected void processOperation(WriteListItem item) throws Exception {
        handlerProperties.writeList.add(item);
    }

    protected HashMap<String, Object> getDataMap(TableMetaData tableMetaData, Op op, boolean useBefore) {

        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("headers", headers());

        for (Col col : op) {
            ColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(col.getIndex());

           /*
            if (useBefore) {
                if (col.getBefore() != null) {
                    dataMap.put(columnMetaData.getOriginalColumnName(), col.getBeforeValue());
                }
            } else {
                if (col.getAfter() != null) {
                    dataMap.put(columnMetaData.getOriginalColumnName(), col.getAfterValue());
                }
            }
            */

            // Use after values if present
            if (col.getAfter() != null) {
                dataMap.put(columnMetaData.getOriginalColumnName(), col.getAfterValue());
            } else if (col.getBefore() != null) {
                dataMap.put(columnMetaData.getOriginalColumnName(), col.getBeforeValue());
            }


        }
        return dataMap;
    }

    protected String prepareKey(TableMetaData tableMetaData, Op op, boolean useBefore) {

        StringBuilder stringBuilder = new StringBuilder();
        String delimiter = "";


        for (ColumnMetaData columnMetaData : tableMetaData.getKeyColumns()) {
            DsColumn column = op.getColumn(columnMetaData.getIndex());

            if (useBefore) {
                if (column.getBefore() != null) {

                    stringBuilder.append(delimiter);
                    stringBuilder.append(column.getBeforeValue());
                    delimiter = "_";
                }
            } else {
                if (column.getAfter() != null) {
                    stringBuilder.append(delimiter);
                    stringBuilder.append(column.getAfterValue());
                    delimiter = "_";
                }
            }
        }
        String index = stringBuilder.toString().replaceAll("\\s+","");
        return "/" + tableMetaData.getTableName().getShortName().toLowerCase() + "/"+ index + ".json";
    }

    private HashMap headers() {
        HashMap<String, Object> headers = new HashMap<String, Object>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date = new Date();
        String fdate = df.format(date);
        headers.put("importDate",fdate);
        return headers;
    };



}
