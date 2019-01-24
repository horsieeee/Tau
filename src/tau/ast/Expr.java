package tau.ast;

import tau.ast.Stmt.Visitor;
import tau.literal.*;
import java.util.*;

public abstract class Expr {
	public interface Visitor<R> {
		R visitBinary(Binary expr);
		R visitGrouping(Grouping expr);
		R visitLiteral(Literal expr);
		R visitUnary(Unary expr);
		R visitVariable(Variable expr);
		R visitAssign(Assign expr);
		R visitLogical(Logical expr);
		R visitCall(Call expr);
		R visitGet(Get expr);
		R visitArray(Array expr);
		R visitFunction(Function expr);
	}
	
	public static class Binary extends Expr {
		public Binary(Expr left, TToken operator,
				Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinary(this);
		}
		
		public final Expr left;
		public final TToken operator;
		public final Expr right;
	}
	
	public static class Function extends Expr {
	    public Function(
	    		List<TToken> parameters, 
	    		List<Stmt> body) {
	    		this.parameters = parameters;
	    		this.body = body;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitFunction(this);
	    }

	    public final List<TToken> parameters;
	    public final List<Stmt> body;
	}
	
	public static class Grouping extends Expr {
		public Grouping(Expr expression) {
			this.expression = expression;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGrouping(this);
		}
		
		public final Expr expression;
	}
	
	public static class Literal extends Expr {
		public Literal(Object value) {
			this.value = value;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteral(this);
		}
		
		public final Object value;
	}
	
	public static class Unary extends Expr {
		public Unary(TToken operator,
				Expr right) {
			this.operator = operator;
			this.right = right;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnary(this);
		}
		
		public final TToken operator;
		public final Expr right;
	}
	
	public static class Variable extends Expr {
	    public Variable(TToken name) {
	    		this.name = name;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitVariable(this);
	    }

	    public final TToken name;
	}
	
	public static class Assign extends Expr {
		public Assign(TToken name,
				Expr value) {
			this.name = name;
			this.value = value;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssign(this);
		}
		
		public final TToken name;
		public final Expr value;
	}
	
	public static class Logical extends Expr {
	    public Logical(Expr left, 
	    		TToken operator, Expr right) {
	    		this.left = left;
	    		this.operator = operator;
	    		this.right = right;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	      return visitor.visitLogical(this);
	    }

	    public final Expr left;
	    public final TToken operator;
	    public final Expr right;
	}
	
	public static class Call extends Expr {
	    public Call(Expr callee, 
	    		TToken paren, List<Expr> arguments) {
	    		this.callee = callee;
	    		this.paren = paren;
	    		this.arguments = arguments;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitCall(this);
	    }

	    public final Expr callee;
	    public final TToken paren;
	    public final List<Expr> arguments;
	}
	
	public static class Get extends Expr {
	    public Get(Expr object, TToken name) {
	    		this.object = object;
	    		this.name = name;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitGet(this);
	    }

	    public final Expr object;
	    public final TToken name;
	}
	
	public static class Array extends Expr {
		public Array(TToken brack,
				List<Expr> elements) {
			this.brack = brack;
			this.elements = elements;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitArray(this);
		}
		
		public final TToken brack;
		public final List<Expr> elements;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
