package com.ming.shopping.beauty.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.model.definition.DefinitionModel;
import com.ming.shopping.beauty.service.model.request.ItemSearcherBody;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.service.StoreService;
import com.ming.shopping.beauty.service.service.SystemService;
import com.ming.shopping.beauty.service.utils.Constant;
import com.ming.shopping.beauty.service.utils.LoginAuthentication;
import me.jiangcai.crud.row.IndefiniteFieldDefinition;
import me.jiangcai.jpa.entity.support.Address;
import me.jiangcai.lib.resource.service.ResourceService;
import me.jiangcai.lib.test.SpringWebTest;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.model.WeixinUserDetail;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import me.jiangcai.wx.test.WeixinTestConfig;
import me.jiangcai.wx.test.WeixinUserMocker;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import org.springframework.test.web.servlet.htmlunit.webdriver.WebConnectionHtmlUnitDriver;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
@ActiveProfiles({ServiceConfig.PROFILE_TEST, ServiceConfig.PROFILE_UNIT_TEST})
@ContextConfiguration(classes = {CoreServiceTestConfig.class})
@WebAppConfiguration
public abstract class CoreServiceTest extends SpringWebTest {
    @Autowired
    protected WeixinTestConfig weixinTestConfig;
    @Autowired
    protected MerchantService merchantService;
    @Autowired
    protected StoreService storeService;
    @Autowired
    protected LoginService loginService;
    @Autowired
    protected RepresentService representService;
    @Autowired
    protected ItemService itemService;
    @Autowired
    protected StoreItemService storeItemService;
    @Autowired
    protected MainOrderService mainOrderService;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private ResourceService resourceService;

    protected Login allRunWith;

    private static final String firstName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍虞万支柯咎管卢莫经房裘缪干解应宗宣丁贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於惠甄魏加封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭厉戎祖武符刘姜詹束龙叶幸司韶郜黎蓟薄印宿白怀蒲台从鄂索咸籍赖卓蔺屠蒙池乔阴郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮牛寿通边扈燕冀郏浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庚终暨居衡步都耿满弘匡国文寇广禄阙东殴殳沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空曾毋沙乜养鞠须丰巢关蒯相查后江红游竺权逯盖益桓公万俟司马上官欧阳夏侯诸葛闻人东方赫连皇甫尉迟公羊澹台公冶宗政濮阳淳于仲孙太叔申屠公孙乐正轩辕令狐钟离闾丘长孙慕容鲜于宇文司徒司空亓官司寇仉督子车颛孙端木巫马公西漆雕乐正壤驷公良拓拔夹谷宰父谷粱晋楚阎法汝鄢涂钦段干百里东郭南门呼延归海羊舌微生岳帅缑亢况后有琴梁丘左丘东门西门商牟佘佴伯赏南宫墨哈谯笪年爱阳佟第五言福百家姓续";

    protected static final String RESULT_CODE_PATH = "$.resultCode";
    protected static final String RESULT_MESSAGE_PATH = "$.resultMsg";
    protected static final String RESULT_DATA_PATH = "$.data";
    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 可以覆盖该方法设定每次测试都将以该身份进行
     *
     * @return 模拟身份
     */
    protected Login allRunWith() {
        return allRunWith;
    }

    /**
     * 将以target为当前身份运行之后所有的程序
     *
     * @param target
     * @see #allRunWith()
     */
    protected void updateAllRunWith(Login target) {
        allRunWith = target;
    }


    @Override
    protected final Authentication autoAuthentication() {
        Login login = allRunWith();
        if (login == null)
            return null;
        return new LoginAuthentication(login.getId(), loginService);
    }

    /**
     * @return 生成一个新的微信帐号，并且应用在系统中
     */
    protected WeixinUserDetail nextCurrentWechatAccount() {
        WeixinUserDetail detail = WeixinUserMocker.randomWeixinUserDetail();
        weixinTestConfig.setNextDetail(detail);
        return detail;
    }

    protected WeixinUserDetail nextCurrentWechatAccount(StandardWeixinUser weixinUser) {
        weixinTestConfig.setNextDetail(weixinUser);
        return weixinUser;
    }

    @Override
    protected DefaultMockMvcBuilder buildMockMVC(DefaultMockMvcBuilder builder) {
        return super.buildMockMVC(builder);
    }

    protected MockHttpServletRequestBuilder wechatPost(String urlTemplate, Object... urlVariables) {
        return makeWechat(super.post(urlTemplate, urlVariables));
    }

    protected MockHttpServletRequestBuilder wechatGet(String urlTemplate, Object... urlVariables) {
        return makeWechat(super.get(urlTemplate, urlVariables));
    }

    protected MockMultipartHttpServletRequestBuilder wechatFileUpload(String urlTemplate, Object... urlVariables) {
        return makeWechat(super.fileUpload(urlTemplate, urlVariables));
    }

    @SuppressWarnings("unchecked")
    protected <T extends MockHttpServletRequestBuilder> T makeWechat(T builder) {
        return (T) builder.header("user-agent", "MicroMessenger");
    }

