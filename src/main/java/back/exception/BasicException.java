package back.exception;

import lombok.Getter;

@Getter
public class BasicException extends RuntimeException{

    private final BasicExceptionType exceptionType;

    public BasicException(BasicExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public BasicException(Throwable cause, BasicExceptionType exceptionType) {
        super(cause);
        this.exceptionType = exceptionType;
    }
}
