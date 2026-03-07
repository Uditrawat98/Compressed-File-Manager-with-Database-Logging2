import java.io.*; // Provides classes for system input and output through data streams, serialization, and the file system.
import java.nio.file.*; // Supplies classes and interfaces for file and directory paths, and operations like reading file lines.
import java.time.LocalDateTime; // Used to get the current date and time without a timezone, for record timestamps.
import java.util.*; // Contains utility classes like ArrayList and List used in storing history records.
import java.util.concurrent.locks.*; // Provides locking mechanisms (e.g., ReentrantLock) for thread safety during concurrent access.

public class DatabaseManager {

    private static final String HISTORY_FILE = "compression_history.csv";
    private static final String HEADER = "file_id,file_name,file_path,file_type,original_size,compressed_size,compression_ratio,algorithm_used,created_at";
    private static final List<HistoryRecord> records = new ArrayList<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private static int nextId = 1;

    static {
        loadHistory();
    }

    private static void loadHistory() {
        lock.lock();
        try {
            Path path = Paths.get(HISTORY_FILE);
            if (!Files.exists(path)) return;

            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) return;

            // Skip header
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                HistoryRecord r = parseRecord(line);
                if (r != null) {
                    records.add(r);
                    if (r.fileId >= nextId) nextId = r.fileId + 1;
                }
            }
        } catch (IOException e) {
            // Ignore - start fresh
        } finally {
            lock.unlock();
        }
    }

    private static HistoryRecord parseRecord(String line) {
        try {
            List<String> parts = parseCsvLine(line);
            if (parts.size() < 9) return null;
            HistoryRecord r = new HistoryRecord();
            r.fileId = Integer.parseInt(parts.get(0).trim());
            r.fileName = parts.get(1);
            r.filePath = parts.get(2);
            r.fileType = parts.get(3);
            r.originalSize = Long.parseLong(parts.get(4).trim());
            r.compressedSize = Long.parseLong(parts.get(5).trim());
            r.compressionRatio = Double.parseDouble(parts.get(6).trim());
            r.algorithmUsed = parts.get(7);
            r.createdAt = parts.get(8);
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().replace("\"\"", "\""));
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().replace("\"\"", "\""));
        return result;
    }

    private static void saveHistory() {
        try (FileWriter w = new FileWriter(HISTORY_FILE)) {
            w.write(HEADER + "\n");
            for (HistoryRecord r : records) {
                w.write(r.fileId + ",");
                w.write(escapeCsv(r.fileName) + ",");
                w.write(escapeCsv(r.filePath) + ",");
                w.write(escapeCsv(r.fileType) + ",");
                w.write(r.originalSize + ",");
                w.write(r.compressedSize + ",");
                w.write(r.compressionRatio + ",");
                w.write(escapeCsv(r.algorithmUsed) + ",");
                w.write(escapeCsv(r.createdAt) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertCompressionRecord(
            String fileName,
            String filePath,
            String fileType,
            long originalSizeBytes,
            long compressedSizeBytes,
            double compressionRatio,
            String algorithmUsed
    ) {
        lock.lock();
        try {
            HistoryRecord r = new HistoryRecord();
            r.fileId = nextId++;
            r.fileName = fileName;
            r.filePath = filePath;
            r.fileType = fileType;
            r.originalSize = originalSizeBytes;
            r.compressedSize = compressedSizeBytes;
            r.compressionRatio = compressionRatio;
            r.algorithmUsed = algorithmUsed;
            r.createdAt = LocalDateTime.now().toString();
            records.add(r);
            saveHistory();
        } finally {
            lock.unlock();
        }
    }

    public static boolean exportHistoryToCsv(String csvFilePath) {
        lock.lock();
        try (FileWriter writer = new FileWriter(csvFilePath)) {
            writer.write(HEADER + "\n");
            for (HistoryRecord r : records) {
                writer.write(r.fileId + ",");
                writer.write(escapeCsv(r.fileName) + ",");
                writer.write(escapeCsv(r.filePath) + ",");
                writer.write(escapeCsv(r.fileType) + ",");
                writer.write(r.originalSize + ",");
                writer.write(r.compressedSize + ",");
                writer.write(r.compressionRatio + ",");
                writer.write(escapeCsv(r.algorithmUsed) + ",");
                writer.write(escapeCsv(r.createdAt) + "\n");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        String result = value.replace("\"", "\"\"");
        if (result.contains(",") || result.contains("\"") || result.contains("\n")) {
            result = "\"" + result + "\"";
        }
        return result;
    }

    private static class HistoryRecord {
        int fileId;
        String fileName, filePath, fileType, algorithmUsed, createdAt;
        long originalSize, compressedSize;
        double compressionRatio;
    }
}
