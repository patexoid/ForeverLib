package com.patex.forever.controllers;

import com.patex.forever.model.Sequence;
import com.patex.forever.service.SequenceService;
import com.patex.forever.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.patex.forever.service.UserService.ADMIN_AUTHORITY;

@Controller
@RequestMapping("/sequence")
@RequiredArgsConstructor
public class SequenceController {

    private final SequenceService sequenceService;
    private final TransactionService transactionService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Sequence getSequence(@PathVariable(value = "id") long id) {
        return sequenceService.getSequenceSimplified(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<Sequence> getSequences(Pageable pageable, @RequestParam(required = false) String prefix) {
        return sequenceService.getSequences(pageable, prefix);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    Sequence renameSequence(@PathVariable(value = "id") long id, @RequestBody Sequence sequence) {
        return sequenceService.renameSequence(id, sequence.getName());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    void deleteSequence(@PathVariable(value = "id") long id) {
        sequenceService.deleteSequence(id);
    }

    @RequestMapping(value = "/{id}/order/{bookId}", method = RequestMethod.PUT)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    void setBookOrder(@PathVariable(value = "id") long id, @PathVariable(value = "bookId") long bookId,
                       @RequestParam("seqOrder") int seqOrder) {
        sequenceService.setBookOrder(id, bookId, seqOrder);
    }

    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    Sequence mergeSequences(@RequestParam("id") Long... ids) {
        List<Sequence> sequences = Arrays.stream(ids).map(id -> {
            Sequence sequence = new Sequence();
            sequence.setId(id);
            return sequence;
        }).collect(Collectors.toList());
        return transactionService.transactionRequired(() -> sequenceService.mergeSequences(sequences));
    }
}
