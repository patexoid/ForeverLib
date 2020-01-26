package com.patex.zombie.opds.api;

import com.patex.model.AggrResult;
import com.patex.model.Author;
import com.patex.model.Sequence;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "core")
@RequestMapping("/sequence")
public interface SequenceClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    Sequence getSequence(long id);
}
