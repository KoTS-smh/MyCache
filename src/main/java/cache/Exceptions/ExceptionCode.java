package cache.Exceptions;


public enum ExceptionCode {
    MULTIPLE_PUT_ERROR(1,"缓存重复添加错误"),
    CACHE_NOT_EXIST_ERROR(2,"缓存不存在");

    private int code;
    private  String msg;

    ExceptionCode(int code,String msg){
        this.code = code;
        this.msg = msg;
    }


    public int getCode(){
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
