package com.epam.sm.example.squirrel;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SquirrelStateMachineContext {

    private Map<String, Object> contextVariables;

    private SquirrelStateMachineContext(Map<String, Object> contextVariables) {
        this.contextVariables = contextVariables;
    }

    public Object getVariable(String key) {
        return contextVariables.get(key);
    }

    public static SquirrelStateMachineContextBuilder builder() {
        return new SquirrelStateMachineContextBuilder();
    }

    public static class SquirrelStateMachineContextBuilder {
        private Map<String, Object> contextVariables;

        SquirrelStateMachineContextBuilder() {
            contextVariables = new HashMap<>();
        }

        public SquirrelStateMachineContextBuilder addVariable(String key, Object var) {
            this.contextVariables.put(key, var);
            return this;
        }

        public SquirrelStateMachineContext build() {
            return new SquirrelStateMachineContext(contextVariables);
        }
    }
}
