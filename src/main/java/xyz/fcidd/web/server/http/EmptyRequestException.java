package xyz.fcidd.web.server.http;

/**
 * 空请求异常
 * 当HttpServletRequest解析请求过程中发现本次是空请求则会抛出该异常
 */
public class EmptyRequestException extends Exception {
    public EmptyRequestException() {
    }

    public EmptyRequestException(String message) {
        super(message);
    }

    public EmptyRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyRequestException(Throwable cause) {
        super(cause);
    }

    public EmptyRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}






