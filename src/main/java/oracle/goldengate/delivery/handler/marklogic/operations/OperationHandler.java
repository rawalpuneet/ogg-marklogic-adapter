package oracle.goldengate.delivery.handler.marklogic.operations;

import oracle.goldengate.datasource.DsColumn;
import oracle.goldengate.datasource.adapt.Col;
import oracle.goldengate.datasource.adapt.Op;
import oracle.goldengate.datasource.meta.ColumnMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;

import oracle.goldengate.delivery.handler.marklogic.HandlerProperties;
import oracle.goldengate.delivery.handler.marklogic.models.WriteListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.*;
import java.util.UUID;


import static java.security.MessageDigest.*;


public abstract class OperationHandler {

    protected HandlerProperties handlerProperties = null;

    public OperationHandler(HandlerProperties handlerProperties) {
        this.handlerProperties = handlerProperties;
    }

    public abstract void process(TableMetaData tableMetaData, Op op) throws Exception;

    final private static Logger logger = LoggerFactory.getLogger(OperationHandler.class);
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

    protected String prepareKey(TableMetaData tableMetaData, Op op, boolean useBefore) throws NoSuchAlgorithmException {

        StringBuilder stringBuilder = new StringBuilder();

        String delimiter = "";


        for (ColumnMetaData columnMetaData : tableMetaData.getKeyColumns()  ) {

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

        return "/" + tableMetaData.getTableName().getShortName().toLowerCase() + "/"+ prepareKeyIndex(stringBuilder) + ".json";
    }

    // Joining key column values and hashing
    private String prepareKeyIndex(StringBuilder sb) throws NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String index;
        if(sb.length() > 0) {
            index = sb.toString();
            md5.update(StandardCharsets.UTF_8.encode(index));
            index =  String.format("%032x", new BigInteger(1, md5.digest()));
        } else {
            index = UUID.randomUUID().toString();
        }

        return index;
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
