package back.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicExceptionFormat {

    private String code;
    private String message;

    public static BasicExceptionFormat create(BasicExceptionType exceptionType){
        return new BasicExceptionFormat(
                exceptionType.getErrorCode(),
                exceptionType.getMessage()
        );
    }
}
