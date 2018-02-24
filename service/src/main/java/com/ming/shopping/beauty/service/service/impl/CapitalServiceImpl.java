package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.log.RechargeLog;
import com.ming.shopping.beauty.service.entity.log.RechargeType;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.RechargeLogRepository;
import com.ming.shopping.beauty.service.service.CapitalService;
import com.ming.shopping.beauty.service.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lxf
 */
@Service
public class CapitalServiceImpl implements CapitalService {

    @Autowired
    private RechargeLogRepository rechargeLogRepository;
    @Autowired
    private LoginService loginService;

    @Override
    public void manualRecharge(Login manage, User user, BigDecimal amount) {
        //TODO 充值记录中是否应该添加一个操作员?
        if (user != null) {
            //记录充值日志
            RechargeLog rechargeLog = new RechargeLog();
            rechargeLog.setAmount(amount);
            rechargeLog.setUser(user);
            rechargeLog.setCreateTime(LocalDateTime.now());
            rechargeLog.setRechargeType(RechargeType.MANUAL);
            //保存记录
            rechargeLogRepository.save(rechargeLog);
        } else {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.USER_NOT_EXIST));
        }
    }

    @Override
    @Transactional
    public BigDecimal deduction(Login manager, User user, BigDecimal amount) {
        if(amount == null){
            //清空余额
            BigDecimal balance = loginService.findBalance(user.getId());

            //记录扣款日志
            RechargeLog rechargeLog = new RechargeLog();
            //取反
            rechargeLog.setAmount(balance.negate());
            rechargeLog.setUser(user);
            rechargeLog.setCreateTime(LocalDateTime.now());
            rechargeLog.setRechargeType(RechargeType.DEDUCTION);
            //保存记录
            rechargeLogRepository.save(rechargeLog);
            return balance;
        }else{
            //记录扣款日志
            RechargeLog rechargeLog = new RechargeLog();
            rechargeLog.setAmount(amount.negate());
            rechargeLog.setUser(user);
            rechargeLog.setCreateTime(LocalDateTime.now());
            rechargeLog.setRechargeType(RechargeType.DEDUCTION);
            //保存记录
            rechargeLogRepository.save(rechargeLog);
            return amount;
        }
    }
}
