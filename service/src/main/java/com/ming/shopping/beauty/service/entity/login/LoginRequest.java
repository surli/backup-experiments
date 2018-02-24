package com.ming.shopping.beauty.service.entity.login;

import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 管理后台登录请求，需要定期清理
 * @author helloztt
 */
@Entity
@Setter
@Getter
public class LoginRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @OneToOne
    private Login login;

    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime requestTime;

    @Column(columnDefinition = Constant.DATE_NULLABLE_COLUMN_DEFINITION)
    private LocalDateTime expiredTime;
}
