package com.group_platform.exception;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.group_platform.response.ErrorResponse;
import com.group_platform.webhook.SlackWebhookService;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalException {

    private final SlackWebhookService slackWebhookService;

    public GlobalException(SlackWebhookService slackWebhookService) {
        this.slackWebhookService = slackWebhookService;
    }

    //500번대 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        // 로깅 중요!
        log.error("서버 오류 발생하였습니다 : {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException e) {
        return ResponseEntity
                .status(e.getExceptionCode().getStatus())
                .body(ErrorResponse.of(e.getExceptionCode()));
    }

    // 잘못된 요청 (예: @Valid 실패 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(e));
    }

    // 제약 조건 위반 (예: @Validated + @NotNull 등)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(e.getConstraintViolations()));
    }

    //허용하지 않은 http 요청 메서 에러
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)  // e.getStatusCode()가 METHOD_NOT_ALLOWED
                .body(ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED,e.getMessage()));
    }

    // 역직렬화 중 오류(형식이 맞지 않거나 잘못된 데이터)
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,"요청한 형식이 잘못되었습니다. 입력값을 다시 확인해주세요"));
    }

    // @RequestParam 쿼리 파라미터 누락
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        return ResponseEntity
                .status(e.getStatusCode())  //HttpStatus.BAD_REQUEST
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    //@PathVariable 경로 변수 누락
    // 보통 경로 설정이 잘못된 경우가 많아서 500처리가 보통이지만
    // 스프링 버전이 올라서 Spring 내부에서는 이 예외가 입력값 바인딩 중 생긴 문제인지, 컨트롤러 또는 핸들러 매핑 문제인지 알아서 체크해준다
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(MissingPathVariableException e) {
        String message = String.format("경로 변수 '%s'가 누락되었습니다.", e.getVariableName());
        return ResponseEntity
                .status(e.getStatusCode())
                .body(ErrorResponse.of((HttpStatus) e.getStatusCode(), message));    }


    //@RequestParam, @PathVariable 등으로 전달된 값의 타입이 예상과 맞지 않을 때 발생
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message;
        Class<?> requiredType = e.getRequiredType();

        // null 체크 후 안전하게 처리할 것. Objects.requireNonNull 사용하면 NullPointerException 발생시켜셔 애플리케이션 다운될 수 있음
        if (requiredType != null) {
            message = String.format("파라미터 '%s'의 타입이 잘못되었습니다. '%s' 타입이어야 합니다.",
                    e.getName(), requiredType.getSimpleName());
        } else {
            // requiredType이 null인 경우
            message = String.format("파라미터 '%s'의 타입 정보가 없습니다.", e.getName());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  //HttpStatus.BAD_REQUEST
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    //낙관적 락 오류 처리
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, "다른 사용자가 변경 중입니다. 잠시 후 시도해주세요"));
    }

    //content-type이 지원하지 않는 타입일 때
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"지원하지 않는 Content-Type입니다"));
    }

    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<ErrorResponse> handleElasticsearchException(ElasticsearchException e) {
        log.error("Elasticsearch 오류 발생: {}", e.getMessage(), e);
        slackWebhookService.sendMessage("[알림] Elasticsearch 장애 발생 에러 로그 확인 바랍니다. " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Elasticsearch 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}
