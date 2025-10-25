import java.util.Objects;

public class Book {
    private String id;
    private String title;
    private String author;
    private Integer year;
    private String isbn;
    private String genres;

    public Book(String id, String title, String author, Integer year, String isbn, String genres) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn == null ? "" : isbn;
        this.genres = genres == null ? "" : genres;
    }

    public static Book create(String title, String author, Integer year, String isbn, String genres) {
        return new Book(java.util.UUID.randomUUID().toString(), title, author, year, isbn, genres);
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Integer getYear() { return year; }
    public String getIsbn() { return isbn; }
    public String getGenres() { return genres; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYear(Integer year) { this.year = year; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setGenres(String genres) { this.genres = genres; }

    public boolean matchesFullText(String q) {
        if (q == null) return false;
        String s = (title + " " + author + " " + (year==null?"":year) + " " + isbn + " " + genres).toLowerCase();
        return s.contains(q.toLowerCase());
    }

    public String briefString() {
        return String.format("[%s] %s — %s", id, title, author);
    }

    public String detailedString() {
        return String.format("ID:%s | Title:%s | Author:%s | Year:%s | ISBN:%s | Genres:%s",
                id, title, author, year == null ? "-" : year.toString(),
                isbn == null || isbn.isEmpty() ? "-" : isbn,
                genres == null || genres.isEmpty() ? "-" : genres);
    }

    public String toFileLine() {
        return escape(id) + "|" + escape(title) + "|" + escape(author) + "|" +
                escape(year == null ? "" : year.toString()) + "|" + escape(isbn) + "|" + escape(genres);
    }

    public static Book fromFileLine(String line) {
        java.util.List<String> parts = Storage.splitUnescaped(line, '|');
        if (parts.size() < 6) throw new IllegalArgumentException("Invalid line");
        String id = unescape(parts.get(0));
        String title = unescape(parts.get(1));
        String author = unescape(parts.get(2));
        String yearStr = unescape(parts.get(3));
        Integer year = null;
        if (!yearStr.isEmpty()) {
            try { year = Integer.parseInt(yearStr); } catch (NumberFormatException ignored) {}
        }
        String isbn = unescape(parts.get(4));
        String genres = unescape(parts.get(5));
        return new Book(id, title, author, year, isbn, genres);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n").replace("\r", "");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char nxt = s.charAt(i + 1);
                if (nxt == '\\') { sb.append('\\'); i++; }
                else if (nxt == '|') { sb.append('|'); i++; }
                else if (nxt == 'n') { sb.append('\n'); i++; }
                else { sb.append(nxt); i++; }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
