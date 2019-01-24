package tau.core;

import java.util.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import tau.runtime.*;
import tau.literal.*;
import tau.interpreter.*;

public class IO extends ModuleInstance {
	public IO() {
		super(null);
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
						BufferedReader reader = new BufferedReader(new FileReader(Interpreter.stringify(
								arguments.get(0))));
						return reader.readLine();
					} catch (IOException e) {
						throw new Interpreter.RuntimeError(name, "Could not get file or read it..");
					}
				}
			};
		} else if(name.lexeme.equals("puts")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					System.out.println(Interpreter.stringify(
							arguments.get(0)));
					return null;
				}
			};
		} else if(name.lexeme.equals("gets")) {
			return new ICallable() {
				@Override
				public int arity() {
					return 1;
				}
				@Override
				public Object call(Interpreter interpreter,
						List<Object> arguments) {
					try {
						InputStreamReader ir =
								new InputStreamReader(System.in);
						BufferedReader buffer =
								new BufferedReader(ir);
						System.out.print(Interpreter.stringify(
								arguments.get(0)));
						return buffer.readLine();
					} catch(IOException e) {
						throw new Interpreter.RuntimeError(name,
								"Could not start input stream reader.");
					}
				}
			};
		}
		throw new Interpreter.RuntimeError(name,
				"Could not get property from core.");
	}
}
