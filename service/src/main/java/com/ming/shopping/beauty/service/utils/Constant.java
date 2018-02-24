package com.ming.shopping.beauty.service.utils;

import com.huotu.verification.VerificationType;
import me.jiangcai.lib.notice.Content;

import javax.activation.DataSource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 常量定义
 * Created by helloztt on 2017/12/21.
 */
public class Constant {
    public static final String UTF8_ENCODIND = "UTF-8";

    /**
     * 非空时间类型
     */
    public static final String DATE_COLUMN_DEFINITION = "timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP";
    /**
     * 可为空的时间类型
     */
    public static final String DATE_NULLABLE_COLUMN_DEFINITION = "datetime";
    public static final int FLOAT_COLUMN_SCALE = 2;
    public static final int FLOAT_COLUMN_PRECISION = 12;

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    /**
     * 银行家舍入发
     */
    public static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;
    /**
     * 管理后台分页大小默认为20,前端暂定为10
     */
    public static final int MANAGE_PAGE_SIZE = 20;
    public static final int CLIENT_PAGE_SIZE = 10;

    /**
     * 短信签名；理论上来讲应该是依赖配置的
     */
    private static final String SMS_SignName = "锋尚来美";

    /**
     * @param code     验证码
     * @param template 模板
     * @return 生成验证码所用的短信内容
     */
    public static Content generateCodeContent(VerificationType type, String code, String template) {
        return new Content() {
            @SuppressWarnings("deprecation")
            @Override
            public String asText() {
                return type.message(code);
            }

            @Override
            public List<DataSource> embedAttachments() {
                return null;
            }

            @Override
            public List<DataSource> otherAttachments() {
                return null;
            }

            @Override
            public String asHtml(Map<String, String> attachmentRefs) {
                return null;
            }

            @Override
            public String asTitle() {
                return null;
            }

            @Override
            public String signName() {
                return Constant.SMS_SignName;
            }

            @Override
            public String templateName() {
                return template;
            }

            @Override
            public Map<String, ?> templateParameters() {
                return Collections.singletonMap("code", code);
            }
        };
    }
}
