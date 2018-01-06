package org.hswebframework.web.authorization.cloud.client.feign;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "${hsweb.cloud.user-center.name:user-center}")
public interface FeignAuthenticationManager extends AuthenticationManager {
    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-auth/{userId}", method = RequestMethod.GET)
    Authentication getByUserId(@PathVariable("userId") String userId);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-auth", method = RequestMethod.PUT)
    Authentication sync(Authentication authentication);
}
