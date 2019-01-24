package tau.parser;

import java.util.*;
import tau.literal.*;
import tau.ast.*;
import tau.error.Error;
import static tau.literal.TType.*;

public class Parser {
	private static class ParseError extends RuntimeException {}
	private final List<TToken> tokens;
	private int current = 0;
	
	public Parser(
			List<TToken> tokens) {
		this.tokens = tokens;
	}
	
	public List<Stmt> parseTokens() {
		List<Stmt> statements = new ArrayList<>();
	    while (!atEnd()) {
	    		statements.add(baseDecl());
	    }
	    return statements;
	}
	
	private Stmt baseDecl() {
		try {
			if(match(LET))
				return varDecl();
			if (check(DEFFUN) && checkNext(IDENTIFIER)) {
				consume(DEFFUN, null);
				return function("function");
			}
			if (match(DEFM)) 
				return moduleDecl();
			if (match(DEFMAP))
				return mapDecl();
			return baseStmt();
		} catch(ParseError e) {
			sync();
			return null;
		}
	}
	
	private Stmt.Function function(String kind) {
		  TToken name = consume(IDENTIFIER, "Expect " + kind + " name.");
		  return new Stmt.Function(name, functionBody(kind));
	}

	private Expr.Function functionBody(String kind) {
		  consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		  List<TToken> parameters = new ArrayList<>();
		  if (!check(RIGHT_PAREN)) {
		    do {
		      if (parameters.size() >= 8) {
		        parsingError(peek(), "Cannot have more than 8 parameters.");
		      }

		      parameters.add(consume(IDENTIFIER, "Expect parameter name."));
		    } while (match(COMMA));
		  }
		  consume(RIGHT_PAREN, "Expect ')' after parameters.");

		  consume(DO, "Expect 'do' before " + kind + " body.");
		  List<Stmt> body = blockStmt();
		  return new Expr.Function(parameters, body);
	}
	
	private Stmt varDecl() {
		TToken name = consume(IDENTIFIER, "Expect variable name.");
	    Expr initializer = null;
	    if (match(EQUAL)) {
	      initializer = baseExpr();
	    }
	    return new Stmt.Var(name, initializer);
	}
	
	private Stmt baseStmt() {
		if(match(DEBUG)) 
			return debugStmt();
		if(match(DO)) 
			return new Stmt.Block(blockStmt());
		if(match(IF)) 
			return ifStmt();
		if(match(WHILE)) 
			return whileStmt();
		if(match(RETURN)) 
			return returnStmt();
		if(match(IMPORT))
			return importStmt();
		return exprStmt();
	}
	
	private Stmt moduleDecl() {
		TToken name = consume(IDENTIFIER, "Expected module name.");
		consume(DO, "Expected 'do' before module body.");
		List<Stmt.Function> methods = new ArrayList<>();
	    while (!check(END) && !atEnd()) {
	    		methods.add(function("method"));
	    }
	    consume(END, "Expect 'end' after module body.");
	    return new Stmt.Module(name, methods);
	}
	
	private Stmt mapDecl() {
		TToken name = consume(IDENTIFIER, "Expect name of map.");
		consume(DO, "Expected 'do' before map body.");
		List<Stmt.MapValue> values =
				new ArrayList<>();
		while(!check(END) && !atEnd()) {
			values.add(mapValue());
		}
		consume(END, "Expect 'end' after map body.");
		return new Stmt.Map(name, values);
	}
	
	private Stmt.MapValue mapValue() {
		TToken name = consume(IDENTIFIER, "Expected map value name.");
		consume(COLON, "Expect ':' after name.");
		Expr value = baseExpr();
		return new Stmt.MapValue(name, value);
	}
	
	private Stmt importStmt() {
		Expr expr = baseExpr();
		if(expr instanceof Expr.Literal &&
				((Expr.Literal)expr).value instanceof String) {
			return new Stmt.Import((Expr.Literal)expr);
		}
		Error.importError("Cannot use non-strings in imports.");
		return null;
	}
	
	private Stmt whileStmt() {
	    consume(LEFT_PAREN, "Expect '(' after 'while'.");
	    Expr condition = baseExpr();
	    consume(RIGHT_PAREN, "Expect ')' after condition.");
	    Stmt body = baseStmt();
	    return new Stmt.While(condition, body);
	}
	
	private Stmt ifStmt() {
		consume(LEFT_PAREN, "Expect '(' before condition.");
		Expr condition = baseExpr();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
		Stmt thenBranch = baseStmt();
		Stmt elseBranch = null;
		if(match(ELSE)) {
			elseBranch = baseStmt();
		}
		return new Stmt.If(condition, thenBranch, elseBranch);
	}
	
	private List<Stmt> blockStmt() {
		List<Stmt> statements = new ArrayList<>();
		while(!check(END) && !atEnd()) {
			statements.add(baseDecl());
		}
		consume(END, "Expect '}' after block statement.");
		return statements;
	}
	
	private Stmt debugStmt() {
		Expr value = baseExpr();
	    return new Stmt.Debug(value);
	}
	
	private Stmt returnStmt() {
		TToken keyword = previous();
		Expr value = null;
		value = baseExpr();
	    return new Stmt.Return(keyword, value);
	}
	
	private Stmt exprStmt() {
		Expr expr = baseExpr();
	    return new Stmt.Expression(expr);
	}
	
	private Expr baseExpr() {
		return assignmentExpr(); // pass into next parse
	}
	
