package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.ManagerModel;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.Predicate;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 超级管理员
 *
 * @author helloztt
 */
@Controller
@RequestMapping("/manage")
@PreAuthorize("hasAnyRole('ROOT')")
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
public class ManageController extends AbstractCrudController<Login, Long, Login> {
    @Autowired
    private LoginService loginService;
    @Autowired
    private ConversionService conversionService;

    @PreAuthorize("denyAll()")
    @Override
    public ResponseEntity addOne(Login postData, WebRequest otherData) throws URISyntaxException {
        return null;
    }

    @PreAuthorize("denyAll()")
    @Override
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @PutMapping("/{id}/levelSet")
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateLevelSet(@PathVariable long id, @RequestBody Set<ManageLevel> target) {
        // 必须在范围内 切不可为root
        if (!Login.rootLevel.containsAll(target))
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , "只允许添加平台级别的管理员", null));
        if (target.contains(ManageLevel.root))
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , "不允许添加root", null));
        //noinspection SimplifyStreamApiCallChains
        loginService.setManageLevel(id, target.stream().toArray(ManageLevel[]::new));
    }


    @Override
    protected List<FieldDefinition<Login>> listFields() {
        return new ManagerModel(conversionService).getDefinitions();
    }

    @Override
    protected Specification<Login> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            conditionList.add(Login.getManageableExpr(root));
            if (queryData.get("username") != null) {
                if(StringUtils.isNotBlank(queryData.get("username").toString())){
                    conditionList.add(cb.like(root.get(Login_.loginName), "%" + queryData.get("username") + "%"));
                }
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

}
