package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.InitService;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.service.StoreService;
import com.ming.shopping.beauty.service.service.impl.support.AbstractLoginService;
import me.jiangcai.jpa.entity.support.Address;
import me.jiangcai.lib.resource.service.ResourceService;
import me.jiangcai.wx.model.Gender;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * @author CJ
 */
@Service
public class StagingServiceImpl extends AbstractLoginService implements StagingService {

    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private RepresentService representService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private StoreItemService storeItemService;
    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private LoginService loginService;

    private Login createDemoUser() {
        return createDemoUser(null);
    }

    private Login createDemoUser(BigDecimal balance) {
        Login login = loginService.newUser(RandomStringUtils.randomNumeric(11)
                , RandomStringUtils.randomAlphabetic(1)
                , Gender.values()[new Random().nextInt(Gender.values().length)], null, null, null
                , null);
        login.getUser().setCurrentAmount(balance != null ? balance : BigDecimal.ZERO);
        userRepository.save(login.getUser());
        return login;
    }


    @Override
    public void initStagingEnv() throws IOException, ClassNotFoundException {
        int count = 20;
        if (loginRepository.count() <= count) {
            // 在staging 中 建立足够多的测试帐号
            for (int i = 0; i < count; i++)
                createDemoUser(new BigDecimal("4999.99"));
        }
        if (itemRepository.count() < 2) {
            generateStagingData();
        }
        if (rechargeCardRepository.count() < count) {
            //在 staging 中建立足够多的充值卡
            registerStagingData();
        }
    }

    @Autowired
    private Environment environment;

    @Override
    @PostConstruct
    public void init() throws IOException, ClassNotFoundException {
        if (environment.acceptsProfiles("staging")) {
            initStagingEnv();
        }
    }

    @Override
    public Object[] generateStagingData() throws IOException {
        Login merchantLogin = createDemoUser();
        Merchant merchant = new Merchant();
        merchant.setName("staging商户");
        merchant.setContact("staging商户联系人");
        merchant.setTelephone(merchantLogin.getLoginName());
        merchant.setAddress(address("staging地址"));
//        merchant.
        merchant = merchantService.addMerchant(merchantLogin.getId(), merchant);

        Login storeLogin = createDemoUser();
        Store store = storeService.addStore(storeLogin.getId(), merchant.getId(), "staging门店", storeLogin.getLoginName()
                , "staging商户门店", address("staging门店地址"));
        // 门店代表
        Login representLogin = createDemoUser();
        Represent represent = representService.addRepresent(representLogin.getId(), store.getId());
        // 项目
        Item[] items = new Item[]{
                createItem(merchant, "ok", new BigDecimal("1000"), AuditStatus.AUDIT_PASS, true, store, true)
                , createItem(merchant, "门店项目!enabled", new BigDecimal("500"), AuditStatus.AUDIT_PASS, true, store, false)
                , createItem(merchant, "项目!enabled", new BigDecimal("600"), AuditStatus.AUDIT_PASS, false, store, true)
                , createItem(merchant, "审核下架了", new BigDecimal("700"), AuditStatus.TO_AUDIT, true, store, true)
                , createItem(merchant, "没有门店", new BigDecimal("800"), AuditStatus.AUDIT_PASS, true, null, true)
        };
        return new Object[]{
                merchant, store, represent, items
        };
    }

    @Override
    public Object[] registerStagingData() throws IOException, ClassNotFoundException {
        // 默认直接给蒋才，不过嘛 若是尚未存在一个这么个用户，那么就随便找一个用户 并且切换为可推荐的
        Login target = loginRepository.findByLoginName(InitService.cjMobile);
        if (target == null) {
            target = addRoot(InitService.cjMobile, "蒋");
        }
        loginService.setGuidable(target.getId(), true);

        RechargeCardBatch batch = rechargeCardService.newBatch(null
                , target.getId(), "caijiang@mingshz.com"
                , 20, true);
        return new Object[]{batch.getCardSet()};
    }

    @Autowired
    private StoreItemRepository storeItemRepository;

    private Item createItem(Merchant merchant, String name, BigDecimal cost, AuditStatus status, boolean enabled
            , Store store, boolean storeEnabled) throws IOException {
        String path = "tmp/" + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
        try (InputStream stream = new ClassPathResource("simpleItem.jpg").getInputStream()) {
            resourceService.uploadResource(path, stream);
        }
        Item item = itemService.addItem(merchant, path, "staging项目" + name, "staging"
                , cost.multiply(BigDecimal.valueOf(5)), cost.multiply(BigDecimal.valueOf(2)), cost
                , "一个好项目", "<p>真的是一个好项目</p>", false);
        item.setAuditStatus(AuditStatus.AUDIT_PASS);
        item.setEnabled(enabled);
        item = itemRepository.save(item);
        if (store != null) {
            StoreItem storeItem = storeItemService.addStoreItem(store.getId(), item.getId(), null, false);
            storeItem.setEnable(storeEnabled);
            storeItemRepository.save(storeItem);
        }
        item.setAuditStatus(status);
        item = itemRepository.save(item);
        return item;
    }

    private Address address(String other) {
        Address address = new Address();
        address.setProvince("浙江省");
        address.setPrefecture("绍兴市");
        address.setCounty("诸暨市");
        address.setOtherAddress(other);
        return address;
    }
}
