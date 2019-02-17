package sample;


import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

enum States {
    WELLCOME, CONNECTING, PLC_CONNECTED, DB_CONNECTED, WAITING_WO, WAITING_UDM, MACHINE_CYCLE, ACCEPTED, REJECTED, CLOSING
}

enum Events {
    onPLCDisconnected, onDBDisconnected, onWOInserted, onUDMInserted, onACCEPTED, onREJECTED, onCODENOTPRESENT, onPLCALARM
}


@Configuration
@EnableStateMachineFactory
public class FSMPressaConfiguration  extends StateMachineConfigurerAdapter<States, Events>{

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(new StateMachineListenerAdapter<States, Events>() {
                    @Override
                    public void stateChanged(State<States, Events> from, State<States, Events> to) {
                        super.stateChanged(from, to);
                    }
                });
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
                .withStates()
                .initial(States.WELLCOME)
                .state(States.CONNECTING)
                .end(States.CLOSING);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
                .withExternal()
                .source(States.WELLCOME).target(States.CONNECTING).event(Events.onPLCDisconnected);
    }
}
