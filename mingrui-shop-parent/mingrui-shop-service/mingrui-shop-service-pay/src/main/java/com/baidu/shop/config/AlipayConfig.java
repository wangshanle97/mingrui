package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class AlipayConfig {

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766659";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCW7nc2al7RR9vwQNU8MdjHve3jCRmsIhQ7RPByC6oT3WSbuSaJweDdqGx+p6LlMseoFx16SLmyxzfnCvbajGA4+bxl1FLGsiaX95MIQhBYf70qrHRYmQgCkoWdp7GaLdiqzR/S56KI2Saeba08/oiFojZx4rm/rcl+3X7IUrKno4nZO0gMX3DmjkwFlxknrQaoJ/KDqKm/G6EjkZc89hXbACDk/zDAq+IyahpagKj2YESq3UZfRF0T71EzOjUjM5QCQGo2c23k6OTagQtvFlBb6CFk4IE0mG0yEMUxPy+p3d0c30vrVaI8ozDTmEpTdy6U5nCNg4VegyWG9RKj4RfhAgMBAAECggEAbfTi0rjhxPPmDn0dHBJwjOwYn7tkgZv+Q0OxLFTFHl2/SxezNM8TREjMaeU07md1P0c/yXOsHcu3NVIujv+PplKCZ2ksuObW6QWLj4uZnu/I34rt5BHw3Pbj8vxVji5yV9TCp4LrTWluEiNy/ymsSjboFUYI7tTsI8m38LcWwrtm6JAgPEtNGfygTiTOW9qxH+8TQ+mJaQdYs5U5xlLy8JPfdV2uyE4zNORTq9jNYvfxhVKNAe2OPsgIC4s9chggWNj9vIPIMJIMp5T59zibQWZ/1qwuXfTN/JS9a7jTKb8zMimNxxR63Q+PHcFMXiwuFl/Hdhj3eEp9QpbTT2KblQKBgQDl3GauhZErOfCB9mjQvhCPHYwagGof0YhRY8/UPqGKUTIzVjpRpb6a6ros8uO9p/bPOKCXnleEfSU1pHf3FVWSeBHEWbicwL4UbHm5cVWiN/Z/yTV0fpA6X7Np049hap6eJjZEjFvNOG9CkKh2d598kw1L5ogWa0znTZ3lt/nxKwKBgQCoGE9B+1/Iz+SqZMVZcYkjQXN33rcdFeBMRmR8T6CrtjTlaAzzGtX3xkWCIqFxe03/j1RzJin8ZheLQIBiHFByQC01Mm4mYE85nrMlxBEw1E5c1iOAr7sgyFLOCWRsqg1yGOQ2fhxQcquNSpM/8JRCJADzP8fsu0cSI4G6NwndIwKBgGpYo0UhVL2sC8MFLw9MsDqWkAh+2xqnhasqQ7BN3c0xrckknszBo0LnUIWVRpqriDvtVhdv0cvhXoLH+hzdyP6AALBYVlpnrkmiulp6vgTUYX5TqCCiobXwKwDOOar3ejVvNnJWge6bqpYgeWB+DdLD8kXofZPpxuXI+kGa8c+LAoGAQJwHas8aA++UbsII8NYo5fo0TbC3JWDRM8Aiw30/voBsWHjj8VUxROlasihpcVr5oe8PgKr8nF0JU6bZMdy8Nw3f0oCtweZmzTjolu2Dxgb0j85nGwFwpasZ2MPXa8T5Ig/bN+7FJ07MDaTKcFsiQUHhc53iVGWvbZNkO5UIH40CgYEAoLRjFzjwMMR+pf5NKi8XYpuoBrKdTfL+MlDpQcNstqYlWpPPwOHVjVV71x41S3mvesZfjblWD2P4O2/mwvLzJw6N4t86q/tX3Unex42/LnwmpWnS5Lr7LtDWcSD9sUYRfrbJ/voFMGtOSRlWpZjufVzgkB8Q0m3QH3Ahxs3mgLE=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt8u6CM++dRHpic3HTaRAZabP4dnupaTWgzZS6oBgo6sVcgf8asi3sSUv5xG6Z158De1IdP+5zuI40s4JKmWcP+qD6/caB2BPYBcthKUwHAYugGhimFPDj0J8DUguRtcYjA7X0103AmR6VeXYzD7UqBBOdk769ZfYa3/eka2IiC43M8EfHhC4cr5/P3hpyIYmYHt+gh/CA0sCp6fRH4MgCksz3cUqdT0yyQ9kfTqkl2oi+8OvLCtLi6C4tRICf5qhRpLqUiOM+yDT27KgVNqpMvmmPt1x6BksoU6tYXabB327ascHQToVx6H8jJawA5qnfmty/OgDg5bG4iyinCyJ2wIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/returnNotify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8900/pay/returnUrl";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
