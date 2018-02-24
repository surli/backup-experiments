package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.web.WeixinWebSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用以测试的控制器
 *
 * @author CJ
 */
@Profile("staging")
@Controller
public class StagingController extends AbstractLoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private SystemService systemService;

    @GetMapping("/loginAs/{id}")
    public RedirectView loginAs(@PathVariable("id") long id, HttpServletRequest request, HttpServletResponse response) {
        loginToSecurity(loginService.findOne(id), request, response);
        if (WeixinWebSpringConfig.isWeixinRequest(request)) {
            return new RedirectView(systemService.toMobileHomeUrl());
        }
        return new RedirectView(systemService.toDesktopUrl(""));
    }

}
