package io.github.icodegarden.commons.lang.spec.sign;//package openapi.sign;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.StringWriter;
//import java.io.Writer;
//import java.security.KeyFactory;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.codec.binary.Base64;
//
///**
// * RSA的签名及验签
// *
// */
//public class RSA {
//	
//	public static void main(String[] args) throws Exception {
//		HashMap<Object, Object> hashMap = new HashMap<>();
//		hashMap.put("d", "sss");
//		hashMap.put("abc", "1");
//		hashMap.put("sign_param", "abc,d,sign_type");
//		hashMap.put("sign_type", "RSA2");
//		hashMap.put("publicKey", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlTP5aCvrv/W0/0N+R57QTrVuWDygVG8e1PnJSrHFnXcNgbXuHQKMd3NhWCwZUYcAFrx2TK8ZJKSjtTJlUWylXzju83kK+BbNA7g7cadOHMouOA5WC6f0T9lUr+QxdliVFqLx2gpO9nDXg2BEVUcEhiPX1Rd21wLJTZOzO6ZhL55JXzPgXjgcrWECDRa3WPTtkmp6opH7yEzkgm/Up5FBNavFiBrnUHfkhkRKZu1DqxYWKLVSDjQZjuaGkI16ZamxVXwaKeeHxNvU04abWugpJIB45ssGHdajA82n47+KqbOt8XLZNsjUlEuniZSjDFBMT21XwQcpWtFpcTSRtQ1ebwIDAQAB");
//		hashMap.put("privateKey", "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCVM/loK+u/9bT/Q35HntBOtW5YPKBUbx7U+clKscWddw2Bte4dAox3c2FYLBlRhwAWvHZMrxkkpKO1MmVRbKVfOO7zeQr4Fs0DuDtxp04cyi44DlYLp/RP2VSv5DF2WJUWovHaCk72cNeDYERVRwSGI9fVF3bXAslNk7M7pmEvnklfM+BeOBytYQINFrdY9O2SanqikfvITOSCb9SnkUE1q8WIGudQd+SGREpm7UOrFhYotVIONBmO5oaQjXplqbFVfBop54fE29TThpta6CkkgHjmywYd1qMDzafjv4qps63xctk2yNSUS6eJlKMMUExPbVfBByla0WlxNJG1DV5vAgMBAAECggEARRWIsPRNN1fEk8EjknP1jcwyKIcB0baV9xUlYAGKN8vtJBciBqqscY6qDxJ0MqyonN5shDs7EB1vc7G++gGtXLbW5UMxkge0q7k9RPWrlGcFGY2Bx+nflK1TqhGl2V9QwOd3xnapczByiXEkkrvMW3PzNsajjxDKNrJh9gemzk3ooig/nigvCCD1GMt9zuQPfQ2SSzigiD03Emqbd8elRBLtpfUEqZ4mLr0CmCdFJyZL7RJCfqAoZk125qA1YOmLQnoEfmqeZuivgSuRjNVBVPWE3cfrc/U97+g8P22yoWnXOHUG9sr8g7peZAYg821nub8H/NtnHAAr+gpxX2GFKQKBgQD6f4Fyhh8BQK+YZdu+uph1zwcmBxUGpzwS5FVTWp3FxiwPoXmorcM62KZrDnNlSsryxkM48iHnNZT7D2k2KV00/piZB1jXCEzs4OX0MuvXLdorexqSrk5L5a4Qw/3//Vl/mR2rvzOTsP9iQzjPGrHOkE155gLwTB8lXqgfij77lQKBgQCYeujJQbJ1/F0SrUqFzA/UQg8MfLATQ8qtOfeYuZfQEi7ichOX/NdFL40bTz9z+Uptzn8X+utWuxi5gcYB7vsIGGG2Q2P8vkl0bjXkzAXitBrvmzZ8MY/dyzOOyrOYkSH7PD8DCLJOODxZq6hYcsauNkF4gKkGzqu4MlD1QI5Q8wKBgC+fC9Hk04IedQNo4dSpjpBe8kH1eLbSFiaVR+9Xu8S8fuXd0c4Scpn+U+zoS2HHTTvIG0F5Lp7Q3ei4rkzAolqPyBzXe7ktd8pUmwLIp3M54U8A0TVvc60UFfpT+DaxSFFsn2pmJ/z82iApHWSp84GMh14ULxzVq9oj25xbYMvdAoGAS2iFlXqbIoSDFwiCkXbg4S0mOhu0DGL7af3/+BTZAnrrmuulywWtBLdGhpoDnHxOFc4Ixrg+CO+Qg7WtUil5FoDQWg4r9cO5mg4jMxq/UmWV6KePI45zQtIKlnSiqzIjZxddZke3vr1LA2HEaLGilmeYq1qdvRMak4a2yQN7HocCgYEAgBrgwAzIAZSNQmltqi8gbzkN9maLyiOVJMu1+2UxXbUwIZJBM2S0j3YocsAVSvEYiDkzjNUfRrMHLXB0ke3Yr2QqoVL9AMjLcMb7F3PT/IgOlxXY0PceGqHgYKR3uwWfRVDDFAhqKcz/ndEJ+iGywY51+na/gxf/FL/IpsURdPI=" );
//		hashMap.put("charset", "utf-8");
//		
//		String rsaSign = rsaSign(hashMap);
//		System.out.println(rsaSign);
//		
////		hashMap.put("content", "abc=1");
//		boolean rsaCheck = rsaCheck(hashMap, rsaSign);
//		System.out.println(rsaCheck);
//	}
//
//    private static final String SIGN_TYPE_RSA = "RSA";
//
//    private static final String SIGN_TYPE_RSA2 = "RSA2";
//
//    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
//
//    private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";
//
//    private static final int DEFAULT_BUFFER_SIZE = 8192;
//
//    /**
//     * RSA/RSA2 生成签名
//     *
//     * @param map
//     *            包含 sign_type、privateKey、charset
//     * @return
//     * @throws Exception
//     */
//    public static String rsaSign(Map map) throws Exception {
//        PrivateKey priKey = null;
//        java.security.Signature signature = null;
//        String signType = map.get("sign_type").toString();
//        String privateKey = map.get("privateKey").toString();
//        String charset = map.get("charset").toString();
//        String content = getSignContent(map);
//        map.put("content", content);
//        System.out.println("请求参数生成的字符串为:" + content);
//        if (SIGN_TYPE_RSA.equals(signType)) {
//            priKey = getPrivateKeyFromPKCS8(SIGN_TYPE_RSA, new ByteArrayInputStream(privateKey.getBytes()));
//            signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
//        } else if (SIGN_TYPE_RSA2.equals(signType)) {
//            priKey = getPrivateKeyFromPKCS8(SIGN_TYPE_RSA, new ByteArrayInputStream(privateKey.getBytes()));
//            signature = java.security.Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
//        } else {
//            throw new Exception("不是支持的签名类型 : : signType=" + signType);
//        }
//        signature.initSign(priKey);
//
//        if (charset.isEmpty()) {
//            signature.update(content.getBytes());
//        } else {
//            signature.update(content.getBytes(charset));
//        }
//
//        byte[] signed = signature.sign();
//
//        return new String(Base64.encodeBase64(signed));
//
//    }
//
//    /**
//     * 验签方法
//     *
//     * @param content
//     *            参数的合成字符串格式: key1=value1&key2=value2&key3=value3...
//     * @param sign
//     * @param publicKey
//     * @param charset
//     * @param signType
//     * @return
//     */
//    public static boolean rsaCheck(Map map, String sign) throws Exception {
//        java.security.Signature signature = null;
//        String signType = map.get("sign_type").toString();
//        String charset = map.get("charset").toString();
//        String content = map.get("content").toString();
//        String publicKey = map.get("publicKey").toString();
//        System.out.println(">>验证的签名为:" + sign);
//        System.out.println(">>生成签名的参数为:" + content);
//        PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));
//        if (SIGN_TYPE_RSA.equals(signType)) {
//            signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
//        } else if (SIGN_TYPE_RSA2.equals(signType)) {
//            signature = java.security.Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
//        } else {
//            throw new Exception("不是支持的签名类型 : signType=" + signType);
//        }
//        signature.initVerify(pubKey);
//
//        if (charset.isEmpty()) {
//            signature.update(content.getBytes());
//        } else {
//            signature.update(content.getBytes(charset));
//        }
//
//        return signature.verify(Base64.decodeBase64(sign.getBytes()));
//    }
//
//    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
//        if (ins == null || algorithm.isEmpty()) {
//            return null;
//        }
//
//        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//
//        byte[] encodedKey = readText(ins).getBytes();
//
//        encodedKey = Base64.decodeBase64(encodedKey);
//
//        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
//    }
//
//    public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws Exception {
//        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//
//        StringWriter writer = new StringWriter();
//        io(new InputStreamReader(ins), writer, -1);
//
//        byte[] encodedKey = writer.toString().getBytes();
//
//        encodedKey = Base64.decodeBase64(encodedKey);
//
//        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
//    }
//
//    /**
//     * 把参数合成成字符串
//     *
//     * @param sortedParams
//     * @return
//     */
//    public static String getSignContent(Map<String, String> sortedParams) {
//        StringBuffer content = new StringBuffer();
//        // app_id,method,charset,sign_type,version,bill_type,timestamp,bill_date
//        String[] sign_param = sortedParams.get("sign_param").split(",");// 生成签名所需的参数
//        List<String> keys = new ArrayList<String>();
//        for (int i = 0; i < sign_param.length; i++) {
//            keys.add(sign_param[i]);
//        }
//        Collections.sort(keys);
//        int index = 0;
//        for (int i = 0; i < keys.size(); i++) {
//            String key = keys.get(i);
//            /*if ("biz_content".equals(key)) {
//                content.append(
//                        (index == 0 ? "" : "&") + key + "={\"bill_date\":\"" + sortedParams.get("bill_date") + "\",")
//                        .append("\"bill_type\":\"" + sortedParams.get("bill_type") + "\"}");
//                index++;
//            } else {*/
//            String value = sortedParams.get(key);
//            if (!key.isEmpty() && !value.isEmpty()) {
//                content.append((index == 0 ? "" : "&") + key + "=" + value);
//                index++;
//            }
////            }
//        }
//        return content.toString();
//    }
//
//    private static String readText(InputStream ins) throws IOException {
//        Reader reader = new InputStreamReader(ins);
//        StringWriter writer = new StringWriter();
//
//        io(reader, writer, -1);
//        return writer.toString();
//    }
//
//    private static void io(Reader in, Writer out, int bufferSize) throws IOException {
//        if (bufferSize == -1) {
//            bufferSize = DEFAULT_BUFFER_SIZE >> 1;
//        }
//
//        char[] buffer = new char[bufferSize];
//        int amount;
//
//        while ((amount = in.read(buffer)) >= 0) {
//            out.write(buffer, 0, amount);
//        }
//    }
//
//}