package kz.iitu.spring_lab_1.catalog;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        return repository.findAll().stream()
                .filter(b -> b.author().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        return repository.save(book);
    }

    public Optional<Book> replace(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        Book updated = new Book(id, book.title(), book.author(), book.year());
        return Optional.of(repository.save(updated));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    // Вариант 1: Поиск по фрагменту названия без учета регистра
    public List<Book> searchByTitle(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String lowerQuery = query.toLowerCase();
        return repository.findAll().stream()
                .filter(b -> b.title().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}