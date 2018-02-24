package com.ming.shopping.beauty.service.service;

import org.eclipse.persistence.internal.jpa.querydef.OrderImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author helloztt
 */
@Service
public class EntityManagerBaseService {

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * 分页查询接口
     *
     * @param resultType  结果类型
     * @param toSelect    查询目标构造方式
     * @param entityClass 目标实体
     * @param condition   查询条件
     * @param pageable    分页
     * @param <R>         结果类型
     * @param <T>         实体类型
     * @return ..
     */
    public  <R, T> Page<R> pageQuery(Class<R> resultType, BiFunction<CriteriaBuilder, Root<T>, Selection<R>> toSelect
            , Class<T> entityClass, BiFunction<Root<T>, CriteriaBuilder, Predicate[]> condition, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultType);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery = criteriaQuery.where(condition.apply(root, criteriaBuilder));
        if (pageable.getSort() != null) {
            List<Order> orderList = new ArrayList<>();
            Iterator<Sort.Order> orderIterable = pageable.getSort().iterator();
            while (orderIterable.hasNext()) {
                Sort.Order sortOrder = orderIterable.next();
                Order order = new OrderImpl(root.get(sortOrder.getProperty()), sortOrder.getDirection().equals(Sort.Direction.ASC) ? true : false);
                orderList.add(order);
            }
            criteriaQuery = criteriaQuery.orderBy(orderList);
        }

        CriteriaQuery<Long> countCriteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> countRoot = countCriteriaQuery.from(entityClass);
        countCriteriaQuery = countCriteriaQuery.where(condition.apply(countRoot, criteriaBuilder));

        // 现在先查询总量
        countCriteriaQuery = countCriteriaQuery.select(criteriaBuilder.count(countRoot));
        long total = entityManager.createQuery(countCriteriaQuery)
                .getSingleResult();

        // 再分页查询
        criteriaQuery = criteriaQuery.select(toSelect.apply(criteriaBuilder, root));
        List<R> list = entityManager.createQuery(criteriaQuery)
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(list, pageable, total);
    }

    public <R, T> R findOne(Class<R> resultType, BiFunction<CriteriaBuilder, Root<T>, Selection<R>> toSelect
            , Class<T> entityClass, BiFunction<Root<T>, CriteriaBuilder, Predicate[]> condition) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultType);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery = criteriaQuery.where(condition.apply(root, criteriaBuilder));
        criteriaQuery = criteriaQuery.select(toSelect.apply(criteriaBuilder, root));
        R result = entityManager.createQuery(criteriaQuery)
                .getSingleResult();
        return result;
    }


}
