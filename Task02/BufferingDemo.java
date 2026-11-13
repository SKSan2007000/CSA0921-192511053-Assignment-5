import java.io.*;
import java.nio.file.*;

public class BufferingDemo {
    static long countSlow(Path file) throws IOException {
        long lines = 0;
        try (InputStream in = Files.newInputStream(file)) {
            int value;
            while ((value = in.read()) != -1) {
                if (value == '\n') lines++;
            }
        }
        return lines;
    }

    static long countFast(Path file) throws IOException {
        long lines = 0;
        byte[] data = new byte[8192];
        try (BufferedInputStream in =
                     new BufferedInputStream(Files.newInputStream(file))) {
            int size;
            while ((size = in.read(data)) != -1) {
                for (int i = 0; i < size; i++) {
                    if (data[i] == '\n') lines++;
                }
            }
        }
        return lines;
    }

    static void makeFile(Path file, int mb) throws IOException {
        byte[] row = new byte[100];
        for (int i = 0; i < row.length - 1; i++) row[i] = 'A';
        row[row.length - 1] = '\n';
        long target = mb * 1024L * 1024L;
        try (OutputStream out = Files.newOutputStream(file)) {
            long written = 0;
            while (written + row.length <= target) {
                out.write(row);
                written += row.length;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of("attendance.txt");
        for (int mb : new int[]{1, 10, 50}) {
            makeFile(file, mb);
            long start = System.nanoTime();
            long slowLines = countSlow(file);
            double slowMs = (System.nanoTime() - start) / 1_000_000.0;

            start = System.nanoTime();
            long fastLines = countFast(file);
            double fastMs = (System.nanoTime() - start) / 1_000_000.0;

            System.out.printf("%d MB | lines=%d | byte-read=%.3f ms | buffered=%.3f ms | same=%s%n",
                    mb, slowLines, slowMs, fastMs, slowLines == fastLines);
        }
        Files.deleteIfExists(file);
    }
}
