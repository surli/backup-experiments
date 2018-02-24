package com.ming.shopping.beauty.service.service.impl.settlement;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.repository.settlementSheet.SettlementSheetRepository;
import com.ming.shopping.beauty.service.service.settlement.SettlementSheetService;
import me.jiangcai.lib.sys.service.SystemStringService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lxf
 */
@Service
public class SettlementSheetServiceImpl implements SettlementSheetService {
    @Autowired
    private MainOrderRepository mainOrderRepository;
    @Autowired
    private SystemStringService systemStringService;
    @Autowired
    private SettlementSheetRepository settlementSheetRepository;

    @Override
    public SettlementSheet findSheet(long id) {
        return settlementSheetRepository.findOne(id);
    }

    @Override
    @Transactional
    public SettlementSheet addSheet(Merchant merchant) {
        //默认获取7天前的订单
        LocalDateTime weekAgo = systemStringService.getCustomSystemString(
                "shopping.service.order.how.long", null, true, LocalDateTime.class, LocalDateTime.now().minusDays(7));
        
        List<MainOrder> okList = mainOrderRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.join(MainOrder_.store).join(Store_.merchant).get(Merchant_.id), merchant.getId()),
                        cb.lessThan(root.get(MainOrder_.payTime), weekAgo),
                        cb.equal(root.get(MainOrder_.orderStatus), OrderStatus.success)
                )
        );
        //创建新的结算单
        SettlementSheet settlementSheet = new SettlementSheet();
        Set<MainOrder> mainOrderSet = new HashSet<>(okList);
        for (MainOrder mainOrder : mainOrderSet) {
            mainOrder.setSettlementSheet(settlementSheet);
            //mainOrderRepository.save(mainOrder);
        }
        settlementSheet.setMainOrderSet(mainOrderSet);
        settlementSheet.setMerchant(merchant);
        settlementSheet.setCreateTime(LocalDateTime.now());
        settlementSheet.setSettlementStatus(SettlementStatus.UNSUBMIT);
        return settlementSheetRepository.save(settlementSheet);
    }

    @Override
    @Transactional
    public void submitSheet(SettlementSheet settlementSheet,String comment) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.UNSUBMIT) || one.getSettlementStatus().equals(SettlementStatus.REJECT)){
            one.setSettlementStatus(SettlementStatus.TO_AUDIT);
            if(comment != null){
                one.setComment(comment);
            }
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    @Transactional
    public void rejectSheet(SettlementSheet settlementSheet,String comment) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.TO_AUDIT)){
            one.setSettlementStatus(SettlementStatus.REJECT);
            //必须要有备注
            if(!StringUtils.isBlank(comment)){
                one.setComment(comment);
            }else{
                throw new ApiResultException(ApiResult.withError(ResultCodeEnum.REJECT_NOT_COMMENT));
            }
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    @Transactional
    public void revokeSheet(SettlementSheet settlementSheet) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.UNSUBMIT)){
            one.setSettlementStatus(SettlementStatus.REVOKE);
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    @Transactional
    public void approvalSheet(SettlementSheet settlementSheet, String comment) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.TO_AUDIT)){
            one.setSettlementStatus(SettlementStatus.APPROVAL);
            if(comment != null){
                one.setComment(comment);
            }
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    @Transactional
    public void alreadyPaid(SettlementSheet settlementSheet, BigDecimal amount) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.APPROVAL)){
            one.setSettlementStatus(SettlementStatus.ALREADY_PAID);
            one.setTransferTime(LocalDateTime.now());
            one.setActualAmount(amount);
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    @Transactional
    public void completeSheet(SettlementSheet settlementSheet) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.ALREADY_PAID)){
            one.setSettlementStatus(SettlementStatus.COMPLETE);
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }

    @Override
    public void putEnabled(SettlementSheet settlementSheet,boolean delete) {
        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        if(one.getSettlementStatus().equals(SettlementStatus.REJECT)
                || one.getSettlementStatus().equals(SettlementStatus.REVOKE)){
            one.setDetect(delete);
            settlementSheetRepository.save(one);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.SHEET_STATUS_ERROR));
        }
    }
}
