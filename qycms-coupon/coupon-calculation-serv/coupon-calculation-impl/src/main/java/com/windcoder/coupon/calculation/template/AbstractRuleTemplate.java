package com.windcoder.coupon.calculation.template;

import com.windcoder.coupon.calculation.api.beans.Product;
import com.windcoder.coupon.calculation.api.beans.ShoppingCart;
import com.windcoder.coupon.template.api.beans.CouponTemplateInfo;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定义通用的计算逻辑
 */
@Slf4j
public abstract class AbstractRuleTemplate implements RuleTemplate{
    @Override
    public ShoppingCart calculate(ShoppingCart order) {
        // 获取订单
        Long orderTotalAmount = getTotalPrice(order.getProducts());
        // 获取以shopId为维度的价格统计
        Map<Long,Long> sumAmount = this.getTotalPriceGroupByShop(order.getProducts());

        // 以下规则只做单个优惠券的计算
        CouponTemplateInfo template = order.getCouponInfos().get(0).getTemplate();
        // 最低消费限制
        Long threshold = template.getRule().getDiscount().getThreshold();
        // 优惠金额或者打折比例
        Long quota = template.getRule().getDiscount().getQuota();
        // 当前优惠券适用的门店ID,如果为空则作用于全店券
        Long shopId = template.getShopId();

        // 如果优惠券未指定shopId，shopTotalAmount=orderTotalAmount
        // 如果指定了shopId，则shopTotalAmount=对应门店下商品总价
        Long shopTotalAmount = (shopId == null)?orderTotalAmount:sumAmount.get(shopId);

        // 如果不符合优惠券使用标准, 则直接按原价走，不使用优惠券
        if (shopTotalAmount==null || shopTotalAmount.longValue() < threshold.longValue()) {
            log.warn("Totals of amount not meet, ur coupons are not applicable to this order");
            order.setCost(orderTotalAmount);
            order.setCouponInfos(Collections.emptyList());
            return order;
        }

        // 子类中计算新的价格
        Long newCost = calculateNewPrice(orderTotalAmount,shopTotalAmount,quota);
        // 订单价格不能小于最低价格
        if (newCost.longValue() <minCost()) {
            newCost = minCost();
        }
        order.setCost(newCost);
        log.debug("original price={}, new price={}", orderTotalAmount, newCost);
        return order;
    }


    /**
     * 金额计算具体逻辑，延迟到子类实现
     * @param orderTotalAmount
     * @param shopTotalAmount
     * @param quota
     * @return
     */
    abstract protected Long calculateNewPrice(Long orderTotalAmount, Long shopTotalAmount, Long quota);

    /**
     * 计算订单总价
     * @param productList
     * @return
     */
    protected long getTotalPrice(List<Product> productList) {
        return productList.stream().mapToLong(product -> product.getPrice() * product.getCount())
                .sum();
    }

    /**
     * 根据门店维度计算每个门店下商品价格
     *
     *  key = shopId
     *   value = 门店商品总价
     * @param products
     * @return
     */
    protected Map<Long, Long> getTotalPriceGroupByShop(List<Product> products) {
        Map<Long, Long> groups = products.stream()
                .collect(Collectors.groupingBy(m -> m.getShopId(),
                        Collectors.summingLong(p -> p.getPrice() * p.getCount()))
                );
        return groups;
    }

    /**
     * 每个订单最少必须支付1分钱
     * @return
     */
    protected long minCost() {
        return 1L;
    }

    protected long convertToDecimal(Double value) {
        return new BigDecimal(value).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
