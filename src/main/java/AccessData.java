import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class AccessData {
    String data;
    int index;

    public AccessData(int index, String data) {
        this.index = index;
        this.data = data;
    }

    static ArrayList<String[]> DataTable = new ArrayList<String[]>();

    static void loadData() {
        try (BufferedReader myReader = new BufferedReader(
                new FileReader("friendshipkasahimatlab.txt"))) {
            String database;
            while (((database = myReader.readLine()) != null)) {
                if (database.contains(":")) {
                    DataTable.add(database.replace("|", "").split(":", 2));
                }
            }
        } catch (Exception e) {
            System.err.println("Data List loading failed");
        }
    }

    static ArrayList<AccessData> getData(String topic) {
        // AccessData ArrayList main desired outcome is to directly
        // replace the line where the (bidirectional) pair is placed
        // this done because else we have to check both elements for every line
        // in the doc with additional complication for the case where
        // the element is common is various pairs but we want to replace it in only one
        topic = topic.strip();
        String[] row;
        ArrayList<AccessData> rows = new ArrayList<AccessData>();
        for (int i = 0; i < DataTable.size(); i++) {
            row = DataTable.get(i);
            if (row[0].equalsIgnoreCase(topic)) {
                rows.add(new AccessData(i, row[1].replace("|", "").toLowerCase()));
            } else if (row[1].equalsIgnoreCase(topic)) {
                rows.add(new AccessData(i, row[0].replace("|", "").toLowerCase()));
            }
        }
        return rows;
    }

    static void addData(String[] pair) {
        DataTable.add(pair);
        writeBack();
    }

    static void replaceWith(int index, String[] pair) {
        DataTable.set(index, pair);
        writeBack();
    }

    static void writeBack() {
        try (BufferedWriter edit = new BufferedWriter(new FileWriter("friendshipkasahimatlab.txt"))) {
            for (String[] applix : DataTable) {
                edit.write("|" + applix[0] + "|:|" + applix[1] + "|" + "\n");
            }
            edit.flush();
            edit.close();
            loadData();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
