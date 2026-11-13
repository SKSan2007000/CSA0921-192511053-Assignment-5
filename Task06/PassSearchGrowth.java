import java.io.*;
import java.nio.file.*;

public class PassSearchGrowth {
    static void createFile(Path file, int count) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(file)) {
            for (int i = 1; i <= count; i++) {
                out.write("P" + i + ",19231" + i + ",Student" + i
                        + "," + (i % 20 + 1) + ",4500.00");
                out.newLine();
            }
        }
    }

    static String[] find(Path file, String id) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file)) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 5) continue;
                try {
                    Integer.parseInt(fields[3]);
                    Double.parseDouble(fields[4]);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (fields[0].equals(id)) return fields;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of("growth_passes.csv");
        for (int count : new int[]{1000, 10000, 50000, 100000}) {
            createFile(file, count);
            String target = "P" + count;
            find(file, target); // warm-up
            double[] times = new double[7];
            String[] result = null;
            for (int run = 0; run < times.length; run++) {
                long start = System.nanoTime();
                result = find(file, target);
                times[run] = (System.nanoTime() - start) / 1_000_000.0;
            }
            java.util.Arrays.sort(times);
            System.out.printf("%d records | found=%s | median=%.3f ms%n",
                    count, result != null, times[times.length / 2]);
        }
        Files.deleteIfExists(file);
    }
}
