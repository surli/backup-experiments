package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 这个controller仅仅是为了解决item中URL冲突而创建的,所以将额外的方法全部禁用了,测试还是在ManageItemControllerTest中
 *
 * @author lxf
 */
@Controller
@RequestMapping("/itemUpdater")
public class ManageItemUpdateController {

    @Autowired
    private ItemService itemService;

    static List<Long> toIdList(@RequestBody Map<String, Object> putData, String name) {
        Collection<?> requestItems = (Collection<?>) putData.get(name);
        return requestItems.stream()
                .map(obj -> (obj instanceof Number) ? (Number) obj : NumberUtils.parseNumber(obj.toString(), Long.class))
                .map(Number::longValue)
                .collect(Collectors.toList());
    }

    /**
     * 项目批量上架下架
     *
     * @param putData 上下架
     */
    @PutMapping("/enabled")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_PLATFORM_AUDIT_ITEM + "')")
    public ApiResult enabled(@RequestBody Map<String, Object> putData) {
        final String param = "enabled";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Long> itemList = toIdList(putData, items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null || putData.get(items) != null) {
            if (itemList.size() != 0) {
                for (Long id : itemList) {
                    try {
                        itemService.freezeOrEnable(id, (boolean) putData.get(param));
                    } catch (Exception e) {
                        count++;
                    }
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    /**
     * 项目批量推荐/取消推荐
     * TODO: 讲道理商户应该改不了的吧……
     *
     * @param putData
     */
    @PutMapping("/recommended")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_PLATFORM_AUDIT_ITEM + "','" + Login.ROLE_MERCHANT_ITEM + "')")
    public ApiResult recommended(@RequestBody Map<String, Object> putData) {
        final String param = "recommended";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Long> itemList = toIdList(putData, items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null) {
            if (itemList.size() != 0) {
                for (Long id : itemList) {
                    itemService.recommended(id, (boolean) putData.get(param));
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }


}
