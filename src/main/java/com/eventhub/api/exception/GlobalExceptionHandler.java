package com.eventhub.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// O @ControllerAdvice avisa o Spring: "Essa classe vai escutar erros de TODOS os controllers"
@ControllerAdvice
public class GlobalExceptionHandler {

    // Quando falhar a validação do DTO (@NotBlank, @NotNull, etc), o Spring lança essa exceção.
    // Nós capturamos ela e formatamos uma resposta amigável.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage); // Ex: "title": "O título é obrigatório"
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Retorna HTTP 400 (Bad Request)
    }

    // Trata exceções genéricas de regra de negócio, como a RuntimeException que lançamos no EventService
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // Retorna HTTP 400
    }
}
