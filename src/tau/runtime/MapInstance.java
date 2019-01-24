package tau.runtime;

import tau.ast.*;
import tau.interpreter.Interpreter;
import tau.literal.*;
import java.util.*;

public class MapInstance {
	public final TToken name;
	public final Map<String, Stmt.MapValue> values;
	
	public MapInstance(TToken name,
			Map<String, Stmt.MapValue> values) {
		this.name = name;
		this.values = values;
	}
	
	public Object get(TToken name) {
		if (values.containsKey(name.lexeme)) {
		      return values.get(name.lexeme).value;
		}
		throw new Interpreter.RuntimeError(name, 
		     "Undefined property '" + name.lexeme + "'.");
	}
}
