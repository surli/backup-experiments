package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.LoginRequest;
import com.ming.shopping.beauty.service.repository.LoginRequestRepository;
import com.ming.shopping.beauty.service.service.LoginRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Service
public class LoginRequestServiceImpl implements LoginRequestService {
    @Autowired
    private LoginRequestRepository loginRequestRepository;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public LoginRequest newRequest(String sessionId) {
        LoginRequest loginRequest = loginRequestRepository.findBySessionId(sessionId);
        if (loginRequest == null) {
            loginRequest = new LoginRequest();
            loginRequest.setSessionId(sessionId);
        }
        loginRequest.setLogin(null);
        loginRequest.setRequestTime(LocalDateTime.now());
        return loginRequestRepository.save(loginRequest);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public LoginRequest login(long requestId, Login login) {
        LoginRequest request = findOne(requestId);
        if (request == null) {
            return null;
        }
        request.setLogin(login);
        return loginRequestRepository.save(request);
    }

    @Override
    public LoginRequest findOne(long requestId) {
        return loginRequestRepository.findOne(requestId);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void remove(long requestId) {
        loginRequestRepository.delete(requestId);
    }

    @Override
    public void remove(String sessionId, Login login) {
        loginRequestRepository.delete(loginRequestRepository.findBySessionIdAndLogin(sessionId, login));
    }
}
