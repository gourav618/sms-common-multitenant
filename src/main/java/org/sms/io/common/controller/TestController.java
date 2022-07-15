package org.sms.io.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.sms.io.common.model.Model;
import org.sms.io.common.repo.ModelRepo;
import org.sms.io.common.utils.TenantContext;
import org.sms.io.common.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @Autowired
    private UserContext userContext;

    @Autowired
    private ModelRepo modelRepo;

    @GetMapping("/test")
    public String testController() {
        log.info("current tenant: " +TenantContext.getCurrentTenant());
        log.info("User: "+ userContext.getUser());
        Model model = new Model();
        model.setName(userContext.getUser());
        modelRepo.save(model);
        return "Tested Everything and its working mate";
    }
}
