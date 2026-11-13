public class AdversarialInputDemo {
    static boolean unsafeLogin(String sqlInput) {
        // Demonstrates why concatenating user input changes SQL structure.
        return sqlInput.contains("' OR '1'='1");
    }
    static String preparedLogin(String id,String password) {
        return "SELECT 1 FROM users WHERE id=? AND pwd=?";
    }
    public static void main(String[] args) {
        String attack="' OR '1'='1";
        System.out.println("Unsafe query accepts crafted condition: "+unsafeLogin(attack));
        System.out.println("Prepared query uses placeholders: "+preparedLogin("admin",attack));
        System.out.println("Damaged rows are skipped by PassStore validation.");
    }
}
