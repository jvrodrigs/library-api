package com.book.libraryapi.Controller.Exceptions;

import com.book.libraryapi.Exception.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErros {
    private List<String> erros;

    public ApiErros(BindingResult bindingResult){
        this.erros = new ArrayList<>();

        bindingResult.getAllErrors().forEach(err ->{
            erros.add(err.getDefaultMessage());
        });
    }

    public ApiErros(BusinessException ex){
        this.erros = Arrays.asList(ex.getMessage());
    }

    public List<String> getErros() {
        return erros;
    }
}
