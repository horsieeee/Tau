package tau.analyzer;

import java.util.*;
import tau.ast.*;
import tau.interpreter.*;
import tau.literal.*;
import tau.error.*;
import tau.error.Error;

public class Analyzer implements Expr.Visitor<Void>, 
Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes =
			new Stack<>();
	private FunctionType currentFunction =
			FunctionType.NONE;
	
	private enum FunctionType {
	    NONE,
	    FUNCTION,
	    METHOD
	}
	
	public Analyzer(Interpreter interpreter) {
		this.interpreter = interpreter;
	}
	
	public void resolve(List<Stmt> statements) {
	    for (Stmt statement : statements) {
	    		resolve(statement);
	    }
	}
	
	@Override
	public Void visitBlock(Stmt.Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}
	
	@Override
	public Void visitFunction(Expr.Function expr) {
		return null;
	}
	
	@Override
	public Void visitVariable(Expr.Variable expr) {
	    if (!scopes.isEmpty() &&
	        scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
	    		Error.error(expr.name,
	          "Cannot read local variable in its own initializer.");
	    }
	    resolveLocal(expr, expr.name);
	    return null;
	}
	
	@Override
	public Void visitVar(Stmt.Var stmt) {
		declare(stmt.name);
		if(stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}
	
	@Override
	public Void visitFunction(Stmt.Function stmt) {
	    declare(stmt.name);
	    define(stmt.name);
	    resolveFunction(stmt, FunctionType.FUNCTION);
	    return null;
	}
	
	@Override
	public Void visitExpression(Stmt.Expression stmt) {
	    resolve(stmt.expression);
	    return null;
	}
	
	@Override
	public Void visitIf(Stmt.If stmt) {
	    resolve(stmt.condition);
	    resolve(stmt.thenBranch);
	    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
	    return null;
	}
	
	@Override
	public Void visitDebug(Stmt.Debug stmt) {
	    resolve(stmt.expression);
	    return null;
	}
	
	@Override
	public Void visitReturn(Stmt.Return stmt) {
		if (currentFunction == FunctionType.NONE) {
		      Error.error(stmt.keyword, "Cannot return from top-level code, "
		      		+ "or outside of functions.");
		}
	    if (stmt.value != null) {
	    		resolve(stmt.value);
	    }
	    return null;
	}
	
	@Override
	public Void visitModule(Stmt.Module stmt) {
	    declare(stmt.name);
	    define(stmt.name);
	    for (Stmt.Function method : stmt.methods) {
	        FunctionType declaration = FunctionType.METHOD;
	        resolveFunction(method, declaration); 
	    }
	    return null;
	}
	
	@Override
	public Void visitMap(Stmt.Map stmt) {
		declare(stmt.name);
		define(stmt.name);
		return null;
	}
	
	@Override
	public Void visitMapValue(Stmt.MapValue stmt) {
		return null;
	}

	@Override
	public Void visitWhile(Stmt.While stmt) {
	    resolve(stmt.condition);
	    resolve(stmt.body);
	    return null;
	}
	
	@Override
	public Void visitBinary(Expr.Binary expr) {
	    resolve(expr.left);
	    resolve(expr.right);
	    return null;
	}
	
	@Override
	public Void visitImport(Stmt.Import stmt) {
		return null;
	}
	
	@Override
	public Void visitAssign(Expr.Assign expr) {
	    resolve(expr.value);
	    resolveLocal(expr, expr.name);
	    return null;
	}
	
	@Override
	public Void visitArray(Expr.Array expr) {
		expr.elements.forEach(
				this::resolve);
		return null;
	}
	
	@Override
	public Void visitCall(Expr.Call expr) {
	    resolve(expr.callee);
	    for (Expr argument : expr.arguments) {
	      resolve(argument);
	    }
	    return null;
	}
	
	@Override
	public Void visitGrouping(Expr.Grouping expr) {
	    resolve(expr.expression);
	    return null;
	}
	
	@Override
	public Void visitLiteral(Expr.Literal expr) {
	    return null;
	}
	
	@Override
	public Void visitGet(Expr.Get expr) {
	    resolve(expr.object);
	    return null;
	}
	
	@Override
	public Void visitLogical(Expr.Logical expr) {
	    resolve(expr.left);
	    resolve(expr.right);
	    return null;
	}
	
	@Override
	public Void visitUnary(Expr.Unary expr) {
	    resolve(expr.right);
	    return null;
	}
	
	// Helpers
	
	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}
	
	private void resolve(Expr expr) {
	    expr.accept(this);
	}
	
	private void resolveLocal(Expr expr, 
			TToken name) {
	    for (int i = scopes.size() - 1; i >= 0; i--) {
	    		if (scopes.get(i).containsKey(name.lexeme)) {
	    			interpreter.resolve(expr, scopes.size() - 1 - i);
	    			return;
	    		}
	    }
	    // Not found, assume is global
	}
	
	private void resolveFunction(Stmt.Function function,
			FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
	    currentFunction = type;
	    beginScope();
	    for (TToken param : function.expr.parameters) {
	    		declare(param);
	    		define(param);
	    }
	    resolve(function.expr.body);
	    endScope();
	    currentFunction = enclosingFunction;
	}
	
	private void declare(TToken name) {
	    if (scopes.isEmpty()) return;
	    Map<String, Boolean> scope = scopes.peek();
	    if (scope.containsKey(name.lexeme)) {
	        	Error.error(name,
	            "Variable with this name already declared in this current"
	            + " scope.");
	      }
	    scope.put(name.lexeme, false);
	}
	
	private void define(TToken name) {
	    if (scopes.isEmpty()) return;
	    scopes.peek().put(name.lexeme, true);
	}
	
	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}
	
	private void endScope() {
		scopes.pop();
	}
}
