package ambient_intelligence.logic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidRequestInputException extends RuntimeException{

	private static final long serialVersionUID = 3550685377966944837L;
	
	public InvalidRequestInputException() {
	}

	public InvalidRequestInputException(String message) {
		super(message);
	}

	public InvalidRequestInputException(Throwable cause) {
		super(cause);
	}

	public InvalidRequestInputException(String message, Throwable cause) {
		super(message, cause);
	}

}
