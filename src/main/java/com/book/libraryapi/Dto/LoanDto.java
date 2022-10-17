package com.book.libraryapi.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long id;
    @NotEmpty
    private String isbn;
    @NotEmpty
    private String customer;
    private BookDTO bookDTO;
    @NotEmpty
    private String customerEmail;
}
