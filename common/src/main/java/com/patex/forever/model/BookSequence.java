package com.patex.forever.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSequence {

    private Long id;

    private int seqOrder;

    private String sequenceName;
}
