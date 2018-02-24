package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.StoreItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * 这个controller仅仅是为了解决StoreItem中URL冲突而创建的,所以将额外的方法全部禁用了,测试还是在ManageStoreControllerTest中
 *
 * @author lxf
 */
@Controller
@RequestMapping("/storeItemUpdater")
public class ManageStoreItemUpdateController {

    @Autowired
    private StoreItemService storeItemService;

    /**
     * 批量推荐/取消推荐/门店项目(可以根据itemId批量操作)
     *
     * @param putData 操作的信息
     * @return 成功失败的数量
     */
    @PutMapping("/recommended")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseBody
    public ApiResult batchRecommended(@RequestBody Map<String, Object> putData) {
        final String recommended = "recommended";
        final String storeItems = "storeItems";
        //失败的个数
        int count = 0;
        List<Long> itemList = ManageItemUpdateController.toIdList(putData, storeItems);
        //总个数
        int size = itemList.size();
        if (putData.get(recommended) != null || putData.get(storeItems) != null) {
            for (Long storeItemId : itemList) {
                try {
                    storeItemService.recommended((boolean) putData.get(recommended), storeItemId);
                } catch (Exception e) {
                    count++;
                }
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "缺少必要数据"), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    /**
     * 批量上架/下架门店项目(可以根据itemId批量操作)
     *
     * @param putData 操作的信息
     * @return 成功失败的数量
     */
    @PutMapping("/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseBody
    public ApiResult batchEnable(@RequestBody Map<String, Object> putData) {
        final String enabled = "enabled";
        final String storeItems = "storeItems";
        //失败的个数
        int count = 0;
        List<Long> itemList = ManageItemUpdateController.toIdList(putData, storeItems);
        //总个数
        int size = itemList.size();
        if (putData.get(enabled) != null || putData.get(storeItems) != null) {
            for (Long storeItemId : itemList) {
                try {
                    storeItemService.freezeOrEnable((boolean) putData.get(enabled), storeItemId);
                } catch (Exception e) {
                    count++;
                }
            }

        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "缺少必要数据"), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

}
