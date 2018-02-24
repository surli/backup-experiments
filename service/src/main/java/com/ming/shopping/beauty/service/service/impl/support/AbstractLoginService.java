package com.ming.shopping.beauty.service.service.impl.support;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.wx.model.Gender;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author CJ
 */
public abstract class AbstractLoginService {

    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private LoginService loginService;

    protected Login addRoot(String mobile, String lastName) {
        final Login target = loginRepository.findByLoginName(mobile);
        if (target == null) {
            Login login = loginService.newUser(mobile, lastName, Gender.male, null, null
                    , null, null);
            login.addLevel(ManageLevel.root);
            login.setGuidable(true);
            return loginRepository.save(login);
        }
        return target;
    }
}
