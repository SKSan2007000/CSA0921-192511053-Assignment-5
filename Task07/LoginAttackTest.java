import java.sql.*;

public class LoginAttackTest {
    static boolean unsafeLogin(Connection con, String id, String password)
            throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id='" + id
                   + "' AND pwd='" + password + "'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    static boolean safeLogin(Connection con, String id, String password)
            throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        if (url == null || user == null || pass == null) {
            System.out.println("Set DB_URL, DB_USER and DB_PASSWORD to run this MySQL test.");
            return;
        }
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id VARCHAR(40) PRIMARY KEY, pwd VARCHAR(80))");
                st.executeUpdate("DELETE FROM users");
                st.executeUpdate("INSERT INTO users VALUES ('student1','pass123')");
            }
            String attack = "' OR '1'='1";
            System.out.println("Unsafe login accepted: " + unsafeLogin(con, "nobody", attack));
            System.out.println("PreparedStatement accepted: " + safeLogin(con, "nobody", attack));
        }
    }
}
