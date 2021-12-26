package academy.devdojo.springboot2.exception;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
public class BadRequestExceptionDetails extends ExceptionDetails{

}
