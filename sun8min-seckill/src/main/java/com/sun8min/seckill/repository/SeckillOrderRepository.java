package com.sun8min.seckill.repository;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayObject;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.sun8min.base.exception.MyException;
import com.sun8min.order.api.OrderService;
import com.sun8min.order.entity.Order;
import com.sun8min.order.entity.OrderLine;
import com.sun8min.product.api.ProductService;
import com.sun8min.product.api.ProductSnapshotService;
import com.sun8min.product.api.ShopService;
import com.sun8min.product.entity.Product;
import com.sun8min.product.entity.ProductSnapshot;
import com.sun8min.product.entity.Shop;
import com.sun8min.seckill.dto.PlaceOrderRequestDTO;
import com.sun8min.web.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.*;

/**
 * 下单服务
 */
@Repository
@Slf4j
public class SeckillOrderRepository {

    @Reference(version = "${service.version}")
    ShopService shopService;
    @Reference(version = "${service.version}")
    ProductService productService;
    @Reference(version = "${service.version}")
    ProductSnapshotService productSnapshotService;
    @Reference(version = "${service.version}")
    OrderService orderService;
    @Autowired
    HttpServletRequest httpServletRequest;

    /**
     * 构建下单请求
     *
     * @param userId            用户id
     * @param productSnapshotId 商品快照id
     * @return
     */
    public PlaceOrderRequestDTO buildQuestDTO(BigInteger userId, BigInteger productSnapshotId) {
        ProductSnapshot productSnapshot = productSnapshotService.getById(productSnapshotId);
        // 订单商品列表
        List<Pair<ProductSnapshot, Long>> productSnapshotQuantitiesList = new ArrayList<>();
        productSnapshotQuantitiesList.add(new ImmutablePair<>(productSnapshot, 1L));
        // 付款人
        BigInteger fromUserId = userId;
        // 购买的商店
        Shop shop = Optional.ofNullable(productSnapshot)
                .map(ProductSnapshot::getProductId)
                .map(productId -> Optional.ofNullable(productService.getById(productId))
                        .map(Product::getShopId)
                        .map(shopId -> shopService.getById(shopId))
                        .orElse(null)
                ).orElse(null);
        return new PlaceOrderRequestDTO(fromUserId, shop, productSnapshotQuantitiesList);
    }

    /**
     * 构建支付宝请求实体
     *
     * @param order
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public AlipayObject buildAlipayRequest(Order order) {
        // PC端 || 移动端
        AlipayObject bizmodel = HttpUtils.isPC(httpServletRequest) ? new AlipayTradePagePayModel() : new AlipayTradeWapPayModel();
        // 参数map
        Map<String, Object> paramsMap = new HashMap<>();
        // 商户订单号
        paramsMap.put("outTradeNo", order.getTradeOrderNo());
        // 订单总金额
        paramsMap.put("totalAmount", order.getOrderTradeAmount());
        // 订单标题
        String subject = "";
        List<OrderLine> orderLines = order.getOrderLines();
        for (int i = 0; i < orderLines.size(); i++) {
            String productName = Optional.ofNullable(productSnapshotService.getById(orderLines.get(i).getProductSnapshotId()))
                    .map(ProductSnapshot::getProductId)
                    .map(productId -> Optional.ofNullable(productService.getById(productId))
                            .map(Product::getProductName)
                            .orElse("")
                    ).orElse("");
            subject += productName + (i == orderLines.size() - 1 ? "" : " ");
        }
        paramsMap.put("subject", StringUtils.isNotBlank(subject) ? subject : "Unknown");
        //销售产品码
        String productCode = HttpUtils.isPC(httpServletRequest) ? "FAST_INSTANT_TRADE_PAY" : "QUICK_WAP_WAY";
        paramsMap.put("productCode", productCode);
        // 额外参数，因为这里传的是json字符串，带引号，需要UrlEncode编码转换2次
        // 否则可以用url参数的方式，不带引号，UrlEncode编码转换1次即可
        Map<String, Object> passbackParamsMap = new HashMap<>();
        passbackParamsMap.put("version", order.getVersion());
        String passbackParams;
        try {
            passbackParams = URLEncoder.encode(URLEncoder.encode(JSON.toJSONString(passbackParamsMap), "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MyException("额外参数使用URL编码异常");
        }
        paramsMap.put("passbackParams", passbackParams);
        log.info("paramsMap: {}" + paramsMap);

        // 实体转换
        try {
            BeanUtils.populate(bizmodel, paramsMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException("实体转换异常");
        }
        return bizmodel;
    }

    /**
     * 二次校验
     * 校验内容：订单号、订单金额是否一致、交易状态是否成功
     *
     * @param tradeOrderNo
     * @param paramsMap
     * @return
     */
    public boolean secondVerified(@PathVariable String tradeOrderNo, Map<String, String> paramsMap) {
        // 订单号
        boolean equalTradeNo = tradeOrderNo.equals(paramsMap.get("out_trade_no"));
        if (!equalTradeNo) return false;
        // 订单金额
        boolean equalTradeAmount = Optional.ofNullable(orderService.findByTradeOrderNo(tradeOrderNo))
                .map(Order::getOrderTradeAmount)
                .map(tradeAmount -> tradeAmount.compareTo(new BigDecimal(paramsMap.get("total_amount"))))
                .map(result -> result == 0)
                .orElse(false);
        if (!equalTradeAmount) return false;
        // 交易状态
        boolean tradeSuccess = "TRADE_SUCCESS".equals(paramsMap.get("trade_status"));
        if (!tradeSuccess) return false;
        return true;
    }

}
