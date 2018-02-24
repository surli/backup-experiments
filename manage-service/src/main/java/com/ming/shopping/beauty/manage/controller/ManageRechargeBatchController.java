package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.modal.RechargeCardBatchCreation;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
@PreAuthorize("hasAnyRole('ROOT')")
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@RequestMapping("/rechargeBatch")
public class ManageRechargeBatchController
        extends AbstractCrudController<RechargeCardBatch, Long, RechargeCardBatchCreation> {

    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private ConversionService conversionService;

    @PutMapping("/{id}/emailSending")
    @Transactional(readOnly = true)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmail(@PathVariable("id") long id) throws ClassNotFoundException {
        rechargeCardService.sendToUser(rechargeCardService.findBatch(id), false);
    }

    @Override
    protected RechargeCardBatch preparePersist(RechargeCardBatchCreation data, WebRequest request) {
        Login login = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return rechargeCardService.newBatch(login, data.getGuideId(), data.getEmailAddress(), data.getNumber(), false);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected CriteriaQuery<RechargeCardBatch> listGroup(CriteriaBuilder cb, CriteriaQuery<RechargeCardBatch> query, Root<RechargeCardBatch> root) {
        return query.groupBy(root);
    }

    @Override
    protected List<FieldDefinition<RechargeCardBatch>> listFields() {
        return Arrays.asList(
                Fields.asBasic("id")
                , Fields.asBasic("emailAddress")
                , FieldBuilder.asName(RechargeCardBatch.class, "createTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(RechargeCardBatch.class, "total")
                        .addBiSelect((rechargeCardBatchRoot, criteriaBuilder)
                                -> criteriaBuilder.sum(rechargeCardBatchRoot.get("cardSet").get("amount")))
                        .build()
                , FieldBuilder.asName(RechargeCardBatch.class, "guide")
                        .addSelect(rechargeCardBatchRoot -> rechargeCardBatchRoot.get(RechargeCardBatch_.guideUser))
                        .addFormat((guideUser, type) -> {
                            Login guide = (Login) guideUser;
                            return guide.getHumanReadName();
                        })
                        .build()
                , FieldBuilder.asName(RechargeCardBatch.class, "manager")
                        .addSelect(rechargeCardBatchRoot -> rechargeCardBatchRoot.join(RechargeCardBatch_.manager, JoinType.LEFT))
                        .addFormat((guideUser, type) -> {
                            if (guideUser == null)
                                return null;
                            Login guide = (Login) guideUser;
                            return guide.getHumanReadName();
                        })
                        .build()
        );
    }

    @Override
    protected Specification<RechargeCardBatch> listSpecification(Map<String, Object> map) {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    public Object getOne(Long aLong) {
        return super.getOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<RechargeCardBatch> getDetail(Long aLong) {
        return super.getDetail(aLong);
    }
}
