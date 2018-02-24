package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author CJ
 */
public class RechargeCardServiceTest extends CoreServiceTest {

    @Autowired
    private RechargeCardService rechargeCardService;

    @Test
    public void localReport() throws IOException, ClassNotFoundException {
        // 生成一个xls文件
        File file = new File("target/card_report.xls");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            RechargeCardBatch batch = rechargeCardService.newBatch(null, loginService.findOne(InitService.cjMobile).getId()
                    , "caijiang@mingshz.com", 20, false);
            rechargeCardService.batchReport(batch, outputStream);
            outputStream.flush();
        }
    }

}