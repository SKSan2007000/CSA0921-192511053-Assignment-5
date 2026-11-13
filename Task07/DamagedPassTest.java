import java.nio.file.*;
import java.util.Arrays;

public class DamagedPassTest {
    public static void main(String[] args) throws Exception {
        Path file = Path.of("damaged_passes.csv");
        Files.writeString(file,
                "P1,192311001,Arun,12,4500.00\n" +
                "P2,DAMAGED LINE\n" +
                "P3,192311003,Divya,7,not-a-number\n" +
                "\n" +
                "P4,192311004,Kumar,8,3900.00\n");
        PassStore store = new PassStore(file);
        System.out.println("Valid record: " + Arrays.toString(store.find("P4")));
        System.out.println("Wrong field count: " + store.find("P2"));
        System.out.println("Invalid amount: " + store.find("P3"));
        System.out.println("Empty line: skipped while searching");
        Files.deleteIfExists(file);
    }
}
