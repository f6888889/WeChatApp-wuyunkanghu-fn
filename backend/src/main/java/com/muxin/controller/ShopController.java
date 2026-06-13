package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.ShopItem;
import com.muxin.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @org.springframework.beans.factory.annotation.Value("${app.strict-content:false}")
    private boolean strictMode;

    private static final Logger log = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopService shopService;

    @GetMapping("/items")
    public ApiResponse<List<ShopItem>> getAllItems() {
        if (strictMode) {
            log.info("审核模式拦截：获取商城列表请求已屏蔽");
            return ApiResponse.success(java.util.Collections.emptyList());
        }
        log.info("获取商城商品列表");
        List<ShopItem> items = shopService.getAllItems();
        return ApiResponse.success(items);
    }

    @GetMapping("/items/{itemId}")
    public ApiResponse<ShopItem> getItemById(@PathVariable String itemId) {
        if (strictMode) {
            log.info("审核模式拦截：获取商品详情请求已屏蔽");
            return ApiResponse.error(403, "功能维护中");
        }
        log.info("获取商品详情: itemId={}", itemId);
        ShopItem item = shopService.getItemById(itemId);
        if (item != null) {
            return ApiResponse.success(item);
        }
        return ApiResponse.error(404, "商品不存在");
    }

    @PostMapping("/redeem/{userId}/{itemId}")
    public ApiResponse<Map<String, Object>> redeemItem(@PathVariable String userId, @PathVariable String itemId) {
        if (strictMode) {
            log.info("审核模式拦截：商品兑换请求已屏蔽");
            return ApiResponse.error(403, "功能维护中，暂不支持兑换");
        }
        log.info("兑换商品: userId={}, itemId={}", userId, itemId);
        Map<String, Object> result = shopService.redeemItem(userId, itemId);
        boolean success = (boolean) result.get("success");
        if (success) {
            return ApiResponse.success("兑换成功", result);
        } else {
            return ApiResponse.error(400, (String) result.get("message"));
        }
    }

    @GetMapping("/redeemed/{userId}")
    public ApiResponse<List<ShopItem>> getRedeemedItems(@PathVariable String userId) {
        if (strictMode) {
            log.info("审核模式拦截：获取已兑换商品请求已屏蔽");
            return ApiResponse.success(java.util.Collections.emptyList());
        }
        log.info("获取已兑换商品: userId={}", userId);
        List<ShopItem> items = shopService.getRedeemedItems(userId);
        return ApiResponse.success(items);
    }
}
