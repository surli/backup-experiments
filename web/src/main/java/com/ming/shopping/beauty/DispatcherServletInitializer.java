package com.ming.shopping.beauty;

import com.ming.shopping.beauty.config.WebConfig;
import com.ming.shopping.beauty.service.utils.Constant;
import me.jiangcai.crud.filter.MultiReadSupportFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;


/**
 * @author helloztt
 */
public class DispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{
                WebConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{
                new CharacterEncodingFilter(Constant.UTF8_ENCODIND)
                , new HttpPutFormContentFilter()
//                , new MultiReadSupportFilter()
        };
    }


}
