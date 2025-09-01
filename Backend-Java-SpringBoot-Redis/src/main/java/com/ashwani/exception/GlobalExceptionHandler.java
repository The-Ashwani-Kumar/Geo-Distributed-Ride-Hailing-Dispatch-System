package com.ashwani.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExistsException(Exception ex){
        return buildResponse(HttpStatus.FOUND, "Resource Already Exists", ex.getMessage());
    }

    // Handle all uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    // Not found exceptions
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<ApiError> handleDriverNotFound(DriverNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Driver Not Found", ex.getMessage());
    }

    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<ApiError> handlePassengerNotFound(PassengerNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Passenger Not Found", ex.getMessage());
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ApiError> handleRideNotFound(RideNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Ride Not Found", ex.getMessage());
    }

    // Bad request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    // Centralized method to build API error response
    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, String message) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                error,
                message
        );
        return new ResponseEntity<>(apiError, status);
    }
}
