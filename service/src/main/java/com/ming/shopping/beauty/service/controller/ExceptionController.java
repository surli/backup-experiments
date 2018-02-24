package com.ming.shopping.beauty.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.exception.ClientAuthRequiredException;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.web.flow.RedirectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定义一些异常的处理
 * Created by helloztt on 2018/1/4.
 */
@ControllerAdvice
public class ExceptionController {
    private static final Log log = LogFactory.getLog(ExceptionController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SystemService systemService;


    @ExceptionHandler(ApiResultException.class)
    public ResponseEntity noHandlerFound(ApiResultException exception, HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
        //如果没有返回提示，获取原始message
        String errorMsg = null;
        Integer errorCode = null;
        if (exception.getApiResult() == null) {
            try {
                errorMsg = HttpStatus.valueOf(exception.getHttpStatus()).getReasonPhrase();
                errorCode = exception.getHttpStatus();
            } catch (IllegalArgumentException ex) {
                //说明是自定义的
            }
        } else {
            errorMsg = exception.getApiResult().getMessage();
            errorCode = exception.getApiResult().getCode();
        }
        if (isAjaxRequestOrBackJson(request)) {
            log.debug(objectMapper.writeValueAsString(exception.getApiResult()));
            return ResponseEntity
                    .status(exception.getHttpStatus()).contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(objectMapper.writeValueAsString(exception.getApiResult()))
                    ;
        } else {
            String errorUrl = systemService.toErrorUrl(errorCode, errorMsg);
            //替换域名为请求时的域名
            if (request.getHeader("X-Forwarded-Host") != null) {
                errorUrl = errorUrl.replaceFirst("[a-zA-Z.0-9]+", request.getHeader("X-Forwarded-Host"));
            }
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(new URI(errorUrl)).build();
        }
    }

    @ExceptionHandler(RedirectException.class)
    public RedirectView redirect(RedirectException ex) {
        return new RedirectView(ex.getUrl());
    }

    // 其实下面2种行为 都是要求授权的行为
    // 区别仅在于客户端的协议不同
    // 对于支持text/html的客户端，因为支持跳转和本地再渲染html所以直接通过/auth跳转就完成了授权登录的需求
    // 而仅支持application/json 说白了只是一个数据行为；即便给它302到/auth 它依然会抓瞎；所以我们明确了
    // 协议要求客户端在识别到该响应式 以客户端的方式完成/auth的调度。

    @ExceptionHandler(ClientAuthRequiredException.class)
    public RedirectView needLogin(ClientAuthRequiredException ex, HttpServletRequest request) {
        StringBuilder uri = new StringBuilder("/auth?redirectUrl=");
        String origin = request.getRequestURL().toString();

        if (request.getHeader("X-Forwarded-Host") != null) {
            origin = origin.replace(request.getServerName(), request.getHeader("X-Forwarded-Host"));
        }

        Matcher matcher = Pattern.compile("http[s]?://[a-zA-Z.0-9]+(:\\d+).*").matcher(origin);
        if (matcher.matches()) {
            origin = origin.replace(matcher.group(1), "");
        }
        uri.append(origin);
        return new RedirectView(uri.toString());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public void needLogin(@AuthenticationPrincipal Login login, AccessDeniedException exception
            , HttpServletResponse response) throws IOException {
        if (!login.getLevelSet().contains(ManageLevel.user)) {
            response.sendError(HttpStatusCustom.SC_NO_USER);
        }
    }

    @ExceptionHandler(Exception.class)
    public void forPrintException(Exception e) throws Exception {
        log.error("NO_HANDLED_EX", e);
        throw e;
    }

    /***
     * 判断当前请求是否是ajax请求或者是返回json格式字符串
     * @param request
     * @return
     */
    private boolean isAjaxRequestOrBackJson(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        return !(!StringUtils.isEmpty(accept) && (
                accept.toLowerCase().contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        || accept.toLowerCase().contains(MediaType.TEXT_HTML_VALUE)));
    }
}
