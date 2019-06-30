package com.patex.stateMachine;

public class StateMachine {

    private Object state;

    public StateMachine(Object state) {
        this.state = state;
    }

    public Object getState() {
        return state;
    }

    public void newState(Object transitionInfo) {

    }
}
