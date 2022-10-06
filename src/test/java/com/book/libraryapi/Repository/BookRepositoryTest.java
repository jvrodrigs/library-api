package com.book.libraryapi.Repository;

import com.book.libraryapi.Model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Retorna verdadeiro quando existir um livro com isbn duplicado.")
    public void testReturnTrueWhenIsbnExists(){
        String isbn = "2509";

        Book book = Book.builder().author("João").title("Rodando o Mundo").isbn(isbn).build();
        testEntityManager.persist(book);

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Retorna falso quando não existir um livro com isbn duplicado.")
    public void testReturnTrueWhenIsbnDoesntExists(){
        String isbn = "2509";

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }
}
