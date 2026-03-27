package com.ct.controller.BackgroundController;

import com.ct.service.ForegroundService.AlipayService;
import com.ct.service.ForegroundService.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    // 生成支付页面
    @GetMapping("/pay")
    public String pay(@RequestParam String outTradeNo,
                      @RequestParam String totalAmount,
                      @RequestParam String subject) throws Exception {
        return alipayService.createOrder(outTradeNo, totalAmount, subject);
    }

    // 异步回调
    @PostMapping("/notify")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        Map<String, String> params = alipayService.getAlipayParams(request);
        boolean verified = alipayService.verify(params);

        if (!verified) {
            return "fail";
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 这里更新你的订单状态：pay=2、支付时间、支付宝交易号等
            orderService.markPaid(outTradeNo, tradeNo);
        }

        return "success";
    }

    // 同步回跳
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = alipayService.getAlipayParams(request);
        boolean verified = alipayService.verify(params);

        String oid = params.get("out_trade_no");

        String redirectUrl;
        if (verified) {
            redirectUrl = "http://localhost:8081/#/mine?payResult=success&oid="
                    + URLEncoder.encode(oid == null ? "" : oid, "UTF-8");
        } else {
            redirectUrl = "http://localhost:8081/#/mine?payResult=fail&oid="
                    + URLEncoder.encode(oid == null ? "" : oid, "UTF-8");
        }

        response.sendRedirect(redirectUrl);
    }
}