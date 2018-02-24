package com.ming.shopping.beauty.controller;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author helloztt
 */
public class InitControllerTest extends TogetherTest {
    @Test
    public void init() throws Exception {
        mockMvc.perform(get("/init"))
                .andDo(print())
                .andExpect(jsonPath("$.minRechargeAmount").exists())
                .andExpect(jsonPath("$.version").exists());
    }

}