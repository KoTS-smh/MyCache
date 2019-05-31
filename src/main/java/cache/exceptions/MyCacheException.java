package cache.exceptions;

public class MyCacheException extends RuntimeException {

    private int code;

    public MyCacheException(ExceptionCode exceptionCode){
        super(exceptionCode.getMsg());
        this.code = exceptionCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
