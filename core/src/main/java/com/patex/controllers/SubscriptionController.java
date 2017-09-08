package com.patex.controllers;

import com.patex.LibException;
import com.patex.entities.Subscription;
import com.patex.service.SubscriptionService;
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

import static com.patex.service.ZUserService.USER;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

//    private static Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Subscription getSubscription(@PathVariable(value = "id") long id) {
        return subscriptionService.findOne(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<Subscription> getSubscriptions(Pageable pageable) {
        return subscriptionService.findAll(pageable);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Secured(USER)
    public @ResponseBody
    Subscription updateSubscription(@RequestBody Subscription book) throws LibException {
        return subscriptionService.save(book);
    }

}