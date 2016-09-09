import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.model.*;
import com.aliyun.openservices.ots.model.condition.RelationalCondition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  We suppose only
 *  1. For each row, one writer exist. if not, we should assign UUID to rowState.
 *  2. If the big cell size fluctuate over a great range, caller can first delete the whole row to save storage space
 *  3. Support STRING only. BINARY can do as the same way.
 */
public class BigCellWriter {
    OTSClient otsClient = null;
    static int SPLIT_THRESHOLD = 1 * 1024 * 1024;
    final static String STATE_COLUMN_NAME = "row_state";

    BigCellWriter(OTSClient otsClient) {
        this.otsClient = otsClient;
    }

    public static String getStateColumnName() {
        return STATE_COLUMN_NAME;
    }

    List<String> splitData(String data, int sliceLength)
    {
        List<String> slices = new LinkedList<String>();
        int start = 0;
        while (start < data.length()) {
            int end = start + sliceLength;
            slices.add(data.substring(start,
                    end > data.length() ? data.length() : end));
            start = end;
        }
        return slices;
    }
    void write(String tableName, RowPrimaryKey primaryKey, String data) {
        List<String> slices = splitData(data, SPLIT_THRESHOLD);
        List<String> columns = new LinkedList<String>();
        BigCellRowState lastBigCellRowState = new BigCellRowState();
        List<String> unreadyCells = new ArrayList<String>();
        List<String> readyCells = new ArrayList<String>();
        for (int i = 0; i < slices.size(); i++) {
            String columnName = "cn_" + Integer.toString(i);
            unreadyCells.add(columnName);
        }
        for (int i = 0; i < slices.size(); i++) {
            String columnName = unreadyCells.get(0);
            readyCells.add(columnName);
            unreadyCells.remove(0);
            BatchWriteRowRequest request = new BatchWriteRowRequest();
            RowUpdateChange rowUpdateChange = new RowUpdateChange(tableName);
            rowUpdateChange.setPrimaryKey(primaryKey);
            rowUpdateChange.addAttributeColumn(columnName, ColumnValue.fromString(slices.get(i)));
            BigCellRowState newBigCellRowState = new BigCellRowState();
            for (String str : readyCells) {
                newBigCellRowState.addReadyCell(str);
            }
            for (String str : unreadyCells) {
                newBigCellRowState.addUnreadyCell(str);
            }
            rowUpdateChange.addAttributeColumn(getStateColumnName(), ColumnValue.fromString(newBigCellRowState.Serialize()));

            /*
            *  1. first cell, just update without condition checking
            *  2. the following cells, update with checking, compare_and_swap(value in TableStore, lastState, newState)
            */
            if (i != 0) {
                String oldStr = lastBigCellRowState.Serialize();
                String newStr = newBigCellRowState.Serialize();
                RelationalCondition columnCond = new RelationalCondition(getStateColumnName(),
                        RelationalCondition.CompareOperator.EQUAL,
                        ColumnValue.fromString(lastBigCellRowState.Serialize()));
                Condition cond = new Condition();
                cond.setColumnCondition(columnCond);
                rowUpdateChange.setCondition(cond);
            }
            request.addRowChange(rowUpdateChange);
            lastBigCellRowState = newBigCellRowState;
            try {
                BatchWriteRowResult result = otsClient.batchWriteRow(request);
                if (result.getFailedRowsOfUpdate().size() != 0) {
                    throw new Exception("fail"); // NOTE : user should replace Exception with app-customized-exception
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
