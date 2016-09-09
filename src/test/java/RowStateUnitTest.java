import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by shiquan.yangsq on 2016/8/16.
 */
public class RowStateUnitTest {
    @Test
    public void TestRowState()
    {
        {
            // only ready
            BigCellRowState state = new BigCellRowState();
            state.addReadyCell("cell1");
            state.addReadyCell("cell2");
            String str = state.Serialize();
            BigCellRowState state2 = new BigCellRowState();
            state2.Deserialize(str);
            {
                List<String> readyCells = state.getReadyCells();
                List<String> readyCells2 = state2.getReadyCells();
                assertEquals(readyCells.size(), readyCells2.size());
                for (int i = 0; i < readyCells.size(); i++) {
                    assertEquals(readyCells.get(i), readyCells2.get(i));
                }
            }
            {
                List<String> unreadyCells = state.getUnreadyCells();
                List<String> unreadyCells2 = state2.getUnreadyCells();
                assertEquals(state.getStateID().equals(state2.getStateID()), true);
                assertEquals(unreadyCells.size(), unreadyCells2.size());
                for (int i = 0; i < unreadyCells.size(); i++) {
                    assertEquals(unreadyCells.get(i), unreadyCells2.get(i));
                }
            }
        }

        {
            // only unready
            BigCellRowState state = new BigCellRowState();
            state.addUnreadyCell("cell1");
            state.addUnreadyCell("cell2");
            String str = state.Serialize();
            BigCellRowState state2 = new BigCellRowState();
            state2.Deserialize(str);
            {
                List<String> readyCells = state.getReadyCells();
                List<String> readyCells2 = state2.getReadyCells();
                assertEquals(readyCells.size(), readyCells2.size());
                for (int i = 0; i < readyCells.size(); i++) {
                    assertEquals(readyCells.get(i), readyCells2.get(i));
                }
            }
            {
                List<String> unreadyCells = state.getUnreadyCells();
                List<String> unreadyCells2 = state2.getUnreadyCells();
                assertEquals(unreadyCells.size(), unreadyCells2.size());
                for (int i = 0; i < unreadyCells.size(); i++) {
                    assertEquals(unreadyCells.get(i), unreadyCells2.get(i));
                }
            }
        }

        {
            // only ready
            BigCellRowState state = new BigCellRowState();
            state.addReadyCell("cell1");
            state.addReadyCell("cell2");
            state.addUnreadyCell("cell3");
            state.addUnreadyCell("cell4");
            String str = state.Serialize();
            BigCellRowState state2 = new BigCellRowState();
            state2.Deserialize(str);
            {
                List<String> readyCells = state.getReadyCells();
                List<String> readyCells2 = state2.getReadyCells();
                assertEquals(readyCells.size(), readyCells2.size());
                for (int i = 0; i < readyCells.size(); i++) {
                    assertEquals(readyCells.get(i), readyCells2.get(i));
                }
            }
            {
                List<String> unreadyCells = state.getUnreadyCells();
                List<String> unreadyCells2 = state2.getUnreadyCells();
                assertEquals(unreadyCells.size(), unreadyCells2.size());
                for (int i = 0; i < unreadyCells.size(); i++) {
                    assertEquals(unreadyCells.get(i), unreadyCells2.get(i));
                }
            }
        }
    }
}
