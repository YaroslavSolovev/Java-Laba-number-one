import java.io.IOException;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    public static final String HEADER = "ID|TITLE|AUTHOR|YEAR|ISBN|GENRES";

    public static void save(List<Book> books, String fileName) throws IOException {
        String safe = sanitizeFileName(fileName);
        if (!safe.toLowerCase().endsWith(".txt")) safe += ".txt";
        Path p = getBaseDirectory().resolve(safe);
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Book b : books) lines.add(b.toFileLine());
        Files.write(p, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static List<Book> load(String fileName) throws IOException {
        String safe = sanitizeFileName(fileName);
        if (!safe.toLowerCase().endsWith(".txt")) safe += ".txt";
        Path p = getBaseDirectory().resolve(safe);
        List<Book> out = new ArrayList<>();
        if (!Files.exists(p)) throw new IOException("File not found: " + p.toAbsolutePath());
        List<String> lines = Files.readAllLines(p, java.nio.charset.StandardCharsets.UTF_8);
        if (lines.isEmpty()) return out;
        int start = 0;
        if (lines.get(0).trim().equalsIgnoreCase(HEADER)) start = 1;
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            out.add(Book.fromFileLine(line));
        }
        return out;
    }

    public static Path getBaseDirectory() {
        try {
            CodeSource cs = Storage.class.getProtectionDomain().getCodeSource();
            if (cs != null) {
                Path p = Paths.get(cs.getLocation().toURI());
                if (Files.isDirectory(p)) return p.toAbsolutePath().normalize();
                else return p.getParent().toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {}
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    public static java.util.List<String> splitUnescaped(String s, char delim) {
        java.util.List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                cur.append(c);
                esc = false;
            } else {
                if (c == '\\') {
                    esc = true;
                } else if (c == delim) {
                    parts.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        parts.add(cur.toString());
        return parts;
    }

    private static String sanitizeFileName(String input) {
        String name = input.replaceAll("[\\\\/:*?\"<>|]", "_");
        int idx = Math.max(name.lastIndexOf('\\'), name.lastIndexOf('/'));
        if (idx >= 0) name = name.substring(idx + 1);
        return name;
    }
}
