package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet_;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.SheetReviewBody;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.settlement.SettlementSheetService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.utils.MapUtils;
import me.jiangcai.lib.sys.SystemStringConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@RequestMapping("/settlementSheet")
@Controller
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@PreAuthorize("hasAnyRole('ROOT','" + SystemStringConfig.MANAGER_ROLE + "','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
public class ManageSettlementSheetController extends AbstractCrudController<SettlementSheet, Long, SettlementSheet> {

    @Autowired
    private SettlementSheetService settlementSheetService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ConversionService conversionService;

    /**
     * 结算单列表
     *
     * @param request 请求
     * @return
     */
    @Override
    public RowDefinition<SettlementSheet> list(HttpServletRequest request) {
        Map<String, Object> queryData = MapUtils.changeIt(request.getParameterMap());
        return new RowDefinition<SettlementSheet>() {
            @Override
            public CriteriaQuery<SettlementSheet> dataGroup(CriteriaBuilder cb, CriteriaQuery<SettlementSheet> query, Root<SettlementSheet> root) {
                return query.groupBy(root);
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<SettlementSheet> root) {
                return listOrder(criteriaBuilder, root);
            }

            @Override
            public Class<SettlementSheet> entityClass() {
                return SettlementSheet.class;
            }

            @Override
            public List<FieldDefinition<SettlementSheet>> fields() {
                return listFields();
            }

            @Override
            public Specification<SettlementSheet> specification() {
                return listSpecification(queryData);
            }
        };
    }

    /**
     * 产生一个结算单,由商户发起,统计系统规定的时间周期内的订单的MainOrder的信息.
     *
     * @param merchantId 商户id
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/{merchantId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity productSheet(@PathVariable("merchantId") Long merchantId) throws URISyntaxException {
        Merchant merchant = merchantService.findMerchant(merchantId);
        SettlementSheet settlementSheet = settlementSheetService.addSheet(merchant);
        return ResponseEntity
                .created(new URI("/settlementSheet/" + settlementSheet.getId() + "/store"))
                .build();
    }

    @PutMapping("/{id}/statusManage")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putEnabledManage(@PathVariable("id") Long id, @RequestBody SheetReviewBody putData) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        if (putData != null) {
            if (putData.getStatus() == null) {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "status"), null));
            }
            SettlementSheet sheet = settlementSheetService.findSheet(id);
            switch (putData.getStatus()) {
                case "APPROVAL":
                    //同意申请
                    if (StringUtils.isBlank(putData.getComment()))
                        throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                                , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "comment"), null));
                    settlementSheetService.approvalSheet(sheet, putData.getComment());
                    break;
                case "REJECT":
                    //打回申请
                    if (StringUtils.isBlank(putData.getComment()))
                        throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                                , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "comment"), null));
                    settlementSheetService.rejectSheet(sheet, putData.getComment());
                    break;
                case "ALREADY_PAID":
                    //已经支付
                    if (putData.getAmount() == null) {
                        throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                                , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "amount"), null));
                    }
                    settlementSheetService.alreadyPaid(sheet, putData.getAmount());
                    break;
                default:
                    throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "status"), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "putData"), null));
        }
    }

    @PutMapping("/{id}/statusMerchant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_MERCHANT_SETTLEMENT + "')")
    public void putEnabledMerchant(@PathVariable("id") Long id, @RequestBody(required = false) SheetReviewBody putData) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        if (putData != null) {
            if (putData.getStatus() == null)
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "status"), null));
            SettlementSheet sheet = settlementSheetService.findSheet(id);
            switch (putData.getStatus()) {
                case "TO_AUDIT":
                    //提交审核
                    if (StringUtils.isBlank(putData.getComment()))
                        throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                                , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "comment"), null));
                    settlementSheetService.submitSheet(sheet, putData.getComment());
                    break;
                case "COMPLETE":
                    //已收款
                    settlementSheetService.completeSheet(sheet);
                    break;
                case "REVOKE":
                    //撤销
                    settlementSheetService.revokeSheet(sheet);
                    break;
                default:
                    throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "status"), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "putData"), null));
        }
    }


    /**
     * 结算单是否在列表中展示出来
     *
     * @param id
     */
    @PutMapping("{id}/delete")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putEnabled(@PathVariable("id") Long id, @RequestBody Boolean delete) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        if (delete == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "delete"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.putEnabled(sheet, delete);
    }


    /**
     * 结算单中根据门店划分的明细
     *
     * @param id 结算单id
     * @return
     */
    @GetMapping("/{id}/store")
    public RowDefinition<MainOrder> getDetailForStore(@PathVariable("id") Long id) {
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        if (sheet.getSettlementStatus().equals(SettlementStatus.REJECT) || sheet.getSettlementStatus().equals(SettlementStatus.REVOKE)) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        return new RowDefinition<MainOrder>() {
            @Override
            public Class<MainOrder> entityClass() {
                return MainOrder.class;
            }

            @Override
            public CriteriaQuery<MainOrder> dataGroup(CriteriaBuilder cb, CriteriaQuery<MainOrder> query, Root<MainOrder> root) {
                return query.groupBy(
                        root.get(MainOrder_.store)
                );
            }

            @Override
            public List<FieldDefinition<MainOrder>> fields() {
                return Arrays.asList(
                        //结算金额
                        FieldBuilder.asName(MainOrder.class, "settlementAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.settlementAmount)))
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "actualAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.finalAmount)))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "count")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.count(mainOrderRoot))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "storeName")
                                .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.store))
                                .build()
                );
            }

            @Override
            public Specification<MainOrder> specification() {
                return (root, query, cb) -> cb.and(
                        cb.lessThan(root.get(MainOrder_.payTime), LocalDateTime.now().minusDays(7))
                        , cb.equal(root.join(MainOrder_.settlementSheet).get(SettlementSheet_.id), id));
            }
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<SettlementSheet> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(SettlementSheet_.createTime))
        );
    }

    @Override
    protected List<FieldDefinition<SettlementSheet>> listFields() {

        return Arrays.asList(
                FieldBuilder.asName(SettlementSheet.class, "id")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "merchantName")
                        .addSelect(settlementSheetRoot -> settlementSheetRoot.join(SettlementSheet_.merchant).get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "actualAmount")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "createTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "settlementAmount")
                        .addBiSelect((settlementSheetRoot, criteriaBuilder) -> criteriaBuilder.sum(settlementSheetRoot.get("mainOrderSet").get("settlementAmount")))
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "status")
                        .addSelect(settlementSheetRoot -> settlementSheetRoot.get(SettlementSheet_.settlementStatus))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "comment")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "transferTime")
                        .build()
        );
    }

    @Override
    protected Specification<SettlementSheet> listSpecification(Map<String, Object> queryData) {
        return (root, query, cb) -> {
            List<Predicate> queryList = new ArrayList<>();
            if (queryData.get("id") != null) {
                queryList.add(cb.equal(root.get(SettlementSheet_.id), Long.valueOf(queryData.get("id").toString())));
            }
            if (queryData.get("status") != null) {
                queryList.add(cb.equal(root.get(SettlementSheet_.settlementStatus),
                        SettlementStatus.valueOf(queryData.get("status").toString())));
            }
            return cb.and(queryList.toArray(new Predicate[queryList.size()]));
        };
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

}
