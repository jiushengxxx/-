package com.ct.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AlipayConfig {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")  // 配置文件中是 alipay.private-key，匹配
    private String privateKey;

    // 关键修改：public-key → alipay-public-key，匹配配置文件的键名
    @Value("${alipay.alipay-public-key}")
    private String publicKey;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                privateKey,
                "json",
                "UTF-8",
                publicKey,
                "RSA2"
        );
    }

}