package com.patex.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Alexey on 31.03.2016.
 */

public class AggrResult {

    String id;
    long result;

    public AggrResult() {
    }

    public AggrResult(String id, long result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
