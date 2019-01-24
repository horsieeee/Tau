package tau.literal;

public class TToken {
	public final TType type;
	public final String lexeme;
	public final Object literal; // Literal could be anything, so defined as object
	public final int line;
	
	public TToken(TType type, String lexeme,
			Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}
}
