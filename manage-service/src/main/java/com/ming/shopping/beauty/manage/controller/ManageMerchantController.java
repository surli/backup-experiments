package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.modal.MerchantCreation;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.MerchantService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/merchant")
public class ManageMerchantController extends AbstractCrudController<Merchant, Long, MerchantCreation> {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private ConversionService conversionService;

    /**
     * 商户列表
     *
     * @param request
     * @return
     */
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT')")
    @Override
    public RowDefinition<Merchant> list(HttpServletRequest request) {
        return super.list(request);
    }


    /**
     * 新增商户
     *
     * @param postData 商户信息
     * @param request  其他信息
     * @return
     * @throws URISyntaxException
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROOT')")
    @Override
    public ResponseEntity addOne(@RequestBody MerchantCreation postData, WebRequest request) throws URISyntaxException {
        final String param = "loginId";
        if (postData.getLoginId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        if (StringUtils.isEmpty(postData.getName()) || StringUtils.isEmpty(postData.getTelephone())
                || StringUtils.isEmpty(postData.getContact())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        Merchant merchant = merchantService.addMerchant(postData.getLoginId(), postData);
        return ResponseEntity
                .created(new URI("/merchant/" + merchant.getId()))
                .build();
    }

    /**
     * 编辑商户信息.
     *
     * @param postData
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @Transactional(rollbackFor = RuntimeException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMerchant(@AuthenticationPrincipal Login login, @RequestBody Merchant postData) {
        if (postData.getId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), postData), null));
        }
        if (login.getLevelSet().contains(ManageLevel.root)) {
            //说明是超管 可以修改所有的对象
            updating(postData);
        } else {
            //仅仅是商户自身
            if (login.getId().equals(postData.getId())) {
                updating(postData);
            } else
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.LOGIN_NOT_MANAGE.getMessage(), login), null));
        }
    }

    private void updating(Merchant postData) {
        Merchant merchant = merchantRepository.findOne(postData.getId());
        if (postData.getName() != null) {
            merchant.setName(postData.getName());
        }
        if (postData.getTelephone() != null) {
            merchant.setTelephone(postData.getTelephone());
        }
        if (postData.getContact() != null) {
            merchant.setContact(postData.getContact());
        }
        merchantRepository.save(merchant);
    }

    @Override
    protected Object describeEntity(Merchant origin) {
        return RowService.drawEntityToRow(origin, Arrays.asList(
                FieldBuilder.asName(Merchant.class, "id")
                        .build()
                , FieldBuilder.asName(Merchant.class, "name")
                        .build()
                , FieldBuilder.asName(Merchant.class, "telephone")
                        .build()
                , FieldBuilder.asName(Merchant.class, "contact")
                        .build()
        ), null);
    }

    /**
     * 商户详情
     *
     * @param aLong
     * @return
     */
    @Override
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseBody
    @GetMapping({"/{id}"})
    public Object getOne(@PathVariable("id") Long aLong) {
        return super.getOne(aLong);
    }

