package com.patex.forever.opds.controller;

import com.patex.forever.LibException;
import com.patex.forever.opds.entity.SubscriptionEntity;
import com.patex.forever.opds.service.SubscriptionService;
import com.patex.forever.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

//   private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody
    SubscriptionEntity getSubscription(@PathVariable(value = "id") long id) {
        return subscriptionService.findOne(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<SubscriptionEntity> getSubscriptions(Pageable pageable) {
        return subscriptionService.findAll(pageable);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Secured(UserService.USER)
    public @ResponseBody
    SubscriptionEntity updateSubscription(@RequestBody SubscriptionEntity book) throws LibException {
        return subscriptionService.save(book);
    }

}