    @Override
    protected MockMvcHtmlUnitDriverBuilder buildWebDriver(MockMvcHtmlUnitDriverBuilder mockMvcHtmlUnitDriverBuilder) {
        return mockMvcHtmlUnitDriverBuilder.withDelegate(new WebConnectionHtmlUnitDriver() {
            @Override
            protected WebClient modifyWebClientInternal(WebClient webClient) {
                webClient.addRequestHeader("user-agent", "MicroMessenger");
                return super.modifyWebClientInternal(webClient);
            }
        });
    }


    /**
     * 随机生成一个登陆用户
     *
     * @return
     */
    protected Login mockLogin() throws Exception {
        return mockLogin(null, null);
    }

    /**
     * 生成一个有推荐人的用户
     *
     * @param guideUserId
     * @return
     */
    protected Login mockLogin(long guideUserId) throws Exception {
        return mockLogin(null, guideUserId);
    }

    /**
     * 生成一个登陆用户
     *
     * @param cardNo      卡密
     * @param guideUserId 推荐人
     * @return
     */
    protected Login mockLogin(String cardNo, Long guideUserId) throws Exception {
        nextCurrentWechatAccount();
        String mobile = randomMobile();
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(mobile);
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));
        registerBody.setCdKey(cardNo);
        registerBody.setGuideUserId(guideUserId);
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().isOk());
        return loginService.findOne(mobile);
    }

    /**
     * 生成一个商户
     *
     * @return
     */
    protected Merchant mockMerchant() throws Exception {
        Login login = mockLogin();
        Merchant merchant = new Merchant();
        merchant.setName(randomChinese(5));
        merchant.setContact(randomChinese(3));
        merchant.setTelephone(randomMobile());
        merchant.setAddress(mockAddress("模拟商户的地址"));
        return merchantService.addMerchant(login.getId(), merchant);
    }

    protected Login mockRoot() throws Exception {
        return mockManager(ManageLevel.root);
    }

    protected Login mockManager(ManageLevel level) throws Exception {
        Login login = mockLogin();
        loginService.setManageLevel(login.getId(), level);
        return login;
    }

    /**
     * 生成一个商户管理员
     *
     * @param merchant     所属商户
     * @param manageLevels 操作员级别
     * @return
     */
    protected Merchant mockMerchantManager(Merchant merchant, ManageLevel... manageLevels) throws Exception {
        Login login = mockLogin();
        return mockMerchantManager(login, merchant, manageLevels);
    }

    /**
     * 设置某个角色为商户的操作员
     *
     * @param login        登录角色
     * @param merchant     所属商户
     * @param manageLevels 操作员级别
     * @return
     * @throws Exception
     */
    protected Merchant mockMerchantManager(Login login, Merchant merchant, ManageLevel... manageLevels) throws Exception {
        merchantService.addMerchantManager(merchant, login.getId(), Stream.of(manageLevels).collect(Collectors.toSet()));
        return merchant;
    }

    /**
     * 生成一个已经上架门店
     *
     * @param merchant
     * @return
     */
    protected Store mockStore(Merchant merchant) throws Exception {
        Login login = mockLogin();
        Store store = storeService.addStore(login.getId(), merchant.getId()
                , randomString(), randomMobile(), randomString(), mockAddress("模拟生成门店的地址"));
        storeService.freezeOrEnable(store.getId(), true);
        return storeService.findStore(store.getId());
    }

    protected Represent mockRepresent(Store store) throws Exception {
        Login login = mockLogin();
        return representService.addRepresent(login.getId(), store.getId());
    }


    /**
     * 在商户下生成一个审核过的已上架的项目
     *
     * @param merchant
     * @return
     */
    protected Item mockItem(Merchant merchant) throws IOException {
        BigDecimal price = BigDecimal.valueOf(random.nextInt(100));
        BigDecimal salesPrice = price.multiply(BigDecimal.valueOf(0.9))
                .setScale(Constant.FLOAT_COLUMN_SCALE, Constant.ROUNDING_MODE);
        BigDecimal costPrice = price.multiply(BigDecimal.valueOf(0.8))
                .setScale(Constant.FLOAT_COLUMN_SCALE, Constant.ROUNDING_MODE);
        Item item = itemService.addItem(merchant, randomTmpImagePath(), randomString(), randomString()
                , price, salesPrice, costPrice, randomString(), randomString(), random.nextBoolean());
        itemService.auditItem(item.getId(), AuditStatus.AUDIT_PASS, null);
        itemService.freezeOrEnable(item.getId(), true);
        return itemService.findOne(item.getId());
    }

    /**
     * 在门店下生成一个已上架的商品项目
     *
     * @param store
     * @param item
     * @return
     */
    protected StoreItem mockStoreItem(Store store, Item item) {
        StoreItem storeItem = storeItemService.addStoreItem(store.getId(), item.getId(), null, random.nextBoolean());
        storeItemService.freezeOrEnable(true, storeItem.getId());
        return storeItemService.findStoreItem(item.getId(), store);
    }

    /**
     * 生成订单
     *
     * @param user      下单用户
     * @param represent 下单代表
     * @param storeItem 门店项目
     * @param amount    下单数量
     * @return
     */
    protected MainOrder mockMainOrder(User user, Represent represent, StoreItem storeItem, Integer amount) {
        MainOrder emptyOrder = mainOrderService.newEmptyOrder(user);
        return mainOrderService.supplementOrder(emptyOrder.getOrderId(), represent.getLogin(), represent.getStore(), storeItem, amount);
    }

    protected MainOrder mockMainOrder(User user, Represent represent, Map<StoreItem, Integer> amounts) {
        MainOrder emptyOrder = mainOrderService.newEmptyOrder(user);
        return mainOrderService.supplementOrder(emptyOrder.getOrderId(), represent.getLogin(), represent.getStore(), amounts);
    }

    protected MainOrder mockMainOrder(User user, Represent represent) {
        return mockMainOrder(user, represent, randomOrderItemSet(represent.getStore().getId()));
    }

    protected Map<StoreItem, Integer> randomOrderItemSet(long storeId) {
        Map<StoreItem, Integer> data = new HashMap<>();
        //查找门店可用的项目
        ItemSearcherBody searcher = new ItemSearcherBody();
        searcher.setStoreId(storeId);
        List<StoreItem> storeItemList = storeItemService.findAllStoreItem(searcher);

        int count = 1 + random.nextInt(storeItemList.size());
        while (count-- > 0 || data.size() == 0) {
            StoreItem randomItem = storeItemList.stream()
                    .filter(item -> !data.keySet().contains(item))
                    .max(new RandomComparator()).orElse(null);
            if (randomItem != null) {
                data.put(randomItem, 1 + random.nextInt(10));
            }
        }
        return data;
    }

    @SuppressWarnings("WeakerAccess")
    protected Login mockGuidableLogin() throws Exception {
        Login login = mockLogin();
        loginService.setGuidable(login.getId(), true);
        return login;
    }

    protected RechargeCard mockRechargeCard() throws Exception {
        return mockRechargeCard(mockGuidableLogin());
    }

    protected RechargeCard mockRechargeCard(Login guide) throws Exception {
        RechargeCardBatch batch = rechargeCardService.newBatch(null, guide.getId()
                , randomEmailAddress(), 1, false);
        return batch.getCardSet().stream()
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * 随机中文
     *
     * @param length
     * @return
     */
    protected String randomChinese(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(firstName.length());
            sb.append(firstName.substring(index, index + 1));
        }
        return sb.toString();
    }

    /**
     * 随机枚举
     *
     * @param enumCls
     * @param <T>
     * @return
     */
    protected <T extends Enum> T randomEnum(Class<T> enumCls) {
        T[] enumArr = enumCls.getEnumConstants();
        return enumArr[random.nextInt(enumArr.length)];
    }

    /**
     * @param model
     * @return 生成一个Matcher 可以确认响应是一个数组，并且包含的对象符合model
     * @see #matchModel(DefinitionModel)
     */
    protected static Matcher<?> matchModels(DefinitionModel<?> model) {
        return new Matcher<Object>() {
            @Override
            public boolean matches(Object item) {
                assertThat(item)
                        .as("必须是一个数组")
                        .isInstanceOf(Collection.class);
                Collection collection = (Collection) item;
                final Optional any = collection.stream().findAny();
                if (!any.isPresent()) {
                    throw new IllegalStateException("基于测试的目的，必须给我们一个size > 0 的响应数组");
                }

                return matchModel(model).matches(any.get());
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("不满足" + model + "的定义");
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    /**
     * @param model 特定Model
     * @return 生成一个Matcher 可以比较响应跟Model约定的区别
     */
    protected static Matcher<?> matchModel(DefinitionModel<?> model) {
        return new Matcher<Object>() {
            @Override
            public boolean matches(Object item) {
                assertThat(item)
                        .describedAs("不可为空")
                        .isNotNull()
                        .describedAs("必须是一个Map")
                        .isInstanceOf(Map.class);
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>) item;
                assertThat(
                        map.keySet()
                )
                        .describedAs("键值必须满足" + model + "所定义")
                        .hasSameElementsAs(
                                model.getDefinitions().stream()
                                        .map(IndefiniteFieldDefinition::name)
                                        .collect(Collectors.toSet())
                        );
                return true;
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("不满足" + model + "的定义");
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    /**
     * @return 一个新增的临时图片path
     */
    protected String randomTmpImagePath() throws IOException {
        String path = "tmp/" + UUID.randomUUID().toString() + ".png";
        resourceService.uploadResource(path, new ClassPathResource("/image.png").getInputStream());
        return path;
    }

    private Address mockAddress(String other) {
        Address address = new Address();
        String[] province = new String[]{"浙江省", "湖北省", "湖南省"};
        address.setProvince(province[random.nextInt(province.length)]);
        String[] prefecture = new String[]{"绍兴市", "杭州市", "温州市"};
        address.setPrefecture(prefecture[random.nextInt(prefecture.length)]);
        String[] county = new String[]{"诸暨市", "滨江区", "余杭区"};
        address.setCounty(county[random.nextInt(county.length)]);
        address.setOtherAddress(other);
        return address;
    }
}
