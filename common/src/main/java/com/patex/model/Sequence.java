package com.patex.model;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class Sequence {

    private Long id;

    private String name;

    private Instant updated;

    private List<SequenceBook> books = new ArrayList<>();

}
