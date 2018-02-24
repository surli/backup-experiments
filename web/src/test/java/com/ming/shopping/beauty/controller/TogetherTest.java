package com.ming.shopping.beauty.controller;


import com.ming.shopping.beauty.config.WebConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {WebConfig.class, EmulationTestConfig.class})
public abstract class TogetherTest extends CoreServiceTest {
}