    /**
     * 启用/禁用 商户
     *
     * @param loginId
     * @param enable
     */
    @PutMapping("/{merchantId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("merchantId") long loginId, @RequestBody Boolean enable) {
        if (enable != null) {
            merchantService.freezeOrEnable(loginId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 商户操作员列表
     *
     * @param merchantId
     * @return
     */
    @GetMapping("/{merchantId}/manage")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition listForManage(@PathVariable long merchantId) throws IOException {
        return new RowDefinition<Merchant>() {
            @Override
            public Class<Merchant> entityClass() {
                return Merchant.class;
            }

            @Override
            public List<FieldDefinition<Merchant>> fields() {
                return listFieldsForManage();
            }

            @Override
            public Specification<Merchant> specification() {
                return listSpecificationForManage(merchantId);
            }
        };
    }

    /**
     * 商户操作员详情
     *
     * @param manageId
     * @return
     */
    @GetMapping("/{merchantId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public String manageDetail(@PathVariable long manageId) {
        return "redirect:/login/" + manageId;
    }

    /**
     * 新增商户操作员
     *
     * @param merchantId
     * @param manageId
     */
    @PostMapping("/{merchantId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMerchantManage(@PathVariable long merchantId, @PathVariable long manageId
            , @RequestBody Map<String, Object> postData) {
        String param = "level";
        if (!postData.containsKey(param)) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.NEED_LEVEL));
        }
        Object level = postData.get(param);
        Collection<?> collection = (Collection<?>) level;
        Set<ManageLevel> manageLevelSet = collection.stream()
                .map(Object::toString)
                .map(ManageLevel::valueOf)
                .collect(Collectors.toSet());

        if (!Login.merchantLevel.containsAll(manageLevelSet))
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , "只允许添加商户级别的管理员", null));

        //找这个商户
        Merchant merchant = merchantService.findMerchant(merchantId);
        merchantService.addMerchantManager(merchant, manageId, manageLevelSet);
    }

    private ManageLevel getManageLevel(String level) {
        ManageLevel manageLevel = conversionService.convert(level, ManageLevel.class);
        if (manageLevel == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LEVEL_NOT_EXIST));
        }
        return manageLevel;
    }

    /**
     * 启用/禁用商户操作员
     *
     * @param merchantId
     * @param manageId
     * @param enable
     */
    @PutMapping("/{merchantId}/manage/{manageId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableMerchantManage(@PathVariable long merchantId, @PathVariable long manageId, @RequestBody Boolean enable) {
        if (enable != null) {
            merchantService.freezeOrEnable(manageId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    @Override
    protected List<FieldDefinition<Merchant>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Merchant.class, "id")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.id))
                        .build()
                , FieldBuilder.asName(Merchant.class, "username")
                        .addSelect(merchantRoot -> merchantRoot.join(Merchant_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Merchant.class, "name")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(Merchant.class, "contact")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.contact))
                        .build()
                , FieldBuilder.asName(Merchant.class, "telephone")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.telephone))
                        .build()
                , FieldBuilder.asName(Merchant.class, "address")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.address))
                        .addFormat((data, type) -> data != null ? data.toString() : null)
                        .build()
                , FieldBuilder.asName(Merchant.class, "enabled")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.enabled))
                        .build()
                , FieldBuilder.asName(Merchant.class, "createTime")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.createTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
        );
    }

    @Override
    protected Specification<Merchant> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("username") != null) {
                if(StringUtils.isNotBlank(queryData.get("username").toString())){
                    conditionList.add(cb.like(root.join(Merchant_.login).get(Login_.loginName), "%" + queryData.get("username") + "%"));
                }
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

    protected List<FieldDefinition<Merchant>> listFieldsForManage() {
        return Arrays.asList(
                new ManageField() {

                    @Override
                    public Selection<?> select(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Root<Merchant> root) {
                        return root;
                    }

                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.getId();
                    }

                    @Override
                    public Expression<?> order(Root<Merchant> root, CriteriaBuilder criteriaBuilder) {
                        return root.get(Merchant_.id);
                    }

                    @Override
                    public String name() {
                        return "id";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.getLogin().getLoginName();
                    }

                    @Override
                    public String name() {
                        return "username";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.isEnabled();
                    }

                    @Override
                    public String name() {
                        return "enabled";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        Set<ManageLevel> levelSet = manage.getLogin().getLevelSet();
                        if (CollectionUtils.isEmpty(levelSet)) {
                            return null;
                        }
                        return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                    }

                    @Override
                    public String name() {
                        return "level";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return conversionService.convert(manage.getCreateTime(), String.class);
                    }

                    @Override
                    public String name() {
                        return "createTime";
                    }
                }
        );
    }

    protected Specification<Merchant> listSpecificationForManage(long merchantId) {
        return (root, cq, cb) ->
                cb.equal(root.get(Merchant_.id), merchantId);
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Merchant> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Merchant_.id))
        );
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Merchant> getDetail(Long aLong) {
        return null;
    }

    private abstract class ManageField implements FieldDefinition<Merchant> {
        @Override
        public Selection<?> select(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Root<Merchant> root) {
            return null;
        }

        @Override
        public Object export(Object origin, MediaType mediaType, Function<List, ?> exportMe) {
            return export((Merchant) origin, exportMe);
        }

        protected abstract Object export(Merchant manage, Function<List, ?> exportMe);

        @Override
        public Expression<?> order(Root<Merchant> root, CriteriaBuilder criteriaBuilder) {
            return null;
        }
    }
}
