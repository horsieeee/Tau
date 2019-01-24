package tau.core;

import tau.runtime.*;
import tau.interpreter.*;
import tau.literal.*;
import java.util.*;

public class Array extends ModuleInstance {
	public final List<Object> elements;
	
	public Array(
			List<Object> elements) {
		super(null);
		this.elements = elements;
	}
	
	public Object get(TToken name) {
		if(name.lexeme.equals("length")) {
			return elements.toArray().length;
		} else if(name.lexeme.equals("get")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					return elements.get(
							((Double)arguments.get(0)).intValue());
				}
			};
		} else if(name.lexeme.equals("set")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 2;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					elements.set(((Double)arguments.get(0)).intValue(),
							arguments.get(0));
					return null;
				}
			};
		} else if(name.lexeme.equals("remove")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					elements.remove(
							((Double)arguments.get(0)).intValue());
					return null;
				}
			};
		}
		throw new Interpreter.RuntimeError(name, "Could not find property.");
	}
}
