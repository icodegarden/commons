package io.github.icodegarden.commons.mybatis.interceptor;

@FunctionalInterface
interface BiIntFunction<T, R> {

    /**
     * 函数主接口
     *
     * @param t 被执行类型 T
     * @param i 参数
     * @return 返回
     */
    R apply(T t, int i);

}
