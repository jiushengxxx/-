package com.ct.service.ForegroundService;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ct.mapper.ForegroundMapper.OrderMapper;
import com.ct.model.ForegroundModel.Order;
import com.ct.model.ForegroundModel.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单服务实现类
 * 优化点：
 * 1. 配置项注入（替代硬编码）
 * 2. 参数名统一（userid → cid）
 * 3. 线程安全优化（SimpleDateFormat → DateTimeFormatter）
 * 4. AlipayClient 单例复用
 * 5. 完善异常处理和参数校验
 * 6. 支付金额格式校验
 */
@Slf4j
@Service
public class OrderService {

    // ========== 支付宝配置（从配置文件注入，替代硬编码） ==========
    @Value("${alipay.app-id}")
    private String alipayAppId;

    @Value("${alipay.private-key}")
    private String merchantPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway-url}")
    private String alipayGateway;

    @Value("${alipay.notify-url}")
    private String alipayNotifyUrl;

    @Value("${alipay.return-url}")
    private String alipayReturnUrl;

    // ========== 线程安全的日期格式化器（替代SimpleDateFormat） ==========
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== AlipayClient 单例复用（避免重复创建） ==========
    private AlipayClient alipayClient;

    // 初始化AlipayClient（单例）
    @Autowired
    public void initAlipayClient() {
        this.alipayClient = new DefaultAlipayClient(
                alipayGateway,
                alipayAppId,
                merchantPrivateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );
        log.info("支付宝客户端初始化成功");
    }

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 生成支付宝支付表单
     * @param order 订单信息
     * @return 支付表单HTML字符串
     * @throws AlipayApiException 支付宝API异常
     */
    public String generatePayForm(Order order) throws AlipayApiException {
        // 参数校验
        if (order == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        if (!StringUtils.hasText(order.getOid())) {
            throw new IllegalArgumentException("订单编号不能为空");
        }
        // 支付金额格式校验（String转BigDecimal，避免非数字格式）
        String totalAmount = order.getProducttotal();
        try {
            // 校验金额格式（必须是数字，且大于0）
            double amount = Double.parseDouble(totalAmount);
            if (amount <= 0) {
                throw new IllegalArgumentException("支付金额必须大于0，当前金额：" + totalAmount);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("支付金额格式错误，必须是数字，当前值：" + totalAmount, e);
        }

        // 构建支付宝支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayNotifyUrl);
        request.setReturnUrl(alipayReturnUrl);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(order.getOid()); // 商户订单号
        model.setTotalAmount(order.getProducttotal()); // 订单总金额
        // 拼接商品名称作为订单标题（处理null情况）
        String subject = "商品购买";
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            subject = order.getProducts().stream()
                    .map(Product::getName)
                    .filter(StringUtils::hasText)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(subject);
        }
        model.setSubject(subject); // 订单标题
        model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 产品码（固定）

        request.setBizModel(model);

        // 调用支付宝API生成支付表单
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("生成支付宝支付表单失败，订单号：{}", order.getOid(), e);
            throw e; // 向上抛出，由控制器处理
        }
    }

    /**
     * 创建订单（主订单 + 订单商品关联）
     * @param order 订单信息
     * @return 操作结果（包含订单号、影响行数）
     */
    public Map<String, Object> insertOrderByOrder(Order order) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 参数校验
            if (order == null) {
                throw new IllegalArgumentException("订单信息不能为空");
            }
            if (order.getCid() <= 0) {
                throw new IllegalArgumentException("创建人编号不能为空且必须大于0");
            }
            if (order.getPid() == null || order.getPid().isEmpty()) {
                throw new IllegalArgumentException("商品ID列表不能为空");
            }

            // 生成UUID订单号（去掉横线，更简洁）
            String orderNo = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            order.setOid(orderNo);

            // 构建订单-商品关联数据
            List<Map<String, Object>> orderProductList = new ArrayList<>();
            for (Integer pid : order.getPid()) {
                if (pid == null || pid <= 0) {
                    log.warn("无效的商品ID：{}，跳过", pid);
                    continue;
                }
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("oid", orderNo);
                productMap.put("pid", pid);
                orderProductList.add(productMap);
            }

            if (orderProductList.isEmpty()) {
                throw new IllegalArgumentException("有效商品ID列表不能为空");
            }

            // 插入主订单
            int orderResult = orderMapper.insertOrderByOrder(order);
            // 批量插入订单商品关联数据
            int productResult = orderMapper.insertOrderProduct(orderProductList);

            // 封装返回结果
            resultMap.put("code", 200);
            resultMap.put("msg", "订单创建成功");
            resultMap.put("oid", orderNo);
            resultMap.put("orderInsertCount", orderResult);
            resultMap.put("productInsertCount", productResult);

            log.info("订单创建成功，订单号：{}，关联商品数：{}", orderNo, orderProductList.size());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            resultMap.put("code", 500);
            resultMap.put("msg", "创建订单失败：" + e.getMessage());
            resultMap.put("oid", null);
            resultMap.put("orderInsertCount", 0);
            resultMap.put("productInsertCount", 0);
        }
        return resultMap;
    }

    /**
     * 根据订单ID查询订单
     * @param oid 订单编号
     * @return 订单信息
     */
    public Order selectOrderById(String oid) {
        if (!StringUtils.hasText(oid)) {
            log.warn("查询订单失败：订单编号为空");
            return null;
        }
        try {
            return orderMapper.selectOrderById(oid);
        } catch (Exception e) {
            log.error("查询订单失败，订单号：{}", oid, e);
            return null;
        }
    }

    /**
     * 根据订单ID更新订单
     * @param order 订单信息
     * @return 受影响行数
     */
    public int updateOrderByOid(Order order) {
        if (order == null || !StringUtils.hasText(order.getOid())) {
            log.warn("更新订单失败：订单信息或订单编号为空");
            return 0;
        }
        try {
            int rows = orderMapper.updateOrderByOid(order);
            log.info("更新订单成功，订单号：{}，受影响行数：{}", order.getOid(), rows);
            return rows;
        } catch (Exception e) {
            log.error("更新订单失败，订单号：{}", order.getOid(), e);
            return 0;
        }
    }

    /**
     * 根据订单ID删除订单
     * @param order 订单信息（至少包含oid）
     * @return 受影响行数
     */
    public int deleteOrderByOid(Order order) {
        if (order == null || !StringUtils.hasText(order.getOid())) {
            log.warn("删除订单失败：订单信息或订单编号为空");
            return 0;
        }
        try {
            return orderMapper.deleteOrderByOid(order);
        } catch (Exception e) {
            log.error("删除订单失败，订单号：{}", order.getOid(), e);
            return 0;
        }
    }

    /**
     * 根据创建人编号（cid）查询订单列表（修正：userid → cid）
     * @param cid 创建人编号
     * @return 订单列表
     */
    public List<Order> selectOrderByUserId(int cid) {
        if (cid <= 0) {
            log.warn("查询订单失败：创建人编号无效，cid={}", cid);
            return Collections.emptyList();
        }
        try {
            return orderMapper.selectOrderByUserId(cid);
        } catch (Exception e) {
            log.error("查询订单失败，创建人编号：{}", cid, e);
            return Collections.emptyList();
        }
    }

    /**
     * 多条件查询订单列表
     * @param order 查询条件
     * @return 订单列表
     */
    public List<Order> selectOrderByList(Order order) {
        try {
            return orderMapper.selectOrderByList(order);
        } catch (Exception e) {
            log.error("多条件查询订单失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据创建人编号查询订单关联的商品信息（修正：userid → cid）
     * @param cid 创建人编号
     * @return 订单商品列表
     */
    public List<Map<String, Object>> selectOrderProductByUserId(int cid) {
        if (cid <= 0) {
            log.warn("查询订单商品失败：创建人编号无效，cid={}", cid);
            return Collections.emptyList();
        }
        try {
            return orderMapper.selectOrderProductByUserId(cid);
        } catch (Exception e) {
            log.error("查询订单商品失败，创建人编号：{}", cid, e);
            return Collections.emptyList();
        }
    }

    /**
     * 通用更新订单
     * @param map 更新参数（至少包含oid）
     * @return 受影响行数
     */
    public int updateOrderById(Map<String, Object> map) {
        if (map == null || !map.containsKey("oid") || !StringUtils.hasText(map.get("oid").toString())) {
            log.warn("通用更新订单失败：订单编号为空");
            return 0;
        }
        try {
            return orderMapper.updateOrderById(map);
        } catch (Exception e) {
            log.error("通用更新订单失败，订单号：{}", map.get("oid"), e);
            return 0;
        }
    }

    /**
     * 标记订单为已支付
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝交易号
     */
    public void markPaid(String outTradeNo, String tradeNo) {
        if (!StringUtils.hasText(outTradeNo)) {
            log.warn("标记订单支付失败：商户订单号为空");
            return;
        }
        try {
            // 查询订单
            Order order = orderMapper.selectOrderById(outTradeNo);
            if (order == null) {
                log.warn("标记订单支付失败：订单不存在，订单号：{}", outTradeNo);
                return;
            }
            // 校验订单状态（避免重复更新）
            if ("2".equals(order.getPay())) {
                log.info("订单已支付，无需重复更新，订单号：{}", outTradeNo);
                return;
            }
            // 设置支付状态和支付时间
            order.setPay("2"); // 2 = 已支付
            order.setPaytime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            order.setTradeNo(tradeNo); // 保存支付宝交易号
            // 备注中添加交易号（可选）
            String remark = order.getRemark() == null ? "" : order.getRemark();
            order.setRemark(remark + " | 支付宝交易号：" + tradeNo);

            // 更新订单
            int rows = orderMapper.updateOrderByOid(order);
            log.info("标记订单支付成功，订单号：{}，支付宝交易号：{}，受影响行数：{}", outTradeNo, tradeNo, rows);
        } catch (Exception e) {
            log.error("标记订单支付失败，订单号：{}", outTradeNo, e);
        }
    }
}