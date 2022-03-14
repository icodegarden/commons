package io.github.icodegarden.commons.mybatis.interceptor;

class CommonsMybatisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommonsMybatisException(String message) {
        super(message);
    }

    public CommonsMybatisException(Throwable throwable) {
        super(throwable);
    }

    public CommonsMybatisException(String message, Throwable throwable) {
        super(message, throwable);
    }
}