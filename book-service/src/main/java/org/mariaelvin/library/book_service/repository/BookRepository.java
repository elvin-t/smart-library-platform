package org.mariaelvin.library.book_service.repository;

import org.mariaelvin.library.book_service.dto.BookGenre;
import org.mariaelvin.library.book_service.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Page<Book> findByGenre(BookGenre genre, Pageable pageable);

    Page<Book> findByAvailableCopiesGreaterThan(Integer availableCopies, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title,
            String author,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT *
                    FROM books b
                    WHERE to_tsvector('english', b.title || ' ' || b.author)
                          @@ plainto_tsquery('english', :keyword)
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM books b
                    WHERE to_tsvector('english', b.title || ' ' || b.author)
                          @@ plainto_tsquery('english', :keyword)
                    """,
            nativeQuery = true
    )
    Page<Book> searchByFullText(String keyword, Pageable pageable);

    Page<Book> findByAvailableCopiesEquals(Integer availableCopies, Pageable pageable);

    Page<Book> findByAvailableCopiesLessThanEqual(Integer availableCopies, Pageable pageable);

}