package com.patex.service;

import com.patex.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
public class SequenceService {

  @Autowired
  SequenceRepository sequenceRepository;

  public Sequence getSequence(long id){
    return sequenceRepository.findOne(id);
  }

}
