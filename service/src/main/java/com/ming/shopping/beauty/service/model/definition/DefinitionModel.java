package com.ming.shopping.beauty.service.model.definition;

import me.jiangcai.crud.row.FieldDefinition;

import java.util.List;

/**
 * 定义出来的模型
 *
 * @author CJ
 */
public interface DefinitionModel<T> {
    List<FieldDefinition<T>> getDefinitions();
}
