package tau.runtime;

import java.util.*;
import tau.interpreter.*;
import tau.literal.*;

public class ModuleInstance {
	private Module mod;
	private final Map<String, Object> fields = 
			new HashMap<>();

	public ModuleInstance(Module mod) {
	    this.mod = mod;
	}
	
	public Object get(TToken name) {
		if (fields.containsKey(name.lexeme)) {
		      return fields.get(name.lexeme);
		}
		Function method = mod.findMethod(this, name.lexeme);
	    if (method != null) return method;
		throw new Interpreter.RuntimeError(name, 
		     "Undefined property '" + name.lexeme + "'.");
	}
	
	@Override
	public String toString() {
	    return mod.name + " instance";
	}
}
