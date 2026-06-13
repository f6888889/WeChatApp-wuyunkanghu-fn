package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.ShopItem;
import com.muxin.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private static final String SHOP_ITEMS_FILE = "shop_items.json";

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private UserService userService;

    public List<ShopItem> getAllItems() {
        List<ShopItem> items = jsonDataService.readJsonFile(SHOP_ITEMS_FILE, new TypeReference<List<ShopItem>>() {});
        return items.stream().filter(item -> item.isActive()).collect(Collectors.toList());
    }

    public ShopItem getItemById(String itemId) {
        return jsonDataService.readJsonFile(SHOP_ITEMS_FILE, new TypeReference<List<ShopItem>>() {}).stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> redeemItem(String userId, String itemId) {
        User user = userService.getUserById(userId);
        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        List<ShopItem> allItems = jsonDataService.readJsonFile(SHOP_ITEMS_FILE, new TypeReference<List<ShopItem>>() {});
        ShopItem targetItem = null;
        for (ShopItem item : allItems) {
            if (itemId.equals(item.getId())) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null || !targetItem.isActive()) {
            result.put("success", false);
            result.put("message", "商品不存在或已下架");
            return result;
        }

        if (targetItem.getStock() <= 0) {
            result.put("success", false);
            result.put("message", "商品库存不足");
            return result;
        }

        if (user.getPoints() < targetItem.getPoints()) {
            result.put("success", false);
            result.put("message", "积分不足");
            result.put("currentPoints", user.getPoints());
            result.put("requiredPoints", targetItem.getPoints());
            return result;
        }

        // Check for daily redemption limit
        String todayStr = LocalDate.now().toString();
        if (user.getLastRedeemDate() != null && user.getLastRedeemDate().equals(todayStr)) {
            result.put("success", false);
            result.put("message", "每天只能兑换一次");
            return result;
        }

        user.setPoints(user.getPoints() - targetItem.getPoints());
        user.setLastRedeemDate(todayStr);
        
        if (user.getRedeemedItems() == null) {
            user.setRedeemedItems(new ArrayList<>());
        }
        user.getRedeemedItems().add(targetItem.getId());
        userService.updateUser(userId, user);

        targetItem.setStock(targetItem.getStock() - 1);
        jsonDataService.writeJsonFile(SHOP_ITEMS_FILE, allItems);

        result.put("success", true);
        result.put("message", "兑换成功");
        result.put("itemName", targetItem.getName());
        result.put("remainingPoints", user.getPoints());
        return result;
    }

    public List<ShopItem> getRedeemedItems(String userId) {
        User user = userService.getUserById(userId);
        if (user == null || user.getRedeemedItems() == null) {
            return new ArrayList<>();
        }
        List<ShopItem> allItems = jsonDataService.readJsonFile(SHOP_ITEMS_FILE, new TypeReference<List<ShopItem>>() {});
        List<String> redeemedIds = user.getRedeemedItems();
        return allItems.stream()
                .filter(item -> redeemedIds.contains(item.getId()))
                .collect(Collectors.toList());
    }
}