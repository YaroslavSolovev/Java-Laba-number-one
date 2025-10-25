import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Library {
    private final List<Book> books = new ArrayList<>();

    public void add(Book b) { books.add(b); }
    public boolean removeById(String id) {
        return books.removeIf(b -> b.getId().equals(id));
    }
    public Book findById(String id) {
        for (Book b : books) if (b.getId().equals(id)) return b;
        return null;
    }
    public List<Book> listAll() { return new ArrayList<>(books); }

    public List<Book> findFullText(String q) {
        if (q == null || q.trim().isEmpty()) return Collections.emptyList();
        String qq = q.toLowerCase();
        return books.stream().filter(b -> b.matchesFullText(qq)).collect(Collectors.toList());
    }

    public List<Book> searchByAttributes(String title, String author, Integer year, String isbn, String genres) {
        String t = title == null ? "" : title.toLowerCase();
        String a = author == null ? "" : author.toLowerCase();
        String i = isbn == null ? "" : isbn.toLowerCase();
        String g = genres == null ? "" : genres.toLowerCase();
        List<Book> out = new ArrayList<>();
        for (Book b : books) {
            if (!t.isEmpty() && !b.getTitle().toLowerCase().contains(t)) continue;
            if (!a.isEmpty() && !b.getAuthor().toLowerCase().contains(a)) continue;
            if (year != null) {
                if (b.getYear() == null || !b.getYear().equals(year)) continue;
            }
            if (!i.isEmpty() && !b.getIsbn().toLowerCase().contains(i)) continue;
            if (!g.isEmpty() && !b.getGenres().toLowerCase().contains(g)) continue;
            out.add(b);
        }
        return out;
    }

    public Optional<Book> recommendRandom() {
        if (books.isEmpty()) return Optional.empty();
        Random rnd = new Random();
        return Optional.of(books.get(rnd.nextInt(books.size())));
    }

    public int totalCount() { return books.size(); }

    public List<Map.Entry<String, Long>> topAuthors(int topN) {
        Map<String, Long> byAuthor = books.stream().collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));
        return byAuthor.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    public OptionalInt maxYear() {
        return books.stream().mapToInt(b -> b.getYear() == null ? Integer.MIN_VALUE : b.getYear())
                .filter(y -> y != Integer.MIN_VALUE).max();
    }

    public void save(String fileName) throws IOException {
        Storage.save(books, fileName);
    }

    public void load(String fileName, boolean replace) throws IOException {
        List<Book> loaded = Storage.load(fileName);
        if (replace) {
            books.clear();
            books.addAll(loaded);
        } else {
            books.addAll(loaded);
        }
    }
}
