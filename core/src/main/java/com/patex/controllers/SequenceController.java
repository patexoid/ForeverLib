package com.patex.controllers;

import com.patex.mapper.SequenceMapper;
import com.patex.model.Sequence;
import com.patex.service.SequenceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/sequence")
public class SequenceController {

    private final SequenceService sequenceService;

    private final SequenceMapper mapper;

    public SequenceController(SequenceService sequenceService, SequenceMapper mapper) {
        this.sequenceService = sequenceService;
        this.mapper = mapper;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Sequence getSequence(@PathVariable(value = "id") long id) {
        return mapper.toDto(sequenceService.getSequence(id));
    }
}
