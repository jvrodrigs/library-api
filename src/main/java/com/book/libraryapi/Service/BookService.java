package com.book.libraryapi.Service;

import com.book.libraryapi.Model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(Book filter, Pageable pgRequest);

    Optional<Book> getBookByIsbn(String book);
}
