package com.ming.shopping.beauty.service.utils;

import me.jiangcai.lib.resource.service.ResourceService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * @author CJ
 */
public class Utils {
    /**
     * 将资源转换成为URL字符串
     */
    public static BiFunction<Object, MediaType, Object> formatResourcePathToURL(ResourceService resourceService) {
        return (data, type) -> {
            if (data == null)
                return null;
            try {
                return resourceService.getResource(data.toString()).httpUrl().toString();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
