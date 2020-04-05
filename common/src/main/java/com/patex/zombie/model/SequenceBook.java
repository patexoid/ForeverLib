package com.patex.zombie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SequenceBook {

    private int seqOrder;

    private Book book;
}