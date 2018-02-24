package com.ming.shopping.beauty.service.service.impl;

import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.lib.upgrade.VersionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author CJ
 */
@Service
public class VersionInfoServiceImpl implements VersionInfoService {
    @Autowired
    private SystemStringService systemStringService;

    @Override
    public <T extends Enum> T currentVersion(Class<T> type) {
        String s = systemStringService.getSystemString("_v", String.class, null);
        if (s == null)
            return null;
        for (T x : type.getEnumConstants()) {
            if (x.name().equals(s))
                return x;
        }
        throw new IllegalStateException("未被识别的版本符号：" + s + "，绝对禁止使用旧版本的程序运行新版本的数据库");
    }

    @Override
    public <T extends Enum> void updateVersion(T currentVersion) {
        systemStringService.updateSystemString("_v", currentVersion.name());
    }
}
