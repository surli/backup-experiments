package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.controller.QRController;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.UserModel;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.crud.row.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author helloztt
 */
@Controller("clientUserController")
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER')")
public class UserController {
    @Autowired
    protected MainOrderService orderService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private QRController qrController;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ConversionService conversionService;

    /**
     * 获取当前登录用户信息
     *
     * @param login
     * @return
     */
    @GetMapping
    @ResponseBody
    public Object userBaseInfo(@AuthenticationPrincipal Login login) {
        return RowService.drawEntityToRow(loginService.findOne(login.getId())
                , new UserModel(loginService, conversionService).getDefinitions(), null);
    }

    /**
     * 用来给门店代表扫码的用户二维码
     *
     * @param input
     * @return
     */
    @GetMapping("/vipCard")
    @Transactional
    @ResponseBody
    public Object vipCard(@AuthenticationPrincipal Login input) {
        //未激活的用户没有二维码
        Login login = loginService.findOne(input.getId());
        if (!login.getUser().isActive()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.USER_NOT_ACTIVE));
        }
        MainOrder mainOrder = orderService.newEmptyOrder(login.getUser());
        Map<String, Object> result = new HashMap<>(3);
        //前端购物车地址
        String text = systemService.toMobileShopUrl(mainOrder.getOrderId());
        result.put("vipCard", login.getUser().getCardNo());
        result.put("qrCode", qrController.urlForText(text).toString());
        result.put("orderId", mainOrder.getOrderId());
        return result;
    }
}
