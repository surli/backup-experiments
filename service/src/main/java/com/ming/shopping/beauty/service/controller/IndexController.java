package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.LoginRequest;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.exception.ClientAuthRequiredException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.ManagerModel;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.LoginRequestService;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.wx.OpenId;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册前的校验、注册、登录、发送验证码等
 *
 * @author helloztt
 */
@Controller
public class IndexController extends AbstractLoginController {

    private static final Log log = LogFactory.getLog(IndexController.class);
    @Autowired
    private LoginService loginService;
    @Autowired
    private LoginRequestService loginRequestService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ConversionService conversionService;


    /**
     * 微信授权
     *
     * @param weixinUserDetail
     * @param redirectUrl
     * @return
     */
    @GetMapping(value = SystemService.AUTH)
    public String auth(WeixinUserDetail weixinUserDetail, @RequestParam String redirectUrl
            , HttpServletRequest request, HttpServletResponse response) {
        if (weixinUserDetail != null) {
            log.debug("wxNickName:" + weixinUserDetail.getNickname() + ",openId:" + weixinUserDetail.getOpenId());
            Login login = loginService.asWechat(weixinUserDetail.getOpenId());
            if (login == null) {
                login = loginService.newEmpty(weixinUserDetail.getOpenId());
            }
            loginToSecurity(login, request, response);
            return "redirect:" + redirectUrl;
        } else {
            return "/views/error";
        }
    }

    /**
     * 判断是否授权或注册，如果已注册就登录
     *
     * @param openId
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping(value = SystemService.TO_LOGIN)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void toLogin(@OpenId String openId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (openId == null) {
            //没授权or授权过期？那就去微信那走一圈
            response.sendError(HttpStatusCustom.SC_NO_OPENID);
            response.getWriter().write(systemService.toMobileUrl(SystemService.AUTH));
        } else {
            Login login = loginService.asWechat(openId);
            if (login == null || StringUtils.isEmpty(login.getLoginName())) {
                response.sendError(HttpStatusCustom.SC_NO_USER);
            } else {
                //注册或登录成功了，加到 security 中
                loginToSecurity(login, request, response);
            }
        }
    }

    /**
     * 检查用户是否已经注册，返回200
     * <p>若已注册，在 {@link ApiResult#data} 中返回登录名</p>
     * <p>若未注册，则返回空</p>
     *
     * @param openId 微信返回的openId
     * @return
     */
    @GetMapping(value = "/isExist")
    @ResponseBody
    public ApiResult isExist(@OpenId String openId) {
        Login login = loginService.asWechat(openId);
        if (login == null) {
            return ApiResult.withOk();
        } else {
            return ApiResult.withOk(login.getLoginName());
        }
    }

    /**
     * 检查这个手机号是否已被注册
     * <p>未被注册，返回{@link HttpStatusCustom#SC_OK}={@code 200}</p>
     * <p>已被注册，返回{@link HttpStatusCustom#SC_EXPECTATION_FAILED}={@code 417}</p>
     *
     * @param mobile 手机号
     * @return
     */
    @GetMapping(value = "/isRegister/{mobile}")
    @ResponseBody
    public ApiResult isRegister(@PathVariable String mobile) {
        loginService.mobileVerify(mobile);
        return ApiResult.withOk();
    }

