package com.aaronkersten.timetracker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static List<Task> readFile(File file) {
        List<Task> tasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            boolean skipHeader = true;
            String line;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                List<String> fields = parseLine(line);
                tasks.add(
                        new Task(
                                tasks.size() + 1,
                                fields.get(0),
                                fields.get(1),
                                fields.get(2),
                                fields.get(3)
                        ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public static void writeFile(File file, List<String> headers, List<Task> tasks) {
        try (FileWriter writer = new FileWriter(file)) {
            for (String header : headers) {
                writer.write(header + ",");
            }
            writer.write("\n");

            for (Task task : tasks) {
                writer.write(escapeForCSV(task.getStartTime()) + ",");
                writer.write(escapeForCSV(task.getEndTime()) + ",");
                writer.write(escapeForCSV(task.getDescription()) + ",");
                writer.write(escapeForCSV(task.getChargeCode()) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeForCSV(String field) {
        if (field == null) {
            return "";
        }

        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }

        fields.add(field.toString());
        return fields;
    }
}
