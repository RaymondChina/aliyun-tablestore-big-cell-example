import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by shiquan.yangsq on 2016/8/17.
 */
public class BigCellWriterUnitTest {
    @Test
    public void TestSplitData()
    {
        StringBuffer buffer = new StringBuffer();
        while (buffer.length() < 224) {
            buffer.append("correlations that could help insulate " +
                    "investment portfolios from the broad swings in an econ");
        }
        BigCellWriter writer = new BigCellWriter(null);
        String str = buffer.toString();
        List<String> strList = writer.splitData(str, 40);

        StringBuffer buffer1 = new StringBuffer();
        for (String s :
                strList) {
            buffer1.append(s);
        }
        assertEquals(buffer.toString().equals(buffer1.toString()), true);
    }
}
