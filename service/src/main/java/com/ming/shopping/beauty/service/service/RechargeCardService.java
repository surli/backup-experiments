package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author helloztt
 */
public interface RechargeCardService {

    /**
     * @param operator     操作者；可以为null
     * @param guideId      发展者
     * @param emailAddress 发展者的email地址；它可以接收到卡密信息
     * @param num          数量
     * @param silence      保持安静，即便发送异常也别出声。
     * @return 批次
     */
    @Transactional
    RechargeCardBatch newBatch(Login operator, long guideId, String emailAddress, int num, boolean silence) throws ClassNotFoundException;

    RechargeCardBatch findBatch(long id);

    /**
     * 生成报表
     *
     * @param batch  批次
     * @param output 输出目标
     * @throws IOException
     */
    void batchReport(RechargeCardBatch batch, OutputStream output) throws IOException;

    /**
     * 发送卡密信息给引导者
     *
     * @param batch 批次
     * @param silence
     * @throws ClassNotFoundException
     */
    void sendToUser(RechargeCardBatch batch, boolean silence) throws ClassNotFoundException;

    /**
     * 校验卡密
     *
     * @param cardNo
     * @return
     */
    RechargeCard verify(String cardNo);

    /**
     * 使用卡密，加上业务锁
     * 实现中应该有这样几个步骤：
     * 1.校验，如果错误就抛出异常
     * 2.设置充值卡已被谁使用
     * 3.给这个用户激活，增加金额
     *
     * @param cardNo 卡密
     * @param userId 充值的用户
     */
    void useCard(String cardNo, Long userId);
}
