import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");
    private static final Library lib = new Library();

    public static void main(String[] args) {
        System.out.println("=== Менеджер библиотеки (OOP) ===");
        System.out.println("Файлы сохраняются/загружаются из: " + Storage.getBaseDirectory());
        loop();
    }

    private static void loop() {
        boolean run = true;
        while (run) {
            System.out.println();
            System.out.println("Меню: 1)Add 2)Edit 3)List 4)Find 5)SearchByAttr 6)Save 7)Load 8)Delete 9)Recommend 10)Stats 11)Exit");
            String cmd = prompt("Введите команду (номер или слово):").trim().toLowerCase();
            switch (cmd) {
                case "1": case "add": handleAdd(); break;
                case "2": case "edit": handleEdit(); break;
                case "3": case "list": handleList(); break;
                case "4": case "find": handleFind(); break;
                case "5": case "search": case "searchbyattr": handleSearchByAttr(); break;
                case "6": case "save": handleSave(); break;
                case "7": case "load": handleLoad(); break;
                case "8": case "delete": case "del": handleDelete(); break;
                case "9": case "recommend": handleRecommend(); break;
                case "10": case "stats": handleStats(); break;
                case "11": case "exit": case "quit": run = false; System.out.println("До свидания!"); break;
                default: System.out.println("Неизвестная команда.");
            }
        }
    }

    private static void handleAdd() {
        System.out.println("--- Add ---");
        String title = promptNotEmpty("Title:");
        String author = promptNotEmpty("Author:");
        Integer year = promptIntOrNull("Year (можно пусто):");
        String isbn = prompt("ISBN (можно пусто):").trim();
        String genres = prompt("Genres (через запятую, можно пусто):").trim();
        lib.add(Book.create(title, author, year, isbn, genres));
        System.out.println("Добавлено.");
    }

    private static void handleEdit() {
        System.out.println("--- Edit ---");
        handleListBrief();
        String id = promptNotEmpty("ID to edit:");
        Book b = lib.findById(id);
        if (b == null) { System.out.println("Not found."); return; }
        System.out.println("Current: " + b.detailedString());
        String t = prompt("New title (Enter to keep):");
        if (!t.trim().isEmpty()) b.setTitle(t.trim());
        String a = prompt("New author (Enter to keep):");
        if (!a.trim().isEmpty()) b.setAuthor(a.trim());
        String y = prompt("New year (Enter to keep):");
        if (!y.trim().isEmpty()) {
            try { b.setYear(Integer.parseInt(y.trim())); } catch (NumberFormatException e) { System.out.println("Bad year, ignored"); }
        }
        String i = prompt("New ISBN (Enter to keep):");
        if (!i.trim().isEmpty()) b.setIsbn(i.trim());
        String g = prompt("New genres (Enter to keep):");
        if (!g.trim().isEmpty()) b.setGenres(g.trim());
        System.out.println("Updated.");
    }

    private static void handleList() {
        System.out.println("--- List ---");
        List<Book> all = lib.listAll();
        if (all.isEmpty()) { System.out.println("No books."); return; }
        for (Book b : all) System.out.println(b.detailedString());
        System.out.println("Total: " + all.size());
    }

    private static void handleListBrief() {
        List<Book> all = lib.listAll();
        for (Book b : all) System.out.println(b.briefString());
    }

    private static void handleFind() {
        System.out.println("--- Find ---");
        String q = promptNotEmpty("Query:");
        List<Book> found = lib.findFullText(q);
        printResult(found);
    }

    private static void handleSearchByAttr() {
        System.out.println("--- SearchByAttr ---");
        String title = prompt("Title (частично):").trim();
        String author = prompt("Author (частично):").trim();
        String yearStr = prompt("Year (точно):").trim();
        Integer year = null;
        if (!yearStr.isEmpty()) {
            try { year = Integer.parseInt(yearStr); } catch (NumberFormatException e) { System.out.println("Bad year, ignored"); }
        }
        String isbn = prompt("ISBN (частично):").trim();
        String genres = prompt("Genres (частично):").trim();
        List<Book> found = lib.searchByAttributes(title, author, year, isbn, genres);
        printResult(found);
    }

    private static void handleSave() {
        System.out.println("--- Save ---");
        String name = prompt("File name (default library.txt):").trim();
        if (name.isEmpty()) name = "library.txt";
        try {
            lib.save(name);
            System.out.println("Saved to: " + Storage.getBaseDirectory().resolve(name.endsWith(".txt") ? name : name + ".txt").toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    private static void handleLoad() {
        System.out.println("--- Load ---");
        String name = prompt("File name (default library.txt):").trim();
        if (name.isEmpty()) name = "library.txt";
        try {
            java.util.List<Book> loaded = Storage.load(name);
            if (loaded.isEmpty()) { System.out.println("No books in file."); return; }
            String how = prompt("1)Replace 2)Append (1/2):").trim();
            if (how.equals("1")) lib.load(name, true);
            else lib.load(name, false);
            System.out.println("Loaded: " + loaded.size());
        } catch (IOException e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }

    private static void handleDelete() {
        System.out.println("--- Delete ---");
        handleListBrief();
        String id = promptNotEmpty("ID to delete:");
        String conf = prompt("Confirm deletion (yes):");
        if (conf.equalsIgnoreCase("yes")) {
            boolean ok = lib.removeById(id);
            System.out.println(ok ? "Deleted." : "Not found.");
        } else System.out.println("Cancelled.");
    }

    private static void handleRecommend() {
        System.out.println("--- Recommend ---");
        Optional<Book> pick = lib.recommendRandom();
        if (pick.isPresent()) System.out.println("Recommend: " + pick.get().detailedString());
        else System.out.println("No books.");
    }

    private static void handleStats() {
        System.out.println("--- Stats ---");
        System.out.println("Total: " + lib.totalCount());
        java.util.List<Map.Entry<String, Long>> top = lib.topAuthors(3);
        System.out.println("Top authors:");
        for (Map.Entry<String, Long> e : top) System.out.println("  " + e.getKey() + " — " + e.getValue());
        OptionalInt my = lib.maxYear();
        System.out.println("Max year: " + (my.isPresent() ? my.getAsInt() : "N/A"));
    }

    private static String prompt(String msg) {
        System.out.print(msg + " ");
        return scanner.nextLine();
    }
    private static String promptNotEmpty(String msg) {
        while (true) {
            String s = prompt(msg);
            if (!s.trim().isEmpty()) return s;
            System.out.println("Не пусто, пожалуйста.");
        }
    }
    private static Integer promptIntOrNull(String msg) {
        String s = prompt(msg).trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { System.out.println("Bad number"); return null; }
    }
    private static void printResult(java.util.List<Book> found) {
        if (found.isEmpty()) { System.out.println("Not found."); return; }
        System.out.println("Found: " + found.size());
        for (Book b : found) System.out.println(b.detailedString());
    }
}
