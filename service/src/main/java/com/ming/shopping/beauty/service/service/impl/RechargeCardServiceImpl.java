package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.RechargeCard_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardBatchRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.lib.notice.Content;
import me.jiangcai.lib.notice.NoticeService;
import me.jiangcai.lib.notice.To;
import me.jiangcai.lib.notice.email.EmailAddress;
import me.jiangcai.lib.notice.exception.NoticeException;
import me.jiangcai.poi.template.IllegalTemplateException;
import me.jiangcai.poi.template.POITemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by helloztt on 2018/1/4.
 */
@Service
public class RechargeCardServiceImpl implements RechargeCardService {
    private static final Log log = LogFactory.getLog(RechargeCardServiceImpl.class);
    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private RechargeCardBatchRepository rechargeCardBatchRepository;
    @Autowired
    private SystemService systemService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private Environment environment;
    @Autowired
    private POITemplateService poiTemplateService;

    @Override
    public RechargeCardBatch findBatch(long id) {
        return rechargeCardBatchRepository.findOne(id);
    }

    @Override
    public RechargeCardBatch newBatch(Login operator, long guideId, String emailAddress, int num, boolean silence)
            throws ClassNotFoundException {
        RechargeCardBatch batch = new RechargeCardBatch();
        batch.setManager(operator);
        batch.setCreateTime(LocalDateTime.now());
        batch.setGuideUser(loginRepository.getOne(guideId));
        batch.setEmailAddress(emailAddress);

        batch = rechargeCardBatchRepository.save(batch);
        Integer defaultAmount = systemService.currentCardAmount();
        // 生成特定数量的卡密
        batch.setCardSet(newCardSet(batch, num, defaultAmount));

        sendToUser(batch, silence);

        return batch;
    }

    @Override
    public void batchReport(RechargeCardBatch batch, OutputStream output) throws IOException {
        try {
            poiTemplateService.export(
                    output
                    , () -> new ArrayList<>(batch.getCardSet())
                    , null
                    , null, null, null
                    , new ClassPathResource("/recharge-card-batch-report.xml"), null
            );
        } catch (IllegalTemplateException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void sendToUser(RechargeCardBatch batch, boolean silence) throws ClassNotFoundException {
        To dist = new To() {
            @Override
            public String mobilePhone() {
                return batch.getGuideUser().getUsername();
            }

            @Override
            public Set<EmailAddress> emailTo() {
                String name = batch.getGuideUser().getWechatUser() != null
                        ? batch.getGuideUser().getWechatUser().getNickname() : batch.getGuideUser().getUsername();
                return Collections.singleton(
                        new EmailAddress(name, batch.getEmailAddress())
                );
            }
        };

        Content content = new Content() {
            @Override
            public String asText() {
                return "卡密已经发往您的邮箱:" + batch.getEmailAddress() + "，请注意查收；若该邮箱非你所有请联系客服。";
            }

            @Override
            public List<DataSource> embedAttachments() {
                return null;
            }

            @Override
            public List<DataSource> otherAttachments() {
                return Collections.singletonList(new DataSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        batchReport(batch, buffer);
                        buffer.flush();
                        return new ByteArrayInputStream(buffer.toByteArray());
                    }

                    @Override
                    public OutputStream getOutputStream() {
                        throw new IllegalStateException("not supported");
                    }

                    @Override
                    public String getContentType() {
                        return "application/vnd.ms-excel";
//                        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    }

                    @Override
                    public String getName() {
                        return conversionService.convert(batch.getCreateTime(), String.class) + "生成的充值卡.xls";
                    }
                });
            }

            @Override
            public String asHtml(Map<String, String> map) {
                StringBuilder sb = new StringBuilder("<p>您好，</p><p>您本次申请的充值卡已下发，请查看附件（仅支持电脑端打开）。</p>");
                sb.append("<table>")
                        .append("<tr><th>卡号</th><th>金额</th>");
                batch.getCardSet().forEach(rc->sb.append("<tr><td>").append(rc.getCode())
                        .append("</td><td>").append(rc.getAmount()).append("</td></tr>"));
                sb.append("</table>");
                return sb.toString();
            }

            @Override
            public String asTitle() {
                return conversionService.convert(batch.getCreateTime(), String.class) + "为" +
                        (batch.getGuideUser().getWechatUser() == null ? batch.getGuideUser().getUsername()
                                : batch.getGuideUser().getWechatUser().getNickname()) + "生成的充值卡的信息";
            }

            @Override
            public String signName() {
                return null;
            }

            @Override
            public String templateName() {
                return null;
            }

            @Override
            public Map<String, ?> templateParameters() {
                return null;
            }
        };

        try {
            if (!environment.acceptsProfiles(ServiceConfig.PROFILE_UNIT_TEST) || environment.acceptsProfiles("emulation")) {
                noticeService.send("me.jiangcai.lib.notice.EmailNoticeSupplier", dist, content);
                noticeService.send(environment.getProperty("com.huotu.notice.supplier"), dist, content);
            }
        } catch (NoticeException ex) {
            if (silence)
                log.warn("", ex);
            else
                throw ex;
        }

    }

    private Set<RechargeCard> newCardSet(RechargeCardBatch batch, int num, Integer amount) {
        Stream.Builder<RechargeCard> builder = Stream.builder();
        while (num-- > 0)
            builder = builder.add(new RechargeCard());

        return builder.build()
                .peek(rechargeCard -> {
                    rechargeCard.setBatch(batch);
                    rechargeCard.setAmount(new BigDecimal(amount));
                    rechargeCard.setCode(User.makeCardNo());
                })
                .map(rechargeCardRepository::save)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeCard verify(String cardNo) {
        RechargeCard rechargeCard = rechargeCardRepository.findOne((root, cq, cb)
                -> cb.equal(root.get(RechargeCard_.code), cardNo));
        if (rechargeCard == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.CARD_NOT_EXIST));
        }
        if (rechargeCard.isUsed()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.CARD_ALREADY_USED));
        }
        return rechargeCard;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @BusinessSafe
    public void useCard(String cardNo, Long userId) {
        RechargeCard rechargeCard = verify(cardNo);
        User user = userRepository.findOne(userId);
        if (user == null || !user.getLogin().isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        rechargeCard.setUsed(true);
        rechargeCard.setUser(user);
        rechargeCard.setUsedTime(LocalDateTime.now());

        if (!user.isActive()) {
            user.setCardNo(cardNo);
        }
        rechargeCardRepository.saveAndFlush(rechargeCard);
        userRepository.save(user);
    }
}
