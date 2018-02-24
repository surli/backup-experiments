package com.ming.shopping.beauty.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import me.jiangcai.lib.git.bean.GitRepositoryState;
import me.jiangcai.lib.sys.service.SystemStringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author helloztt
 */
@Controller
public class InitController {
    private final ObjectMapper mapper;
    private final GitRepositoryState state;
    private final SystemStringService systemStringService;
    private final Environment env;

    @Autowired
    public InitController(GitRepositoryState gitRepositoryState
            , SystemStringService systemStringService
            , Environment env) {
        this.mapper = new ObjectMapper();
        this.state = gitRepositoryState;
        this.systemStringService = systemStringService;
        this.env = env;
    }

    /**
     * 提供一些初始化的数据
     *
     * @return
     */
    @GetMapping("/init")
    public ResponseEntity init() throws JsonProcessingException {
        InitModel initModel = new InitModel();
        initModel.setMinRechargeAmount(systemStringService.getCustomSystemString("shopping.service.recharge.min.amount"
                , null, true, Double.class
                , env.acceptsProfiles("staging") ? 0D : 5000D));
        initModel.setVersion(state.getBuildVersion() + "-" + state.getCommitId());
        initModel.setSystemEmailAddress(env.getProperty("me.jiangcai.lib.notice.email.from.email"));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8)
                .body((mapper.writeValueAsString(initModel)));
    }

    @Data
    class InitModel {
        private Double minRechargeAmount;
        private String version;
        private String systemEmailAddress;
    }
}
