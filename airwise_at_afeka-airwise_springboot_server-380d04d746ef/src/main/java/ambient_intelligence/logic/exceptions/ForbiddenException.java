package ambient_intelligence.logic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

	private static final long serialVersionUID = -1694544275260534004L;
	
	public ForbiddenException() {
	}

	public ForbiddenException(String user) {
		super(user);
	}

	public ForbiddenException(Throwable cause) {
		super(cause);
	}

	public ForbiddenException(String user, Throwable cause) {
		super(user, cause);
	}

}
