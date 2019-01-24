package tau.core;

import tau.runtime.*;
import tau.literal.*;
import tau.interpreter.*;

import java.io.*;

import java.util.*;

public class FileInstance extends ModuleInstance {
	public String path;
	
	public FileInstance(String path) {
		super(null);
		this.path = path;
	}
	
	public Object get(TToken name) {
		if(name.lexeme.equals("readLine")) {
			return new ICallable() {
				public int arity() {
					return 0;
				}
				@Override
				public Object call(Interpreter interpreter, List<Object> arguments) {
					try {
						BufferedReader reader = new BufferedReader(new FileReader(path));
						return reader.readLine();
					} catch (IOException e) {
						throw new Interpreter.RuntimeError(name, "Could not get file or read it..");
					}
				}
			};
		}
		throw new Interpreter.RuntimeError(name, "Could not get property.");
	}
}
