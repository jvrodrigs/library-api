package com.book.libraryapi.Controller;

import com.book.libraryapi.Dto.BookDTO;
import com.book.libraryapi.Dto.LoanDto;
import com.book.libraryapi.Dto.LoanFilterDTO;
import com.book.libraryapi.Dto.ReturnedLoanDTO;
import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Model.Loan;
import com.book.libraryapi.Service.BookService;
import com.book.libraryapi.Service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDto loanDto){

        Book book = bookService.getBookByIsbn(loanDto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(loanDto.getCustomar())
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = service.save(entity);

        return savedLoan.getId();

    }

    @GetMapping
    public Page<LoanDto> get(LoanFilterDTO dto, Pageable pgRequest){
        Page<Loan> res = service.find(dto, pgRequest);

        List<LoanDto> result = res.getContent().stream().map(l -> {
            Book book = l.getBook();
            BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
            LoanDto loanDto = modelMapper.map(l, LoanDto.class);
            loanDto.setBookDTO(bookDTO);
            return loanDto;
        }).collect(Collectors.toList());
        return new PageImpl<LoanDto>(result, pgRequest, res.getTotalElements());
    }

    @PatchMapping("/{id}")
    public void returnBook(@PathVariable Long id,
                           @RequestBody ReturnedLoanDTO dto){

        Loan loan = service.getById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
        loan.setReturned(dto.getReturned());
        service.update(loan);
    }
}
