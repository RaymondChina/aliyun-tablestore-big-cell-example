import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BigCellRowState {
    List<String> readyCells = new LinkedList<String>();
    List<String> unreadyCells = new LinkedList<String>();

    public String getStateID() {
        return stateID;
    }

    String stateID = null;
    final static String SMALL_SEPARATOR = ",";
    final static String BIG_SEPARATOR = ";";
    final static String PLACE_HOLDER = "PH";

    BigCellRowState()
    {
        stateID = UUID.randomUUID().toString();
    }
    BigCellRowState copy() {
        BigCellRowState s = new BigCellRowState();
        s.readyCells.addAll(this.readyCells);
        s.unreadyCells.addAll(this.unreadyCells);
        s.stateID = this.stateID;
        return s;
    }
    public List<String> getReadyCells() {
        return readyCells;
    }
    public List<String> getUnreadyCells() {
        return unreadyCells;
    }

    void addReadyCell(String readyCell)
    {
        readyCells.add(readyCell);
    }
    void addUnreadyCell(String unreadyCell)
    {
        unreadyCells.add(unreadyCell);
    }
    String Serialize()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(stateID);
        buffer.append(BIG_SEPARATOR);

        List<String> listStr = new LinkedList<String>();
        listStr.addAll(readyCells);
        listStr.add(PLACE_HOLDER);
        for (String readyCell:
                listStr) {
            buffer.append(readyCell);
            buffer.append(SMALL_SEPARATOR);
        }
        buffer.append(BIG_SEPARATOR);
        listStr.clear();
        listStr.addAll(unreadyCells);
        listStr.add(PLACE_HOLDER);
        for (String unready :
                listStr) {
            buffer.append(unready);
            buffer.append(SMALL_SEPARATOR);
        }
        return buffer.toString();
    }
    void Deserialize(String str) {
        String[] slices = str.split(BIG_SEPARATOR);
        assert (slices.length == 3);
        stateID = slices[0];
        for (String s :
                slices[1].split(SMALL_SEPARATOR)) {
            if (!s.equals(PLACE_HOLDER)) {
                readyCells.add(s);
            }
        }
        for (String s :
                slices[2].split(SMALL_SEPARATOR)) {
            if (!s.equals(PLACE_HOLDER)) {
                unreadyCells.add(s);
            }
        }
    }
}
