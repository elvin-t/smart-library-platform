-- =====================================================
-- BOOK SERVICE - CREATE BOOKS TABLE
-- =====================================================

-- Required for updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,

    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    description TEXT,
    genre VARCHAR(50) NOT NULL,

    total_copies INTEGER NOT NULL DEFAULT 1,
    available_copies INTEGER NOT NULL DEFAULT 1,

    publication_date DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT books_isbn_check
        CHECK (LENGTH(isbn) >= 10 AND LENGTH(isbn) <= 20),

    CONSTRAINT books_title_check
        CHECK (LENGTH(TRIM(title)) > 0),

    CONSTRAINT books_author_check
        CHECK (LENGTH(TRIM(author)) > 0),

    CONSTRAINT books_genre_check
        CHECK (genre IN (
            'FICTION',
            'NON_FICTION',
            'SCIENCE',
            'TECHNOLOGY',
            'HISTORY',
            'BIOGRAPHY',
            'MYSTERY',
            'ROMANCE',
            'FANTASY'
        )),

    CONSTRAINT books_total_copies_check
        CHECK (total_copies >= 1),

    CONSTRAINT books_available_copies_check
        CHECK (available_copies >= 0),

    CONSTRAINT books_copies_logic_check
        CHECK (available_copies <= total_copies),

    CONSTRAINT books_publication_date_check
        CHECK (publication_date IS NULL OR publication_date <= CURRENT_DATE)
);

-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_genre ON books(genre);
CREATE INDEX idx_books_available_copies ON books(available_copies);
CREATE INDEX idx_books_created_at ON books(created_at);

-- PostgreSQL Full-Text Search indexes
CREATE INDEX idx_books_title_fts
ON books USING GIN (to_tsvector('english', title));

CREATE INDEX idx_books_author_fts
ON books USING GIN (to_tsvector('english', author));

CREATE INDEX idx_books_title_author_fts
ON books USING GIN (to_tsvector('english', title || ' ' || author));

-- Combined functional index
CREATE INDEX idx_books_title_author_lower
ON books(LOWER(title), LOWER(author));

CREATE INDEX idx_books_available_genre
ON books(genre, available_copies)
WHERE available_copies > 0;

-- =====================================================
-- UPDATED_AT TRIGGER
-- =====================================================

CREATE TRIGGER update_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE books IS 'Book catalog for library management system';
COMMENT ON COLUMN books.id IS 'Primary key - auto-generated book ID';
COMMENT ON COLUMN books.isbn IS 'International Standard Book Number';
COMMENT ON COLUMN books.title IS 'Book title';
COMMENT ON COLUMN books.author IS 'Book author';
COMMENT ON COLUMN books.description IS 'Book description';
COMMENT ON COLUMN books.genre IS 'Book genre';
COMMENT ON COLUMN books.total_copies IS 'Total copies owned';
COMMENT ON COLUMN books.available_copies IS 'Available copies';
COMMENT ON COLUMN books.publication_date IS 'Book publication date';
COMMENT ON COLUMN books.created_at IS 'Created timestamp';
COMMENT ON COLUMN books.updated_at IS 'Updated timestamp';