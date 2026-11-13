import java.sql.*;
import java.util.*;

public class JdbcComparison {
    static List<String[]> makeRows(int count) {
        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            rows.add(new String[]{"P" + i, "19231" + i, "Student" + i,
                    String.valueOf((i % 20) + 1), "4500.00"});
        }
        return rows;
    }

    static void clear(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM bus_pass");
        }
    }

    static long insertWithStatement(Connection con, List<String[]> rows)
            throws SQLException {
        clear(con);
        long start = System.nanoTime();
        try (Statement st = con.createStatement()) {
            for (String[] p : rows) {
                String sql = "INSERT INTO bus_pass VALUES ('" + p[0] + "','"
                        + p[1] + "','" + p[2] + "'," + p[3] + "," + p[4] + ")";
                st.executeUpdate(sql);
            }
        }
        return System.nanoTime() - start;
    }

    static long insertWithPrepared(Connection con, List<String[]> rows)
            throws SQLException {
        clear(con);
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        long start = System.nanoTime();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : rows) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.executeUpdate();
            }
        }
        return System.nanoTime() - start;
    }

    static long insertBatch(Connection con, List<String[]> rows)
            throws SQLException {
        clear(con);
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        boolean oldAuto = con.getAutoCommit();
        long start = System.nanoTime();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int count = 0;
            for (String[] p : rows) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.addBatch();
                count++;
                if (count % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            con.commit();
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
        return System.nanoTime() - start;
    }


    static void showTenRowMetadata(Connection con) throws SQLException {
        clear(con);
        List<String[]> rows = makeRows(10);
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : rows) {
                ps.setString(1, p[0]); ps.setString(2, p[1]);
                ps.setString(3, p[2]); ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.executeUpdate();
            }
        }
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bus_pass")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
                System.out.println(md.getColumnName(i) + " : " + md.getColumnTypeName(i));
            int count = 0;
            while (rs.next()) count++;
            System.out.println("Verified rows: " + count);
        }
    }

    static int forcedFailure(Connection con, boolean useTransaction) throws SQLException {
        clear(con);
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(!useTransaction);
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                String id = (i == 3) ? "P1" : "P" + i;
                ps.setString(1, id); ps.setString(2, "19231" + i);
                ps.setString(3, "Test" + i); ps.setInt(4, 12);
                ps.setDouble(5, 4500.0);
                ps.executeUpdate();
            }
            if (useTransaction) con.commit();
        } catch (SQLException e) {
            if (useTransaction) con.rollback();
            System.out.println("Forced failure: " + e.getMessage());
        } finally {
            con.setAutoCommit(oldAuto);
        }
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            System.out.println("Set DB_URL, DB_USER and DB_PASSWORD to run MySQL comparison.");
            return;
        }
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS bus_pass (pass_id VARCHAR(30) PRIMARY KEY, reg_no VARCHAR(30), name VARCHAR(100), route_no INT, amount DECIMAL(10,2))");
            }
            System.out.println("10-row metadata check:");
            showTenRowMetadata(con);
            System.out.println("Rows remaining after auto-commit failure: " + forcedFailure(con, false));
            System.out.println("Rows remaining after transaction failure: " + forcedFailure(con, true));
            for (int n : new int[]{1000, 10000, 100000}) {
                List<String[]> rows = makeRows(n);
                double a = insertWithStatement(con, rows) / 1_000_000.0;
                double b = insertWithPrepared(con, rows) / 1_000_000.0;
                double c = insertBatch(con, rows) / 1_000_000.0;
                System.out.printf("%d rows | Statement %.2f ms | Prepared %.2f ms | Batch %.2f ms%n",
                        n, a, b, c);
            }
        }
    }
}
