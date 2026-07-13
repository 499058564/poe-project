import java.sql.*;

public class InspectSeed {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:app-ui/src/main/resources/seed.db")) {
            int total;
            try (var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM base_items")) {
                total = rs.next() ? rs.getInt(1) : 0;
            }
            System.out.println("base_items rows: " + total);
            System.out.println();

            System.out.print("Columns: ");
            try (var cols = conn.createStatement().executeQuery("PRAGMA table_info(base_items)")) {
                while (cols.next()) System.out.print(cols.getString(2) + " | ");
            }
            System.out.println("\n");

            try (var sample = conn.createStatement().executeQuery("SELECT * FROM base_items LIMIT 3")) {
                int cc = sample.getMetaData().getColumnCount();
                for (int i = 1; i <= 3; i++) {
                    if (sample.next()) {
                        System.out.println("--- Row " + i + " ---");
                        for (int c = 1; c <= cc; c++) {
                            String val = sample.getString(c);
                            if (val != null && !val.isEmpty()) {
                                String v = val.length() > 80 ? val.substring(0, 80) + "..." : val;
                                System.out.println("  " + sample.getMetaData().getColumnName(c) + "=" + v);
                            }
                        }
                    }
                }
            }

            System.out.println("\n--- Column fill stats ---");
            try (var cols = conn.createStatement().executeQuery("PRAGMA table_info(base_items)")) {
                while (cols.next()) {
                    String cn = cols.getString(2);
                    int n;
                    try (var c = conn.createStatement().executeQuery(
                            "SELECT COUNT(*) FROM base_items WHERE \"" + cn + "\" IS NOT NULL AND CAST(\"" + cn + "\" AS TEXT) != ''")) {
                        n = c.next() ? c.getInt(1) : 0;
                    }
                    System.out.printf("  %-20s %d/%d%n", cn, n, total);
                }
            }
        }
    }
}
