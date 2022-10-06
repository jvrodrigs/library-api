package com.book.libraryapi.Service;

import com.book.libraryapi.Exception.BusinessException;
import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Repository.BookRepository;
import com.book.libraryapi.Service.Impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Salvar livro")
    public void testSaveBook(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        BDDMockito.when(repository.save(book)).thenReturn(
                Book.builder()
                    .id(11L)
                    .author("João")
                    .title("Rodando o Mundo")
                    .isbn("2509").build()
        );

        Book savedBook = service.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("2509");
        assertThat(savedBook.getAuthor()).isEqualTo("João");
        assertThat(savedBook.getTitle()).isEqualTo("Rodando o Mundo");
    }

    @Test
    @DisplayName("Lançar exception ao tentar salvar um livro com um isbn duplicado")
    public void testShouldNotSaveABookWithDupicatedIsbn(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable ex = Assertions.catchThrowable( () -> service.save(book));
        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createValidBook(){
        return Book.builder().author("João").title("Rodando o Mundo").isbn("2509").build();
    }
}
