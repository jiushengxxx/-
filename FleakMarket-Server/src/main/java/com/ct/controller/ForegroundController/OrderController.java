package com.ct.controller.ForegroundController;

import com.ct.model.ForegroundModel.Order;
import com.ct.service.ForegroundService.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台订单接口
 * 修正：将 getUserid() 替换为 getCid()，适配 Order 实体类的实际属性
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 统一返回结果封装
    private <T> ResponseEntity<Map<String, Object>> success(T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "操作成功");
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> fail(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("msg", msg);
        result.put("data", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 创建订单
     * 修正：将 getUserid() 改为 getCid()，适配实体类属性
     */
    @PostMapping({"/insert", "/insertOrderByOrder"})
    public ResponseEntity<Map<String, Object>> insertOrder(@RequestBody Order order) {
        try {
            // 修正：校验 cid（创建人编号），而非 userid
            if (order == null || order.getCid() <= 0) { // cid 是 int 类型，默认0，只需判<=0
                return fail("订单信息或创建人编号不能为空");
            }
            Map<String, Object> result = orderService.insertOrderByOrder(order);
            return success(result);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("resulto", 0);
            errorResult.put("msg", "创建订单失败：" + e.getMessage());
            return fail("创建订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询订单
     */
    @GetMapping("/{oid}")
    public ResponseEntity<Map<String, Object>> selectOrderById(@PathVariable String oid) {
        try {
            if (oid == null || oid.trim().isEmpty()) {
                return fail("订单ID不能为空");
            }
            Order order = orderService.selectOrderById(oid);
            return success(order);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return fail("查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单ID更新订单
     */
    @PutMapping("/{oid}")
    public ResponseEntity<Map<String, Object>> updateOrderByOid(@PathVariable String oid, @RequestBody Order order) {
        try {
            if (oid == null || oid.trim().isEmpty() || order == null) {
                return fail("订单ID或更新信息不能为空");
            }
            order.setOid(oid);
            int rows = orderService.updateOrderByOid(order);
            return success(rows > 0 ? "更新成功" : "更新失败，订单不存在");
        } catch (Exception e) {
            log.error("更新订单失败", e);
            return fail("更新订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单ID删除订单（RESTful规范：DELETE请求）
     */
    @DeleteMapping("/{oid}")
    public ResponseEntity<Map<String, Object>> deleteOrderByOid(@PathVariable String oid) {
        try {
            if (oid == null || oid.trim().isEmpty()) {
                return fail("订单ID不能为空");
            }
            Order order = new Order();
            order.setOid(oid);
            int rows = orderService.deleteOrderByOid(order);
            return success(rows > 0 ? "删除成功" : "删除失败，订单不存在");
        } catch (Exception e) {
            log.error("删除订单失败", e);
            return fail("删除订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据创建人编号（cid）查询订单列表
     * 修正：将 userid 改为 cid，适配实体类属性
     */
    @GetMapping("/user/{cid}")
    public ResponseEntity<Map<String, Object>> selectOrderByUserId(@PathVariable Integer cid) {
        try {
            // 修正：校验创建人编号 cid
            if (cid == null || cid <= 0) {
                return fail("创建人编号不能为空且必须大于0");
            }
            List<Order> orderList = orderService.selectOrderByUserId(cid); // 注意：需同步修正 OrderService 中的方法参数名
            return success(orderList);
        } catch (Exception e) {
            log.error("查询用户订单失败", e);
            return fail("查询用户订单失败：" + e.getMessage());
        }
    }

    /**
     * 多条件查询订单列表
     */
    @PostMapping("/list")
    public ResponseEntity<Map<String, Object>> selectOrderByList(@RequestBody Order order) {
        try {
            List<Order> orderList = orderService.selectOrderByList(order);
            return success(orderList);
        } catch (Exception e) {
            log.error("多条件查询订单失败", e);
            return fail("多条件查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据创建人编号（cid）查询订单关联的商品信息
     * 修正：将 userid 改为 cid
     */
    @GetMapping("/user/{cid}/products")
    public ResponseEntity<Map<String, Object>> selectOrderProductByUserId(@PathVariable Integer cid) {
        try {
            if (cid == null || cid <= 0) {
                return fail("创建人编号不能为空且必须大于0");
            }
            List<Map<String, Object>> orderProductList = orderService.selectOrderProductByUserId(cid); // 同步修正 Service 层参数
            return success(orderProductList);
        } catch (Exception e) {
            log.error("查询用户订单商品失败", e);
            return fail("查询用户订单商品失败：" + e.getMessage());
        }
    }

    /**
     * 通用更新订单
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateOrderById(@RequestBody Map<String, Object> map) {
        try {
            if (map == null || map.get("oid") == null) {
                return fail("订单ID不能为空");
            }
            int rows = orderService.updateOrderById(map);
            return success(rows > 0 ? "更新成功" : "更新失败，订单不存在");
        } catch (Exception e) {
            log.error("通用更新订单失败", e);
            return fail("通用更新订单失败：" + e.getMessage());
        }
    }

    /**
     * 生成支付宝支付表单
     */
    @GetMapping("/pay/{oid}")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable String oid) {
        try {
            if (oid == null || oid.trim().isEmpty()) {
                return fail("订单ID不能为空");
            }
            Order order = orderService.selectOrderById(oid);
            if (order == null) {
                return fail("订单不存在");
            }
            if ("2".equals(order.getPay())) {
                return fail("该订单已支付，无需重复支付");
            }
            String payForm = orderService.generatePayForm(order);
            return success(payForm);
        } catch (Exception e) {
            log.error("生成支付表单失败", e);
            return fail("生成支付表单失败：" + e.getMessage());
        }
    }

    /**
     * 支付宝支付异步回调
     */
    @PostMapping("/notify")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request) {
        try {
            Map<String, String[]> requestParams = request.getParameterMap();
            Map<String, String> params = new HashMap<>();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                if (values != null && values.length > 0) {
                    params.put(name, values[0]);
                }
            }

            boolean verified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params, alipayPublicKey, "UTF-8", "RSA2");

            if (!verified) {
                log.warn("支付宝回调验签失败，参数：{}", params);
                return "fail";
            }

            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                Order order = new Order();
                order.setOid(outTradeNo);
                order.setPay("2");
                order.setPaytime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
                int rows = orderService.updateOrderByOid(order);
                if (rows > 0) {
                    log.info("订单{}支付成功，已更新订单状态", outTradeNo);
                } else {
                    log.warn("订单{}支付成功，但更新订单状态失败", outTradeNo);
                }
            }

            return "success";
        } catch (Exception e) {
            log.error("支付宝回调处理失败", e);
            return "fail";
        }
    }
}