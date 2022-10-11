package com.book.libraryapi.Service;

import com.book.libraryapi.Dto.LoanFilterDTO;
import com.book.libraryapi.Exception.BusinessException;
import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Model.Loan;
import com.book.libraryapi.Repository.LoanRepository;
import com.book.libraryapi.Service.Impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {
    @MockBean
    LoanRepository repository;
    LoanService service;
    static final LocalDate DATE = LocalDate.now();
    static final String CUSTOMER = "João";

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Salvar um novo empréstimo.")
    public void testSaveNewLoan(){
        Book book = Book.builder().id(1L).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(CUSTOMER)
                .loanDate(DATE)
                .build();
        Loan savedLoan = Loan.builder()
                .id(1L)
                .loanDate(DATE)
                .customer("CUSTOMER")
                .book(book)
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan saveLan = service.save(savingLoan);

        assertThat(saveLan.getId()).isEqualTo(savedLoan.getId());
        assertThat(saveLan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(saveLan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(saveLan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Retornar um EXCEPTION caso o livro já emprestado")
    public void testLoanedBookSave(){
        Book book = Book.builder().id(1L).build();

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(CUSTOMER)
                .loanDate(DATE)
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable ex = catchThrowable( () -> service.save(savingLoan));

        assertThat(ex).isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository,Mockito.never()).save(savingLoan);
    }

    @Test
    @DisplayName("Opter informações de emprestimo pelo id")
    public void testGetLoanDetails(){
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(repository).findById(id);
    }

    @Test
    @DisplayName("Atualizar um empréstimo existente.")
    public void testUpdateLoan(){
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        Mockito.when(repository.save(loan))
                .thenReturn(loan);

        Loan updateLan = service.update(loan);

        assertThat(updateLan.getReturned()).isTrue();
        Mockito.verify(repository).save(loan);
    }

    @Test
    @DisplayName("Buscar/filtrar empréstimos pelas suas própriedades")
    public void testFindLoanFilter(){
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();

        Loan loan = createLoan();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        Mockito.when( repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))
        )
                .thenReturn(page);

        Page<Loan> result = service.find( loanFilterDTO, pageRequest );


        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();

        return  Loan.builder()
                .book(book)
                .customer(CUSTOMER)
                .loanDate(DATE)
                .build();
    }
}
