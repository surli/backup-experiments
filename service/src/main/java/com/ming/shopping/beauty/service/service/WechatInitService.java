package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.config.ServiceConfig;
import me.jiangcai.lib.sys.SystemStringUpdatedEvent;
import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.wx.model.Menu;
import me.jiangcai.wx.model.MenuType;
import me.jiangcai.wx.model.PublicAccount;
import me.jiangcai.wx.model.media.NewsMediaItem;
import me.jiangcai.wx.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author CJ
 */
@Service
public class WechatInitService {

    public static final String MARKET_FIRST_MENUS = "market.first.menus";
    private static final Log log = LogFactory.getLog(WechatInitService.class);
    @Autowired
    private SystemService systemService;
    @Autowired
    private PublicAccount publicAccount;
    @Autowired
    private Environment environment;
    @Autowired
    private SystemStringService systemStringService;

    @EventListener(SystemStringUpdatedEvent.class)
    public void updated(SystemStringUpdatedEvent e) {
        if (MARKET_FIRST_MENUS.equals(e.getKey())) {
            init();
        }
    }

    @PostConstruct
    public void init() {
        //菜单
        if (environment.acceptsProfiles(ServiceConfig.PROFILE_UNIT_TEST)) {
            log.info("单元测试时没有必要更新公众号菜单");
            return;
        }
        try {

            // 一号菜单的名字
            String name = "关于我们";
            //  获取其他菜单，一般就是 a:b|
            String menuPattern = systemStringService.getCustomSystemString(MARKET_FIRST_MENUS
                    , "market.first.menus.comment"
                    , false, String.class, "介绍:名顺|更多:名顺");

            log.debug("视图使用的菜单:" + menuPattern);
            Map<String, String> menuMedias = Stream.of(menuPattern.split("\\|"))
                    .map(s -> s.split(":"))
                    .collect(Collectors.toMap(strings -> strings[0], strings -> strings[1]));
            List<String> menus = Stream.of(menuPattern.split("\\|"))
                    .map(s -> s.split(":")[0])
                    .collect(Collectors.toList());

            Map<String, String> menuMediaIds = new HashMap<>();

            final Protocol protocol = Protocol.forAccount(publicAccount);
            // urls
            int page = 0;
            while (true) {
                final Page<NewsMediaItem> items = protocol.listNewsMedia(new PageRequest(page++, 20));
                items
                        .forEach(newsMediaItem -> {
                            // 找下存在 menuMedias 但并不存在在 menuUrls 中的
                            newsMediaItem.getNews()
                                    .stream()
                                    .filter(newsArticle -> {
                                        String title = newsArticle.getTitle();
                                        log.debug("查找到微信素材:" + title);
                                        return menuMedias.entrySet()
                                                .stream()
                                                .filter(stringStringEntry -> !menuMediaIds.keySet().contains(stringStringEntry.getKey()))
                                                .map(Map.Entry::getValue)
                                                .anyMatch(input -> input.equalsIgnoreCase(title));
                                    }).forEach(newsArticle
                                    -> menuMedias.entrySet()
                                    .stream()
                                    .filter(stringStringEntry -> newsArticle.getTitle().equalsIgnoreCase(stringStringEntry.getValue()))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .ifPresent(menuName -> menuMediaIds.put(menuName, newsMediaItem.getId())));
                        });
                if (items.isLast())
                    break;
            }

            if (log.isDebugEnabled()) {
                log.debug("最终确定菜单：");
                menuMediaIds.forEach((key, value) -> log.debug(key + ":" + value));
            }

            final Menu menu1 = createMenu(name, menuMediaIds.entrySet().stream()
                    .sorted(Comparator.comparingInt(o -> menus.indexOf(o.getKey())))
                    .map(stringStringEntry -> {
                        Menu menu = new Menu();
                        menu.setType(MenuType.media_id);
                        menu.setName(stringStringEntry.getKey());
                        menu.setData(stringStringEntry.getValue());
                        return menu;
                    })
                    .toArray(Menu[]::new)
            );

//            final Menu menu3 = createMenu("个人中心"
//                    , createMenu("帮助", systemService.toUrl(SystemService.helpCenterURi))
//                    , createMenu("分享", systemService.toUrl(SystemService.wechatShareUri))
//                    , createMenu("我的", systemService.toUrl(SystemService.wechatMyURi)));

            final Menu menu3 = createMenu("锋尚推荐", systemService.toMobileItemUrl());
            final Menu menu2 = createMenu("会员专区",
                    createMenu("个人中心", systemService.toMobileHomeUrl())
                    , createMenu("会员卡", systemService.toMobileVipUrl())
            );


            try {
                protocol.createMenu(
                        new Menu[]
                                {
                                        menu3, menu2, menu1,
                                }
                );
                log.info("updated the menus");
            } catch (Throwable ex) {
                log.warn("Error on Update Wechat Menus", ex);
            }

        } catch (Exception ex) {
            log.warn("更新微信菜单失败", ex);
        }


    }

    private Menu createMenu(String name, Menu... menus) {
        // 把 null 移除掉
        Menu menu = new Menu();
        menu.setType(MenuType.parent);
        menu.setName(name);
        menu.setSubs(Stream.of(menus)
                .filter(Objects::nonNull)
                .toArray(Menu[]::new)
        );
        return menu;
    }

    private Menu createMenu(String name, String url) {
        Menu menu = new Menu();
        menu.setType(MenuType.view);
        menu.setName(name);
        menu.setData(url);
        return menu;
    }


}
