package com.ming.shopping.beauty.service.controller;

import com.google.zxing.WriterException;
import com.ming.shopping.beauty.service.service.QRCodeService;
import com.ming.shopping.beauty.service.service.SystemService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 可以产生二维码的控制器
 *
 * @author CJ
 */
@Controller
public class QRController {

    public static final String QR_URL = "/qrImageForText";

    @Autowired
    private SystemService systemService;
    @Autowired
    private QRCodeService qrCodeService;

    @RequestMapping(method = RequestMethod.GET, value = QR_URL)
    public BufferedImage toQRCode(String text) throws IOException, WriterException {
        return qrCodeService.generateQRCode(text);
    }

    /**
     * @param text 要转换为二维码的文本
     * @return 可以展示该二维码的URL
     */
    @SneakyThrows({UnsupportedEncodingException.class, MalformedURLException.class})
    public URL urlForText(String text) {
        // 此处取决于用户请求！
        return new URL(systemService.toMobileUrl(QR_URL + "?text=" + URLEncoder.encode(text, "UTF-8")));
    }

}