    /**
     * 注册登录接口
     *
     * @param weixinUserDetail
     * @param postData
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = SystemService.LOGIN)
    @ResponseStatus(HttpStatus.OK)
    public void login(WeixinUserDetail weixinUserDetail, @Valid @RequestBody LoginOrRegisterBody postData, BindingResult bindingResult
            , HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new ApiResultException(
                    //提示 XXX格式错误
                    ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), bindingResult.getAllErrors().get(0).getDefaultMessage())
                            , null));
        }
        Login login = loginService.getLogin(weixinUserDetail.getOpenId(), postData.getMobile(), postData.getAuthCode()
                , postData.getSurname(), postData.getGender(), postData.getCdKey(), postData.getGuideUserId());
        //注册或登录成功了，加到 security 中
        loginToSecurity(login, request, response);
    }

    /**
     * 管理登录申请
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/managerLoginRequest")
    @ResponseBody
    public Object managerLoginRequest(@AuthenticationPrincipal Object principal, HttpServletRequest request, HttpServletResponse response) {
        //判断是否登录
        String sessionId = request.getSession().getId();
        final Map<String, Object> result;
        if (principal instanceof String) {
            LoginRequest loginRequest = loginRequestService.newRequest(sessionId);
            response.setStatus(HttpStatusCustom.SC_ACCEPTED);
            String text = systemService.toMobileUrl("/managerLogin/" + loginRequest.getId());
            result = new HashMap<>();
            result.put("id", loginRequest.getId());
            result.put("url", text);
        } else if (principal instanceof Login) {
            Login login = (Login) principal;
            result = RowService.drawEntityToRow(login, new ManagerModel(conversionService).getDefinitions(), null);
            response.setStatus(HttpStatusCustom.SC_OK);
        } else
            throw new IllegalStateException("这是什么登录情况：" + principal);
        return result;
    }

    // 扫码登录
    @GetMapping("/managerLogin/{requestId}")
    public ResponseEntity manageLogin(@AuthenticationPrincipal Object current, @PathVariable long requestId)
            throws URISyntaxException, ClientAuthRequiredException {
        if (!(current instanceof Login)) {
            throw new ClientAuthRequiredException();
        }
        Login login = (Login) current;

        if (StringUtils.isEmpty(login.getLoginName())) {
            //说明没有这个角色或者是个空的角色
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(new URI(systemService.toMobileJoinUrl()))
                    .build();
        } else if (login.getLevelSet().stream().allMatch(ManageLevel.user::equals) || !login.isEnabled()) {
            //说明用户没有权限登录管理后台
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(new URI(systemService.toMobileHomeUrl()))
                    .build();
        } else {
            //执行登录
            loginRequestService.login(requestId, login);
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(new URI(systemService.toMobileHomeUrl()))
                    .build();
        }

    }

    @GetMapping("/currentManager")
    @ResponseBody
    public Object currentManager(@AuthenticationPrincipal Object principal, HttpServletResponse response) {
        if (principal instanceof String) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        } else {
            return RowService.drawEntityToRow(principal, new ManagerModel(conversionService).getDefinitions(), null);
        }
    }

    /**
     * 管理登录结果
     */
    @GetMapping("/manageLoginResult/{requestId}")
    @ResponseBody
    public Object manageLoginResult(@PathVariable long requestId, HttpServletRequest request, HttpServletResponse response) {
        LoginRequest loginRequest = loginRequestService.findOne(requestId);
        if (loginRequest == null) {
            //session已失效，请重新获取二维码
            response.setStatus(HttpStatusCustom.SC_SESSION_TIMEOUT);
            return null;
        } else if (loginRequest.getLogin() == null) {
            //登录尚未被获准
            response.setStatus(HttpStatusCustom.SC_NO_CONTENT);
            return null;
        } else {
            loginToSecurity(loginRequest.getLogin(), request, response);
            response.setStatus(HttpStatusCustom.SC_OK);
            return RowService.drawEntityToRow(loginRequest.getLogin(), new ManagerModel(conversionService).getDefinitions(), null);
        }
    }

    @GetMapping(SystemService.LOGIN_OUT)
    @ResponseStatus(HttpStatus.OK)
    public void logout(@AuthenticationPrincipal Object principal, HttpServletRequest request) {
        // TODO: 2018/1/31 单元测试
        String sessionId = request.getSession().getId();
        if (principal instanceof Login) {
            loginRequestService.remove(sessionId, (Login) principal);
        }
    }

    @GetMapping("/error")
    public ModelAndView error(@RequestParam int status, @RequestParam(required = false) String message) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/views/error");
        modelAndView.addObject("status", status);
        modelAndView.addObject("message", message);
        return modelAndView;
    }


}
