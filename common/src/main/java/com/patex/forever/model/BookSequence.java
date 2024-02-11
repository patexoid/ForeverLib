package com.patex.forever.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSequence {

    private Long Id;

    private int seqOrder;

    private String sequenceName;
}
