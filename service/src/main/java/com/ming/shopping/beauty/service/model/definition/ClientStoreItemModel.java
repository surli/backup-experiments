package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.utils.Utils;
import lombok.Getter;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.lib.resource.service.ResourceService;

import javax.persistence.criteria.Join;
import java.util.Arrays;
import java.util.List;

/**
 * 规格上跟API中的ItemProperties保持一致。
 * 虽然我们查询的是门店item,但是必须以item进行group；确保用户看到的只有一个项目
 *
 * @author lxf
 */
@Getter
public class ClientStoreItemModel implements DefinitionModel<StoreItem> {

    private final List<FieldDefinition<StoreItem>> definitions;
    private Join<Item, Merchant> merchantJoin;
    private Join<StoreItem, Item> itemJoin;

    /**
     * @param resourceService 资源服务
     * @param singleStore     单店模式，在这个模式下 就没必要聚合了，因为我们将直接显示这个门店所有的门店项目。
     */
    public ClientStoreItemModel(ResourceService resourceService, boolean singleStore) {
        super();
        definitions = Arrays.asList(
                FieldBuilder.asName(StoreItem.class, "itemId")
                        .addSelect(root -> {
                            itemJoin = root.join(StoreItem_.item);
                            merchantJoin = itemJoin.join(Item_.merchant);
                            return itemJoin.get(Item_.id);
                        })
                        .build()
                , FieldBuilder.asName(StoreItem.class, "thumbnail")
                        .addSelect(root -> itemJoin.get(Item_.mainImagePath))
                        .addFormat(Utils.formatResourcePathToURL(resourceService))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "title")
                        .addSelect(root -> itemJoin.get(Item_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "tel")
                        .addSelect(itemRoot -> merchantJoin.get(Merchant_.telephone))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "address")
                        .addSelect(root -> merchantJoin.get(Merchant_.address))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(StoreItem.class, "type")
                        .addSelect(storeItemRoot -> itemJoin.get(Item_.itemType))
                        .build()
                //TODO 距离还有问题
                , FieldBuilder.asName(StoreItem.class, "distance")
                        .addBiSelect((storeItemRoot, criteriaBuilder) -> criteriaBuilder.literal(0))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "vipPrice")
                        .addSelect(root -> itemJoin.get(Item_.salesPrice))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "originalPrice")
                        .addSelect(root -> itemJoin.get(Item_.price))
                        .build()
        );
    }
}
