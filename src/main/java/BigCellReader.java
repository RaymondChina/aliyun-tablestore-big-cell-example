import com.aliyun.openservices.ots.OTS;
import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BigCellReader {
    OTSClient otsClient = null;

    BigCellReader(OTSClient otsClient) {
        this.otsClient = otsClient;
    }

    Map<String, String> readCell(String tableName, RowPrimaryKey primaryKey, List<String> columnNames) {
        MultiRowQueryCriteria multiRowQueryCriteria = new MultiRowQueryCriteria(tableName);
        multiRowQueryCriteria.addRow(primaryKey);
        multiRowQueryCriteria.addColumnsToGet(BigCellWriter.getStateColumnName());
        for (String columnName : columnNames) {
            multiRowQueryCriteria.addColumnsToGet(columnName);
        }
        Map<String, String> returnKV = new TreeMap<String, String>();
        try {
            BatchGetRowRequest batchGetRowRequest = new BatchGetRowRequest();
            batchGetRowRequest.addMultiRowQueryCriteria(multiRowQueryCriteria);
            BatchGetRowResult result = otsClient.batchGetRow(batchGetRowRequest);
            if (result.getFailedRows().size() > 0) {
                throw new Exception("please log it");
            }
            List<BatchGetRowResult.RowStatus> succeedRows = result.getSucceedRows();
            Map<String, ColumnValue> columnValueMap = succeedRows.get(0).getRow().getColumns();
            for (Map.Entry<String,ColumnValue> entry : columnValueMap.entrySet()) {
                returnKV.put(entry.getKey(), entry.getValue().asString());
            }
            return returnKV;
        } catch (Exception ex) {
            System.out.println("please log it");
        }
        return returnKV;
    }

    /**
     * @return 1. null : no big cell exist
     * 2. empty string : big cell exist, but not all cell-slice complete writing. caller should wait 20-100ms to try again
     * 3. non-empty string : big cell exist, return big cell value
     */
    String tryReadBigCell(String tableName, RowPrimaryKey primaryKey) {
        List<String> columnNames = new LinkedList<String>();
        columnNames.add(BigCellWriter.getStateColumnName());
        Map<String, String> kv = readCell(tableName, primaryKey, columnNames);
        String rowStateStr = kv.get(BigCellWriter.getStateColumnName());
        if (rowStateStr == null) {
            // not big cell exist
            return null;
        }
        BigCellRowState bigCellRowState = new BigCellRowState();
        bigCellRowState.Deserialize(rowStateStr);
        if (bigCellRowState.getUnreadyCells().size() != 0) {
            // big cell exist, but the row is partially updated. caller should wait a while.
            return new String("");
        }
        String rowStateID = bigCellRowState.getStateID();
        List<String> splitCells = new LinkedList<String>();
        splitCells.addAll(bigCellRowState.getReadyCells());
        StringBuffer buffer = new StringBuffer();
        for (String columnName :
                splitCells) {
            columnNames.clear();
            columnNames.add(columnName);
            columnNames.add(BigCellWriter.getStateColumnName());
            kv = readCell(tableName, primaryKey, columnNames);
            rowStateStr = kv.get(BigCellWriter.getStateColumnName());
            assert rowStateStr != null;
            bigCellRowState.Deserialize(rowStateStr);
            if (!rowStateID.equals(bigCellRowState.getStateID())) {
                return new String();
                // some one change row when we read, re-read again
            }
            String value = kv.get(columnName);
            assert value != null;
            buffer.append(value);
        }
        return buffer.toString();
    }
}
