package com.patex.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class TransactionService {


    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void newTransaction(Runnable run){
        run.run();
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public <T> T newTransaction(Supplier<T> supplier){
        return supplier.get();
    }

    @Transactional(propagation = REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void transactionRequired(Runnable run){
        run.run();
    }

    @Transactional(propagation = REQUIRED, isolation = Isolation.SERIALIZABLE)
    public <T> T transactionRequired(Supplier<T> supplier){
        return supplier.get();
    }

}
