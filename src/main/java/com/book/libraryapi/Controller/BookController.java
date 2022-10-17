package com.book.libraryapi.Controller;

import com.book.libraryapi.Controller.Exceptions.ApiErros;
import com.book.libraryapi.Dto.BookDTO;
import com.book.libraryapi.Dto.LoanDto;
import com.book.libraryapi.Exception.BusinessException;
import com.book.libraryapi.Model.Book;
import com.book.libraryapi.Model.Loan;
import com.book.libraryapi.Service.BookService;
import com.book.libraryapi.Service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Api("Book API")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;
    private LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a book")
    public BookDTO createBook(@RequestBody @Valid BookDTO dto){
        Book entity = modelMapper.map(dto, Book.class);
        entity = service.save(entity);
        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("/{id}")
    @ApiOperation("Obtains a book details by id")
    public BookDTO getBook(@PathVariable("id") Long id){
        return service.getById(id)
                .map(bk -> modelMapper.map(bk, BookDTO.class))
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Obtains all book by params")
    public Page<BookDTO> findBookAndQuery(BookDTO dto, Pageable pgRequest){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pgRequest);

        List<BookDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(list, pgRequest, result.getTotalElements());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete a book")
    public void deleteBook(@PathVariable("id") Long id){
        Book book = service.getById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("/{id}")
    @ApiOperation("Upgrade a book")
    public BookDTO updateBook(@PathVariable("id") Long id, BookDTO bookUpdateBody){
        return service.getById(id)
            .map(book -> {
                book.setAuthor(bookUpdateBody.getAuthor());
                book.setTitle(bookUpdateBody.getTitle());

                book = service.update(book);
                return modelMapper.map(book, BookDTO.class);
        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Obtains loans by id book")
    public Page<LoanDto> LoansByBook(@PathVariable Long id, Pageable pageable){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDto> list = result.getContent().stream().map( loan -> {
                   Book bk = loan.getBook();
                   BookDTO bookDTO = modelMapper.map(bk, BookDTO.class);
                   LoanDto loanDTO = modelMapper.map(loan, LoanDto.class);
                   loanDTO.setBookDTO(bookDTO);
                   return loanDTO;
        }).collect(Collectors.toList());
        return new PageImpl<LoanDto>(list, pageable, result.getTotalElements());
    }

}
