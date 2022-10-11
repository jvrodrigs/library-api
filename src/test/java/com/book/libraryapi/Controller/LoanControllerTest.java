package com.book.libraryapi.Controller;

import com.book.libraryapi.Dto.LoanDto;
import com.book.libraryapi.Dto.LoanFilterDTO;
import com.book.libraryapi.Dto.ReturnedLoanDTO;
import com.book.libraryapi.Exception.BusinessException;
import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Model.Loan;
import com.book.libraryapi.Repository.LoanRepository;
import com.book.libraryapi.Service.BookService;
import com.book.libraryapi.Service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static com.book.libraryapi.Service.LoanServiceTest.createLoan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_URL = "/loans/";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanRepository repository;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Realizar um emprestimo")
    public void testCreateLoan() throws Exception{
        LoanDto dto = LoanDto.builder().isbn("2509").customar("Book Consumer").build();

        String json = new ObjectMapper().writeValueAsString(dto);
        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();

        BDDMockito.given(bookService.getBookByIsbn("2509"))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1L).customer("Book Consumer").book(book)
                .loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Retornar error quando um Isbn for invalido.")
    public void testInvalidIsbnCreateRequest() throws Exception{
        LoanDto dto = LoanDto.builder().isbn("2509").customar("Book Consumer").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("2509"))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("erros", Matchers.hasSize(1)))
                .andExpect(jsonPath("erros[0]").value("Book not found for passed isbn"));
    }

    @Test
    @DisplayName("Retornar error quando um livro estiver emprestado.")
    public void testLoanedBookErrorOnCreate() throws Exception{
        LoanDto dto = LoanDto.builder().isbn("2509").customar("Book Consumer").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();
        BDDMockito.given(bookService.getBookByIsbn("2509"))
                .willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("erros", Matchers.hasSize(1)))
                .andExpect(jsonPath("erros[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void testReturnBook() throws Exception{
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true).build();
        Loan loan = Loan.builder().id(1L).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_URL.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar not found para um livro inexistente")
    public void testReturnInexistentBook() throws Exception{
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_URL.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Consulta/filtro avançado para empréstimos.")
    public void testFindLoanQuery() throws Exception {
        Long id = 1L;

        Loan loan = createLoan();
        loan.setId(id);
        Book bo = Book.builder().id(1l).isbn("2509").build();
        loan.setBook(bo);



        BDDMockito.given(loanService.find(Mockito.any(
                LoanFilterDTO.class), Mockito.any(Pageable.class)
                ))
                .willReturn(new PageImpl<Loan>( Arrays.asList(loan), PageRequest.of(0, 100), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100",
                bo.getIsbn(),
                loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_URL.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }
}
