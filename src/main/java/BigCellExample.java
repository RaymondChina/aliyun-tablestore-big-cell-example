import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.model.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BigCellExample {

    OTSClient otsClient = null;
    String accessID = "";
    String accessKey = "";
    String httpEndPoint = "";
    String instanceName = "";
    String testTableName = "big_cell_table";
    String testPKName = "big_cell_pk";
    void Init()
    {
        otsClient = new OTSClient(httpEndPoint, accessID, accessKey, instanceName);
    }

    void tryCreateTable()
    {
        boolean done = false;
        ListTableResult tables = otsClient.listTable();
        for (String table: tables.getTableNames()) {
            if (testTableName.equals(table)) {
                done = true;
                break;
            }
        }
        if (false == done) {
            com.aliyun.openservices.ots.model.TableMeta tableMeta = new com.aliyun.openservices.ots.model.TableMeta(testTableName);
            tableMeta.addPrimaryKeyColumn(testPKName, PrimaryKeyType.STRING);
            CapacityUnit capacityUnit = new CapacityUnit(0, 0);
            CreateTableRequest request = new CreateTableRequest();
            request.setTableMeta(tableMeta);
            request.setReservedThroughput(capacityUnit);
            otsClient.createTable(request);
        }
    }
    String getExampleCellValue(int size)
    {
        StringBuffer value = new StringBuffer();
        value.ensureCapacity(size);
        while (value.length() < size) {
            value.append("just a test");
        }
        return value.toString();
    }
    void writeBigCell(String pkValue, String cellValue)
    {
        BigCellWriter writer = new BigCellWriter(otsClient);
        RowPrimaryKey primaryKey = new RowPrimaryKey();
        primaryKey.addPrimaryKeyColumn(testPKName, PrimaryKeyValue.INF_MIN.fromString(pkValue));
        writer.write(testTableName, primaryKey, cellValue);
    }

    String readBigCell(String pkValue)
    {
        BigCellReader reader = new BigCellReader(otsClient);
        RowPrimaryKey primaryKey = new RowPrimaryKey();
        primaryKey.addPrimaryKeyColumn(testPKName, PrimaryKeyValue.fromString(pkValue));
        return reader.tryReadBigCell(testTableName, primaryKey);
    }

    public static void main(String[] args) {
        BigCellExample example = new BigCellExample();
        example.Init();
        example.tryCreateTable();
        int CELL_SIZE = 32 * 1024 * 1024;
        String writeValue = example.getExampleCellValue(CELL_SIZE);
        long start = System.currentTimeMillis();
        example.writeBigCell("123", writeValue);
        long end = System.currentTimeMillis();
        System.out.println(String.format("write consume(ms): %d", (end - start)));
        start = System.currentTimeMillis();
        String readValue = example.readBigCell("123");
        end = System.currentTimeMillis();
        System.out.println(String.format("read consume(ms): %d", (end - start)));
        if (writeValue.equals(readValue)) {
            System.out.println("write ok, read ok");
        }
    }
}
