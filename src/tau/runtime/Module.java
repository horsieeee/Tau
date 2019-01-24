package tau.runtime;

import java.util.*;
import tau.interpreter.*;

public class Module implements ICallable {
	public final String name;
	private final Map<String, Function> methods;
	
	public Module(String name, 
			Map<String, Function> methods) {
		this.name = name;
		this.methods = methods;
	}
	
	public Function findMethod(ModuleInstance instance, 
			String name) {
	    if (methods.containsKey(name)) {
	      return methods.get(name);
	    }
	    return null;
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
	    ModuleInstance instance = new ModuleInstance(this);
	    return instance;
	}

	@Override
	public int arity() {
	    return 0;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
