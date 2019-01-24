package tau.runtime;

import java.util.*;
import tau.literal.*;
import tau.interpreter.Interpreter;

public class Environment {
	public final Environment enclosing;
	private final Map<String, Object> values =
			new HashMap<>();
	
	public Environment() {
		enclosing = null;
	}
	
	public Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}
	
	public void define(String name, Object value) {
		values.put(name, value);
	}
	
	public void assign(TToken name,
			Object value) {
		if(values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}
		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}
		throw new Interpreter.RuntimeError(name,
				"The variable '" + name.lexeme + "' doesn't exist.");
	}
	
	public Object get(TToken name) {
	    if (values.containsKey(name.lexeme)) {
	      return values.get(name.lexeme);
	    }
	    if(enclosing != null)
	    		return enclosing.get(name);
	    throw new Interpreter.RuntimeError(name,
	        "Undefined variable '" + name.lexeme + "'.");
	}
	
	public Object getAt(int distance, String name) {
	    return ancestor(distance).values.get(name);
	}
	
	public void assignAt(int distance, TToken name, Object value) {
	    ancestor(distance).values.put(name.lexeme, value);
	}
	
	public Environment ancestor(int distance) {
	    Environment environment = this;
	    for (int i = 0; i < distance; i++) {
	    		environment = environment.enclosing; 
	    }
	    return environment;
	}
}
