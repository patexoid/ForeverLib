package com.patex.zombie.opds.api;

import com.patex.model.Sequence;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "core", contextId = "SequenceClient")
@RequestMapping("/sequence")
public interface SequenceClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    Sequence getSequence(long id);
}
