package academy.devdojo.springboot2.exception;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BadRequestExceptionDetails {
    private String title;
}
