package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.login.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by helloztt on 2017/12/21.
 */
public interface UserRepository extends JpaRepository<User,Long>,JpaSpecificationExecutor<User>{
}