	private Expr arrayExpr() {
		List<Expr> elements = new ArrayList<>();
		if(!check(RIGHT_BRACK)) {
			do {
				elements.add(baseExpr());
			} while(match(COMMA));
		}
		TToken brack = consume(RIGHT_BRACK, 
				"Expect ']' after list.");
		return new Expr.Array(brack, elements);
	}
	
	private Expr assignmentExpr() {
		Expr expr = orExpr();
		if(match(EQUAL)) {
			TToken equals = previous();
			Expr value = assignmentExpr();
			if(expr instanceof Expr.Variable) {
				TToken name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name, value);
			}
			parsingError(peek(), "Invalid assignment target.");
		}
		return expr;
	}
	
	private Expr orExpr() {
		Expr expr = andExpr();
		while(match(OR)) {
			TToken operator = previous();
			Expr right = andExpr();
			expr = new Expr.Logical(expr, operator, right);
		}
		return expr;
	}
	
	private Expr andExpr() {
		Expr expr = equalityExpr();
		while(match(AND)) {
			TToken operator = previous();
			Expr right = equalityExpr();
			expr = new Expr.Logical(expr, operator, right);
		}
		return expr;
	}
	
	private Expr equalityExpr() {
		Expr expr = comparisonExpr();
		while(match(BANG_EQUAL, EQUAL_EQUAL)) {
			TToken operator = previous();
			Expr right = comparisonExpr();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}
	
	private Expr comparisonExpr() {
		Expr expr = additionExpr();
		while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			TToken operator = previous();
			Expr right = additionExpr();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}
	
	private Expr additionExpr() {
		Expr expr = multiplicationExpr();
		while(match(MINUS, PLUS)) {
			TToken operator = previous();
			Expr right = multiplicationExpr();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}
	
	private Expr multiplicationExpr() {
		Expr expr = unaryExpr();
		while(match(SLASH, STAR)) {
			TToken operator = previous();
			Expr right = unaryExpr();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}
	
	private Expr unaryExpr() {
		if(match(BANG, MINUS)) {
			TToken operator = previous();
			Expr right = unaryExpr();
			return new Expr.Unary(operator, right);
		}
		return callExpr();
	}
	
	private Expr callExpr() {
		Expr expr = primaryExpr();
		while(true) {
			if(match(LEFT_PAREN)) {
				expr = finishCallExpr(expr);
			} else if (match(DOT)) {
		        TToken name = consume(IDENTIFIER,
		            "Expect property name after '.'.");
		        expr = new Expr.Get(expr, name);
			} else {
				break;
			}
		}
		return expr;
	}
	
	private Expr finishCallExpr(Expr calleeExpr) {
		List<Expr> arguments = 
				new ArrayList<>();
		if(!check(RIGHT_PAREN)) {
			do {
				if(arguments.size() >= 32) {
					parsingError(peek(),
							"Can't have more than 32 arguments on a call.");
				}
				arguments.add(baseExpr());
			} while(match(COMMA));
		}
		TToken paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
		return new Expr.Call(calleeExpr, paren, arguments);
	}
	
	private Expr symbolExpr() {
		consume(IDENTIFIER, "Expected identifier after '@'.");
		return new Expr.Literal(previous().literal);
	}
	
	private Expr primaryExpr() {
		if(match(FALSE)) 
			return new Expr.Literal(false);
		if(match(TRUE)) 
			return new Expr.Literal(true);
		if(match(NONE)) 
			return new Expr.Literal(null);
		if(match(LEFT_BRACK))
			return arrayExpr();
		if(match(NUMBER, STRING)) {
			return new Expr.Literal(previous().literal);
		}
		if(match(IDENTIFIER)) {
		      return new Expr.Variable(previous());
		}
		if(match(AT)) {
			return symbolExpr();
		}
		if(match(LEFT_PAREN)) {
			Expr expr = baseExpr();
			consume(RIGHT_PAREN, "Expect ')' after grouping expression.");
			return new Expr.Grouping(expr);
		}
		if (match(DEFFUN)) 
			return functionBody("function");
		throw parsingError(peek(), "Expect expression.");
	}
	
	// Helpers
	
	private ParseError parsingError(TToken token,
			String message) {
		Error.error(token, message);
		return new ParseError();
	}
	
	private boolean match(TType... types) {
		for(TType type : types) {
			if(check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}
	
	private boolean check(TType type) {
		if(atEnd()) return false;
		return peek().type == type;
	}
	
	private boolean checkNext(TType tokenType) {
		if (atEnd()) return false;
		if (tokens.get(current + 1).type == EOF) return false;
		return tokens.get(current + 1).type == tokenType;
	}
	
	private boolean atEnd() {
		return peek().type == EOF;
	}
	
	private TToken peek() {
		return tokens.get(current);
	}
	
	private TToken previous() {
		return tokens.get(current - 1);
	}
	
	private TToken advance() {
		if(!atEnd()) current++;
		return previous();
	}
	
	private TToken consume(TType type,
			String message) {
		if(check(type)) return advance();
		throw parsingError(peek(), message);
	}
	
	private void sync() {
		advance();
		while(!atEnd()) {
			if(previous().type == SEMICOLON)
				return;
			switch(peek().type) {
			case DEFFUN:
			case DEFM:
			case DEFMAP:
			case DEFTYPE:
			case IF:
			case WHILE:
			case LET:
			case RETURN:
			{
				return;
			}
			}
			advance();
		}
	}
}
