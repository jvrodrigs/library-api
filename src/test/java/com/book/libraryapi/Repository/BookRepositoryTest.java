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

        Book book = createNewBookEntityTest(isbn);
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

    @Test
    @DisplayName("Obter informações de um livro pelo id")
    public void testFindByIdBook(){
        Book book = createNewBookEntityTest("2509");
        testEntityManager.persist(book);

        boolean exists = repository.findById(book.getId()).isPresent();

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Salvar um novo livro.")
    public void testSaveBook(){
        Book book = createNewBookEntityTest("2509");
        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deletar um livro.")
    public void testDeleteBook(){
        Book book = createNewBookEntityTest("2509");
        testEntityManager.persist(book);

        Book findBook = testEntityManager.find(Book.class, book.getId());

        repository.delete(findBook);

        Book deletedBook = testEntityManager.find(Book.class, book.getId());

        assertThat(deletedBook).isNotNull();
    }

    private Book createNewBookEntityTest(String isbn) {
        return Book.builder().author("João").title("Rodando o Mundo").isbn(isbn).build();
    }
}
