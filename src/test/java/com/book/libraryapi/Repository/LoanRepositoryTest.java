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
       Loan loan = createAndPersistLoan();

       Book book = loan.getBook();

        boolean exists = repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Buscar um empréstimo pelo isbn ou customer")
    public void testFindByBookIsbnCustomer(){
        Loan loan = createAndPersistLoan();

        Page<Loan> result = repository.findByBookIsbnOrCustomer("2509", "João", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    public Loan createAndPersistLoan(){
        Book book = createNewBookEntityTest("2509");
        testEntityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("João")
                .loanDate(LocalDate.now())
                .build();
        testEntityManager.persist(loan);

        return loan;
    }
}
