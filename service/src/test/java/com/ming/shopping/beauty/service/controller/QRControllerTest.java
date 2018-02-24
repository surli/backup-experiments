package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.CoreServiceTest;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author CJ
 */
public class QRControllerTest extends CoreServiceTest {

    @Test
    public void go() throws Exception {
        mockMvc.perform(
                get("/qrImageForText")
                        .param("text", UUID.randomUUID().toString())
        )
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
    }

}