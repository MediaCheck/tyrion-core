package core.exceptions;

public class RequestTimeoutException extends BaseException {

    public RequestTimeoutException() {
        super();
    }

    public RequestTimeoutException(String message) {
        super(message);
    }
}
