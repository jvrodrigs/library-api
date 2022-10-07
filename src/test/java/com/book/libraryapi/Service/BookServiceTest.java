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

import java.util.Optional;

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

    @Test
    @DisplayName("Obter informações de um livro por id")
    public void testGetById(){
        Long id = 1L;

        Book book = createValidBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> bookReturnConsult = service.getById(id);

        assertThat(bookReturnConsult.isPresent()).isTrue();
        assertThat(bookReturnConsult.get().getId()).isEqualTo(id);
        assertThat(bookReturnConsult.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(bookReturnConsult.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(bookReturnConsult.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Retornar nada quando não existir o livro")
    public void testGetByIdNotExists(){
        Long id = 1L;

        Book book = createValidBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> bookReturnConsult = service.getById(id);

        assertThat(bookReturnConsult.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deletar um livro")
    public void testDeleteBook(){
        Book book = Book.builder().id(1L).build();

        //verificando que foi usado a função, mas não deu nenhum tipo de erro
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        //meu repository foi executado pelo menos uma vez, usando aquele book.
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Livro invalido foi informado para deleção")
    public void testDeleteInvalidBook(){
        Book book = new Book();

        //Verificando que foi usando a função e foi enviado um erro
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //não foi executando nenhuma vez.
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Atualizar as informações de um livro")
    public void testUpdateBook(){
        Book book = Book.builder().id(1L).build();

        Book updatingBook = createValidBook();
        updatingBook.setId(book.getId());

        Mockito.when(repository.save(book)).thenReturn(updatingBook);

        Book updateBook = service.update(book);

        assertThat(updateBook.getId()).isEqualTo(updateBook.getId());
        assertThat(updateBook.getTitle()).isEqualTo(updateBook.getTitle());
        assertThat(updateBook.getAuthor()).isEqualTo(updateBook.getAuthor());
        assertThat(updateBook.getIsbn()).isEqualTo(updateBook.getIsbn());
    }

    @Test
    @DisplayName("Livro invalido foi informado para atualização")
    public void testUpdateInvalidBook(){
        Book book = new Book();

        //Verificando que foi usando a função e foi enviado um erro
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        //não foi executando nenhuma vez.
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createValidBook(){
        return Book.builder().author("João").title("Rodando o Mundo").isbn("2509").build();
    }
}
