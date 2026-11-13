import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class MigrationTool {
    static List<String[]> readValid(Path file, List<String> rejected) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            int number = 0;
            while ((line = r.readLine()) != null) {
                number++;
                String[] fields = line.split(",", -1);
                try {
                    if (fields.length != 5) throw new IllegalArgumentException("wrong field count");
                    Integer.parseInt(fields[3]);
                    Double.parseDouble(fields[4]);
                    rows.add(fields);
                } catch (RuntimeException e) {
                    rejected.add("line " + number + ": " + e.getMessage());
                }
            }
        }
        return rows;
    }

    static int insertRows(Connection con, List<String[]> rows) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES(?,?,?,?,?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int count = 0;
            for (String[] row : rows) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.setString(3, row[2]);
                ps.setInt(4, Integer.parseInt(row[3]));
                ps.setDouble(5, Double.parseDouble(row[4]));
                ps.addBatch();
                count++;
                if (count % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            con.commit();
            return count;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    static void showMetadata(Connection c) throws SQLException {
        DatabaseMetaData m = c.getMetaData();
        System.out.println("Database: " + m.getDatabaseProductName());
        System.out.println("Driver: " + m.getDriverVersion());
        try (ResultSet rs = m.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) System.out.println("Table: " + rs.getString("TABLE_NAME"));
        }
    }

    public static void main(String[] args) {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            System.out.println("Set DB_URL, DB_USER and DB_PASSWORD to run the database migration.");
            return;
        }
        List<String> rejected = new ArrayList<>();
        try {
            List<String[]> rows = readValid(Path.of("migration_input.csv"), rejected);
            for (String item : rejected) System.out.println("Rejected " + item);
            try (Connection con = DriverManager.getConnection(url, user, password)) {
                System.out.println("Rows migrated: " + insertRows(con, rows));
                showMetadata(con);
            }
        } catch (IOException | SQLException e) {
            System.out.println("Migration error: " + e.getMessage());
        }
    }
}
