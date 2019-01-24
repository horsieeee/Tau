package tau.error;

import tau.Tau;
import tau.literal.*;
import tau.interpreter.*;

public class Error {
	public static void runtimeError(Interpreter.RuntimeError error) {
	    System.err.println(error.getMessage() +
	        "\n[line " + error.token.line + "]");
	    Tau.hadRuntimeError = true;
	}
	public static void error(TToken line,
			String message) {
		error(line.line, message);
	}
	
	public static void error(Integer line,
			String message) {
		report(line, message);
	}
	
	public static void importError(String message) {
		System.err.println("[runtime] Import Error: " + message);
		Tau.hadError = true;
	}
	
	public static void report(Integer line,
			String message) {
		System.err.println("[line " + line.toString() + "] Error: " +
			message);
		Tau.hadError = true;
	}
}
