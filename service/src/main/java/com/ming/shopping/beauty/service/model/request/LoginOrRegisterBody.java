package com.ming.shopping.beauty.service.model.request;

import lombok.Data;
import me.jiangcai.wx.model.Gender;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author helloztt
 */
@Data
public class LoginOrRegisterBody {

    /**
     * 手机号必须为11为
     */
    @NotNull
    @Size(min = 11, max = 11, message = "手机号")
    private String mobile;
    /**
     * 验证码
     */
    @NotNull
    @Size(min = 4, max = 4, message = "验证码")
    private String authCode;
    /**
     * 姓氏
     */
    @Size(max = 30, message = "姓氏")
    private String surname;
    /**
     * 称谓(性别)。先生(male) / 女生(female)
     */
    private Gender gender;
    /**
     * 会员卡卡密
     */
//    @Size(min = 20, max = 20, message = "卡密")
    private String cdKey;
    /**
     * 推荐人
     */
    private Long guideUserId;
}
