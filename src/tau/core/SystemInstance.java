package tau.core;

import java.util.*;
import tau.runtime.*;
import tau.literal.*;
import tau.Tau;
import tau.interpreter.*;


public class SystemInstance extends ModuleInstance {
	public SystemInstance() {
		super(null);
	}
	
	public Object get(TToken name) {
		if(name.lexeme.equals("gc")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					System.gc();
					return null;
				}
			};
		} else if(name.lexeme.equals("os")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					return System.getProperty("os.name");
				}
			};
		} else if(name.lexeme.equals("user")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					return System.getProperty("user.name");
				}
			};
		} else if(name.lexeme.equals("argv")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 0;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					return new Array(Arrays.asList((Object[])Tau.arguments));
				}
			};
		} else if(name.lexeme.equals("cwd")) {
			return System.getProperty("user.dir");
		} else if(name.lexeme.equals("halt")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 2;
				}
				
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					if(arguments.get(1) == null) {
						System.out.println("Exited");
						System.exit(((Double)arguments.get(0)).intValue());
						return null;
					} else {
						System.out.println(
								Interpreter.stringify(arguments.get(1)));
						System.exit(((Double)arguments.get(0)).intValue());
						return null;
					}
				}
			};
		}
		throw new Interpreter.RuntimeError(name,
				"Could not get property from core.");
	}
}
