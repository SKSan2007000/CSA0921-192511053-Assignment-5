import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BusPassStore {
    private final Path file;

    public BusPassStore(Path file) { this.file = file; }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }

    private String[] splitRecord(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (escaped) {
                part.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '|') {
                fields.add(part.toString());
                part.setLength(0);
            } else {
                part.append(ch);
            }
        }
        if (escaped) return new String[0];
        fields.add(part.toString());
        return fields.toArray(new String[0]);
    }

    public void add(String[] pass) throws IOException {
        if (pass.length != 7) throw new IllegalArgumentException("Seven fields are required");
        try (BufferedWriter out = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (int i = 0; i < pass.length; i++) {
                if (i > 0) out.write("|");
                out.write(escape(pass[i]));
            }
            out.newLine();
        }
    }

    private boolean valid(String[] f) {
        if (f.length != 7) return false;
        try {
            Integer.parseInt(f[3]);
            Double.parseDouble(f[6]);
            return !f[0].isEmpty() && !f[1].isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String[] find(String passId) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file)) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] f = splitRecord(line);
                if (valid(f) && f[0].equals(passId)) return f;
            }
        }
        return null;
    }

    public void listByRoute(int route) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file)) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] f = splitRecord(line);
                if (valid(f) && Integer.parseInt(f[3]) == route)
                    System.out.println(line);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of("full_passes.txt");
        Files.deleteIfExists(file);
        BusPassStore store = new BusPassStore(file);
        store.add(new String[]{"P2001", "192511001", "Arun", "12",
                "Central", "2027-03-31", "4500.00"});
        store.add(new String[]{"P2002", "192511002", "Divya | Devi", "7",
                "Guindy", "2027-03-31", "3800.00"});
        Files.writeString(file, "DAMAGED|ROW\nP2003|192511003|Bad|X|Stop|2027-03-31|oops\n",
                StandardOpenOption.APPEND);
        System.out.println(Arrays.toString(store.find("P2002")));
        System.out.println("Route 12:");
        store.listByRoute(12);
        Files.deleteIfExists(file);
    }
}
