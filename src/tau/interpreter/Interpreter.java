package tau.interpreter;

import java.util.*;
import java.util.stream.Collectors;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import tau.Tau;
import tau.analyzer.Analyzer;
import tau.ast.*;
import tau.literal.*;
import tau.scanner.*;
import tau.parser.*;
import tau.error.Error;
import tau.runtime.*;
import tau.runtime.Module;
import tau.core.*;
import static tau.literal.TType.*;
import java.io.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	public final Environment globals = 
			new Environment();
	private Environment environment = 
			globals;
	private final Map<Expr, Integer> locals = 
			new HashMap<>();
	private String default_package_name = "_init.tau";
	
	public Interpreter() {
		globals.define("IO", new IO());
		globals.define("System", new SystemInstance());
		globals.define("getenv", new ICallable() {
			@Override
			public int arity() {
				return 0;
			}
			@Override
			public Object call(Interpreter interpreter,
					List<Object> arguments) {
				return globals;
			}
		});
		globals.define("File", new ICallable() {
			@Override
			public int arity() {
				return 0;
			}
			@Override
			public Object call(Interpreter interpreter,
					List<Object> arguments) {
				return new FileInstance(stringify(arguments.get(0)));
			}
		});
	}
	
	public static class RuntimeError extends RuntimeException {
		  public final TToken token;
		  
		  public RuntimeError(TToken token, String message) {
			  super(message);
			  this.token = token;
		  }
	}
	
	public static class Return extends RuntimeException {
		  public final Object value;

		  public Return(Object value) {
			  super(null, null, false, false);
			  this.value = value;
		  }
	}
	
	public void resolve(Expr expr, int depth) {
	    locals.put(expr, depth);
	}
	
	public void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
		        execute(statement);
		    }
		} catch (RuntimeError error) {
		    Error.runtimeError(error);
		}
	}
	
	@Override
	public Object visitFunction(Expr.Function expr) {
		return new Function(null, expr, environment);
	}
	
	@Override
	public Void visitExpression(Stmt.Expression stmt) {
	    evaluate(stmt.expression);
	    return null; 
	}
	
	@Override
	public Void visitModule(Stmt.Module stmt) {
		environment.define(stmt.name.lexeme, null);
		Map<String, Function> methods = new HashMap<>();
	    for (Stmt.Function method : stmt.methods) {
	      Function function = new Function(method.name.lexeme, method.expr, environment);
	      methods.put(method.name.lexeme, function);
	    }
	    Module mod = new Module(stmt.name.lexeme, methods);
		environment.define(stmt.name.lexeme, mod);
		return null;
	}
	
	@Override
	public Void visitMap(Stmt.Map stmt) {
		environment.define(stmt.name.lexeme, null);
		Map<String, Stmt.MapValue> values =
				new HashMap<>();
		for(Stmt.MapValue st : stmt.values) {
			values.put(st.name.lexeme, st);
		}
		MapInstance map = new MapInstance(stmt.name, values);
		environment.define(stmt.name.lexeme, map);
		return null;
	}
	
	@Override
	public Void visitMapValue(Stmt.MapValue stmt) {
		return null;
	}
	
	@Override
	public Void visitDebug(Stmt.Debug stmt) {
	    Object value = evaluate(stmt.expression);
	    System.out.println(stringify(value));
	    return null;
	}
	
	@Override
	public Void visitFunction(Stmt.Function stmt) {
		Function function = new Function(stmt.name.lexeme, stmt.expr, environment);
		environment.define(stmt.name.lexeme, function);
		return null;
	}
	
	@Override
	public Void visitIf(Stmt.If stmt) {
	    if (truthy(evaluate(stmt.condition))) {
	      execute(stmt.thenBranch);
	    } else if (stmt.elseBranch != null) {
	      execute(stmt.elseBranch);
	    }
	    return null;
	}
	
	@Override
	public Void visitWhile(Stmt.While stmt) {
	    while (truthy(evaluate(stmt.condition))) {
	    		execute(stmt.body);
	    }
	    return null;
	}
	
	@Override
	public Void visitReturn(Stmt.Return stmt) {
	    Object value = null;
	    if (stmt.value != null) 
	    		value = evaluate(stmt.value);
	    throw new Return(value);
	}

	@Override
	public Void visitVar(Stmt.Var stmt) {
	    Object value = null;
	    if (stmt.initializer != null) {
	    		value = evaluate(stmt.initializer);
	    }
	    environment.define(stmt.name.lexeme, value);
	    return null;
	}
	
	@Override
	public Void visitBlock(Stmt.Block stmt) {
	    executeBlock(stmt.statements, new Environment(environment));
	    return null;
	}
	
	@Override
	public Void visitImport(Stmt.Import stmt) {
		processImport((String)stmt.expr.value);
		return null;
	}
	
	@Override
	public Object visitCall(Expr.Call expr) {
		Object callee = evaluate(expr.callee);
		List<Object> arguments = 
				new ArrayList<>();
		for(Expr argument : expr.arguments) {
			arguments.add(evaluate(argument));
		}
		if (!(callee instanceof ICallable)) {
		      throw new RuntimeError(expr.paren,
		          "Can only call functions and classes.");
		}
		ICallable called = (ICallable)callee;
		if (arguments.size() != called.arity()) {
			throw new RuntimeError(expr.paren, "Expected " +
		          called.arity() + " arguments but got " +
		          arguments.size() + ".");
		}
		return called.call(this, arguments);
	}
	
	@Override
	public Object visitGet(Expr.Get expr) {
	    Object object = evaluate(expr.object);
	    if (object instanceof ModuleInstance) {
	      return ((ModuleInstance) object).get(expr.name);
	    } else if (object instanceof MapInstance) {
		  return evaluate((Expr) ((MapInstance) object).get(expr.name));
		}
	    throw new RuntimeError(expr.name,
	        "Only instances have properties.");
	}
	
	@Override
	public Object visitLogical(Expr.Logical expr) {
		Object left = evaluate(expr.left);
	    if (expr.operator.type == OR) {
	    		if (truthy(left)) return left;
	    } else {
	    		if (!truthy(left)) return left;
	    }
	    return evaluate(expr.right);
	}
	
	@Override
	public Object visitAssign(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		environment.assign(expr.name, value);
		return value;
	}
	
	@Override
	public Object visitLiteral(Expr.Literal expr) {
		return expr.value;
	}
	
	@Override
	public Object visitVariable(Expr.Variable expr) {
		return lookUpVariable(expr.name, expr);
	}
	
	@Override
	public Object visitGrouping(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}
	
	@Override
	public Object visitArray(Expr.Array expr) {
		return new Array(expr.elements.stream()
				.map(this::evaluate)
				.collect(Collectors.toList()));
	}
	
	@Override
	public Object visitUnary(Expr.Unary expr) {
		Object right = evaluate(expr.right);
		switch(expr.operator.type) {
		case MINUS:
		{
			return -(double)right;
		}
		case BANG:
		{
			return !truthy(right);
		}
		}
		return null;
	}
	
	@Override
	public Object visitBinary(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		switch(expr.operator.type) {
		case BANG_EQUAL:
		{
			return !isEqual(left, right);
		}
	    case EQUAL_EQUAL:
	    {
	    		return isEqual(left, right);
	    }
		case GREATER:
		{
			checkNumberOperands(expr.operator, left, right);
	        return (double)left > (double)right;
		}
	    case GREATER_EQUAL:
	    {
	    		checkNumberOperands(expr.operator, left, right);
	        return (double)left >= (double)right;
	    }
	    case LESS:
	    {
	    		checkNumberOperands(expr.operator, left, right);
	        return (double)left < (double)right;
	    }
	    case LESS_EQUAL:
	    {
	    		checkNumberOperands(expr.operator, left, right);
	        return (double)left <= (double)right;
	    }
		case PLUS:
		{
	        if (left instanceof Double && right instanceof Double) {
	        		return (double)left + (double)right;
	        } 
	        if (left instanceof String && right instanceof String) {
	        		return (String)left + (String)right;
	        }
	        throw new RuntimeError(expr.operator,
	                "Operands must be two numbers or two strings.");
		}
		case MINUS:
		{
			checkNumberOperand(expr.operator, right);
			return (double)left - (double)right;
		}
		case SLASH:
		{
			checkNumberOperands(expr.operator, left, right);
			return (double)left / (double)right;
		}
		case STAR:
		{
			checkNumberOperands(expr.operator, left, right);
			return (double)left * (double)right;
		}
		}
		return null;
	}
	
	// Helpers
	
	private void checkNumberOperand(TToken operator, Object operand) {
	    if (operand instanceof Double) return;
	    throw new RuntimeError(operator, "Operand must be a number.");
	}
	
	private void checkNumberOperands(TToken operator,
            Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers.");
	}
	
	private void execute(Stmt stmt) {
	    stmt.accept(this);
	}
	
	private Object lookUpVariable(TToken name, Expr expr) {
	    Integer distance = locals.get(expr);
	    if (distance != null) {
	    		return environment.getAt(distance, name.lexeme);
	    } else {
	    		return globals.get(name);
	    }
	}
	
	public void executeBlock(List<Stmt> statements,
			Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;
			for(Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}
	
	private String getNameFromPath(String name) {
		String str = name.replaceAll("\\.\\w+", "");
		return str;
	}
	
	private boolean isEqual(Object a, Object b) {
	    // nil is only equal to nil
	    if (a == null && b == null) return true;
	    if (a == null) return false;

	    return a.equals(b);
	}
	
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	
	private void processImport(String path) {
		if(Files.isRegularFile(Paths.get(path))) {
			try {
				FileReader file = 
						new FileReader(path);
				BufferedReader reader =
						new BufferedReader(file);
				String line = null;
				StringBuilder builder =
						new StringBuilder("");
				while((line = reader.readLine()) != null) {
					builder.append(line + " ");
				}
				reader.close();
				TScanner scanner = new TScanner(builder.toString());
				List<TToken> toks = scanner.scan();
				if(Tau.hadError)
					return;
				Parser parse = new Parser(toks);
				List<Stmt> statements = parse.parseTokens();
				if(Tau.hadError)
					return;
				Analyzer ana = new Analyzer(this);
				ana.resolve(statements);
				if(Tau.hadError)
					return;
				this.interpret(statements);
				if(Tau.hadError)
					return;
			} catch (IOException e) {
				Error.error(0, "Failed to process import.");
			}
		}
	}
	
	private boolean truthy(Object object) {
		if(object == null) return false;
		if(object instanceof Boolean) return (boolean)object;
		return true;
	}
	
	public static String stringify(Object object) {
	    if (object == null) return "nil";
	    if (object instanceof Double) {
	    		String text = object.toString();
	    		if (text.endsWith(".0")) {
	    			text = text.substring(0, text.length() - 2);
	    		}
	    		return text;
	    }
	    return object.toString();
	}
}
