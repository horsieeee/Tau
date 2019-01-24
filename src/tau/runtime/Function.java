package tau.runtime;

import tau.ast.*;
import tau.interpreter.*;
import java.util.*;

public class Function implements ICallable {
	private String name;
	private final Expr.Function declaration;
	private final Environment closure;
	
	public Function(
			String name,
			Expr.Function declaration, Environment closure) {
		this.name = name;
		this.closure = closure;
		this.declaration = declaration;
	}
	
	@Override
	public int arity() {
	    return declaration.parameters.size();
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(closure);
	    for (int i = 0; i < declaration.parameters.size(); i++) {
	    		environment.define(declaration.parameters.get(i).lexeme,
	    				arguments.get(i));
	    }
	    try {
	        interpreter.executeBlock(declaration.body, environment);
	    } catch (Interpreter.Return returnValue) {
	        return returnValue.value;
	    }
	    return null;
	}
}
