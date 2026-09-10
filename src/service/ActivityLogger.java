package service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ActivityLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String logFilePath;

    public ActivityLogger(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(String userId, String action, String targetEntityId) {
        String entry = String.format("%s | %s | %s | %s", LocalDateTime.now().format(FMT), userId, action, targetEntityId);
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFilePath, true))) {
            pw.print(entry + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public ArrayList<String> getRecentLogs(int count) {
        ArrayList<String> allLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) allLines.add(line);
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
        int start = Math.max(0, allLines.size() - count);
        return new ArrayList<>(allLines.subList(start, allLines.size()));
    }

    public ArrayList<String> getAllLogs() {
        return getRecentLogs(Integer.MAX_VALUE);
    }
}
