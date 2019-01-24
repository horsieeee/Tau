package tau.ast;

import tau.literal.TToken;
import java.util.*;

public abstract class Stmt {
	public interface Visitor<R> {
		R visitExpression(Stmt.Expression stmt);
		R visitDebug(Stmt.Debug stmt);
		R visitVar(Stmt.Var stmt);
		R visitBlock(Stmt.Block stmt);
		R visitIf(Stmt.If stmt);
		R visitWhile(Stmt.While stmt);
		R visitFunction(Stmt.Function stmt);
		R visitReturn(Stmt.Return stmt);
		R visitModule(Stmt.Module stmt);
		R visitMap(Stmt.Map stmt);
		R visitMapValue(Stmt.MapValue stmt);
		R visitImport(Stmt.Import stmt);
	}
	
	public static class Expression extends Stmt {
		public Expression(Expr expression) {
			this.expression = expression;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpression(this);
		}
		
		public final Expr expression;
	}
	
	public static class Debug extends Stmt {
	    public Debug(Expr expression) {
	    		this.expression = expression;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	      return visitor.visitDebug(this);
	    }

	    public final Expr expression;
	}
	
	public static class Var extends Stmt {
	    public Var(TToken name, Expr initializer) {
	    		this.name = name;
	    		this.initializer = initializer;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitVar(this);
	    }

	    public final TToken name;
	    public final Expr initializer;
	}
	
	public static class Block extends Stmt {
		public Block(List<Stmt> statements) {
			this.statements = statements;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlock(this);
		}
		
		public final List<Stmt> statements;
	}
	
	public static class If extends Stmt {
		public If(Expr condition,
				Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIf(this);
		}
		
		public final Expr condition;
		public final Stmt thenBranch;
		public final Stmt elseBranch;
	}
	
	public static class While extends Stmt {
	    public While(Expr condition, 
	    		Stmt body) {
	    		this.condition = condition;
	    		this.body = body;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	      return visitor.visitWhile(this);
	    }

	    public final Expr condition;
	    public final Stmt body;
	}
	
	public static class Function extends Stmt {
	    public Function(TToken name, 
	    		Expr.Function expr) {
	    		this.name = name;
	    		this.expr = expr;
	    }
	    
		public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitFunction(this);
	    }

	    public final TToken name;
	    public final Expr.Function expr;
	}
	
	public static class Return extends Stmt {
	    public Return(TToken keyword, 
	    		Expr value) {
	    		this.keyword = keyword;
	    		this.value = value;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	      return visitor.visitReturn(this);
	    }

	    public final TToken keyword;
	    public final Expr value;
	}
	
	public static class Module extends Stmt {
	    public Module(TToken name, 
	    		List<Stmt.Function> methods) {
	    		this.name = name;
	    		this.methods = methods;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitModule(this);
	    }

	    public final TToken name;
	    public final List<Stmt.Function> methods;
	}
	
	public static class Map extends Stmt {
	    public Map(TToken name, 
	    		List<Stmt.MapValue> values) {
	    		this.name = name;
	    		this.values = values;
	    }

	    public <R> R accept(Visitor<R> visitor) {
	    		return visitor.visitMap(this);
	    }

	    public final TToken name;
	    public final List<Stmt.MapValue> values;
	}
	
	public static class MapValue extends Stmt {
		public MapValue(TToken name,
				Expr value) {
			this.name = name;
			this.value = value;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitMapValue(this);
		}
		
		public final TToken name;
		public final Expr value;
	}
	
	public static class Import extends Stmt {
		public Import(Expr.Literal expr) {
			this.expr = expr;
		}
		
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitImport(this);
		}
		
		public final Expr.Literal expr;
	}
	
	public abstract <R> R accept(Visitor<R> visitor);
}
