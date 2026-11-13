import java.sql.*;
import java.util.*;

public class PassLoader {
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int count = 0;
            for (String[] p : passes) {
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
            return count;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public static void main(String[] args) {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            System.out.println("Set DB_URL, DB_USER and DB_PASSWORD to run the MySQL batch test.");
            return;
        }
        int size = args.length == 0 ? 1000 : Integer.parseInt(args[0]);
        List<String[]> passes = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            passes.add(new String[]{"P" + i, "19231" + i, "Student" + i,
                    String.valueOf((i % 20) + 1), "4500.00"});
        }
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            System.out.println("Rows loaded: " + loadAll(con, passes));
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
