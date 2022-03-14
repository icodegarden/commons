package io.github.icodegarden.commons.mybatis.interceptor;

class SqlUtils {

    private final static SqlFormatter SQL_FORMATTER = new SqlFormatter();


    /**
     * 格式sql
     *
     * @param boundSql
     * @param format
     * @return
     */
    public static String sqlFormat(String boundSql, boolean format) {
        if (format) {
            try {
                return SQL_FORMATTER.format(boundSql);
            } catch (Exception ignored) {
            }
        }
        return boundSql;
    }

    /**
     * 用%连接like
     *
     * @param str 原字符串
     * @return
     */
    public static String concatLike(String str, SqlLike type) {
        StringBuilder builder = new StringBuilder(str.length() + 3);
        switch (type) {
            case LEFT:
                builder.append(StringPool.PERCENT).append(str);
                break;
            case RIGHT:
                builder.append(str).append(StringPool.PERCENT);
                break;
            case CUSTOM:
                builder.append(str);
                break;
            default:
                builder.append(StringPool.PERCENT).append(str).append(StringPool.PERCENT);
        }
        return builder.toString();
    }

    /**
     * 获取需要转义的SQL字段
     *
     * @param dbType   数据库类型
     * @param val      值
     * @param isColumn val 是否是数据库字段
     */
    public static String sqlWordConvert(DbType dbType, String val, boolean isColumn) {
        if (dbType == DbType.POSTGRE_SQL) {
            if (isColumn && (StringUtils.isNotColumnName(val) || val.toLowerCase().equals(val))) {
                // 都是数据库字段的情况下
                // 1.手动加了转义符
                // 2.全小写之后和原值一样
                // 都直接返回
                return val;
            }
            return String.format("\"%s\"", val);
        }
        return val;
    }

    /**
     * SQL注入内容剥离
     *
     * @param sql 待处理 SQL 内容
     * @return this
     */
    public static String stripSqlInjection(String sql) {
        Assert.notNull(sql, "strip sql is null.");
        return sql.replaceAll("('.+--)|(--)|(\\|)|(%7C)", StringPool.EMPTY);
    }
}