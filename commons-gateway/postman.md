# 测试openapi接口

POST http://localhost:8080/openapi/v1/biz/methods
{
    "app_id": "dev_ota_cn_appid",
    "method": "consumer.metadata.system",
    "format": "JSON",
    "charset": "utf-8",
    "sign_type": "SHA256",
    "timestamp": "2014-07-24 03:07:50",
    "version": "1.0",
    "request_id": "1624613288981",
    "biz_content": "{\"partNumber\":\"8888111156\"}",
    "sign":"54B1A9C38E86B58947DE201C74FB83994493D2DFEBB17F6A694FA01446119047"
}
