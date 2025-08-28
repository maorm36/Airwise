package ambient_intelligence.logic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
public class ExternalApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalApiException() {
        super();
    }

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(Throwable cause) {
        super(cause);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
