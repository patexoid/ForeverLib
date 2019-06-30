package com.patex.stateMachine;

public class StateMachineTest {

    private class Initial {

    }

    @StateConfig(stateClass = Initial.class)
    private class InitialProcessor {

        @Transition
        public StateOne toOne(ToStateOne toStateOne) {
            return new StateOne(toStateOne.getValue());
        }
    }

    private class StateOne {
        private String stateValue;

        public StateOne(String stateValue) {
            this.stateValue = stateValue;
        }

    }

    @StateConfig(stateClass = StateOne.class)
    private class StateOneProcessor {

        @Transition(action = Reset.class)
        private Initial reset(){
            return new Initial();
        }
    }


    private class ToStateOne {
        private String value;

        public ToStateOne(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private class Reset{

    }

}
