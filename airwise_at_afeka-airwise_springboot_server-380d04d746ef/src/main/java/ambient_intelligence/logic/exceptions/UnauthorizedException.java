package ambient_intelligence.logic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException  {

	private static final long serialVersionUID = 2292823047462240311L;
	
	public UnauthorizedException() {
	}

	public UnauthorizedException(String user) {
		super(user);
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
	}

	public UnauthorizedException(String user, Throwable cause) {
		super(user, cause);
	}
}
