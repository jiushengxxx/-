package com.ct.service.ForegroundService;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayService {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.merchant-private-key}")
    private String merchantPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    public String createOrder(String outTradeNo, String totalAmount, String subject) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                gatewayUrl,
                appId,
                merchantPrivateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(new Gson().toJson(bizContent));

        return alipayClient.pageExecute(request).getBody();
    }

    public Map<String, String> getAlipayParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            params.put(key, String.join(",", values));
        });
        return params;
    }

    public boolean verify(Map<String, String> params) throws AlipayApiException {
        return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
    }
}