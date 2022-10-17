package com.book.libraryapi.Repository;

import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Model.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import static com.book.libraryapi.Repository.BookRepositoryTest.createNewBookEntityTest;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private LoanRepository repository;

    @Test
    @DisplayName("Verificar se existe empréstimo não devolvido para livro.")
    public void testExistsByBookAndNotReturned(){
       Loan loan = createAndPersistLoan(LocalDate.now());

       Book book = loan.getBook();

        boolean exists = repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Buscar um empréstimo pelo isbn ou customer")
    public void testFindByBookIsbnCustomer(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findByBookIsbnOrCustomer("2509", "João", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Obter empréstimos quando a data emprestimo for menor ou igual a três dias atrás e não retornados")
    public void testFindByLoanDateLessThanAndNotReturned(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(4));

        List<Loan> loanList = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(loanList).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Retornar vazio caso não tenha emprestimos atrasados")
    public void testNotFindByLoanDateLessThanAndNotReturned(){
        createAndPersistLoan(LocalDate.now());

        List<Loan> loanList = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(loanList).isEmpty();
    }

    public Loan createAndPersistLoan(LocalDate loanDate){
        Book book = createNewBookEntityTest("2509");
        testEntityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("João")
                .loanDate(loanDate)
                .build();
        testEntityManager.persist(loan);

        return loan;
    }
}
