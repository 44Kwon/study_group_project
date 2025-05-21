package com.group_platform.response;

import com.group_platform.exception.ExceptionCode;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
//에러 반환 response 클래스
public class ErrorResponse {
    private int status; //HTTP 상태 코드에 맞춰 클라이언트에 응답을 전달
    private String message; //메시지는 일반적으로 사용자가 이해할 수 있는 수준의 설명
    private List<FieldError> fieldErrors; //필드 단위에서 발생한 유효성 검사 오류, 주로 폼 제출 시 발생하는 오류들에 대해 각 필드에서 발생한 오류들을 포함
    private List<ConstraintViolationError> violationErrors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(List<FieldError> fieldErrors, List<ConstraintViolationError> violationErrors) {
        this(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()); //400 고정
        this.fieldErrors = fieldErrors;
        this.violationErrors = violationErrors;
    }

    public static ErrorResponse of(HttpStatus status) {
        return new ErrorResponse(status.value(), status.getReasonPhrase());
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message);
    }

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(FieldError.of(bindingResult), null);
    }

    public static ErrorResponse of(Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(null, ConstraintViolationError.of(violations));
    }

    public static ErrorResponse of(ExceptionCode exceptionCode) {
        return new ErrorResponse(exceptionCode.getStatus(), exceptionCode.getMessage());
    }

    @Getter
    //Spring의 데이터 바인딩 및 검증에서 사용
    //MethodArgumentNotValidException이 발생했을 때, 이 오류는 FieldError 객체에 담김
    public static class FieldError {
        private String field;   //오류가 발생한 필드명
        private Object rejectedValue;   //클라이언트가 전송한 잘못된 값
        private String reason;  //검증 실패의 이유

        private FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors =
                    bindingResult.getFieldErrors();

            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage()
                    ))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    //Bean Validation(예: @NotNull, @Size, @Min 등)에서 발생하는 오류
    //ConstraintViolationException이 발생했을 때 사용
    public static class ConstraintViolationError {
        private String propertyPath;    //오류가 발생한 속성이나 경로
        private Object rejectedValue;   // 클라이언트에서 보낸 잘못된 값
        private String reason;  //해당 제약 조건이 위반된 이유

        private ConstraintViolationError(String propertyPath, Object rejectedValue, String reason) {
            this.propertyPath = propertyPath;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<ConstraintViolationError> of(Set<ConstraintViolation<?>> constraintViolations) {
            return constraintViolations.stream()
                    .map(error -> new ConstraintViolationError(
                            error.getPropertyPath().toString(),
                            error.getInvalidValue(),
                            error.getMessage()
                    ))
                    .collect(Collectors.toList());
        }
    }
}
