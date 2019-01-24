package tau.scanner;

import java.util.*;
import tau.literal.*;
import static tau.literal.TType.*;
import tau.error.*;
import tau.error.Error;

public class TScanner {
	public String source;
	private int start = 0;
	private int current = 0;
	private int line = 1;
	private final List<TToken> tokens = 
			new ArrayList<>();
	private static final Map<String, TType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("or", OR);
		keywords.put("true", TRUE);
		keywords.put("false", FALSE);
		keywords.put("let", LET);
		keywords.put("if", IF);
		keywords.put("else", ELSE);
		keywords.put("while", WHILE);
		keywords.put("def", DEFFUN);
		keywords.put("module", DEFM);
		keywords.put("type", DEFTYPE);
		keywords.put("map", DEFMAP);
		keywords.put("del", DEL);
		keywords.put("none", NONE);
		keywords.put("return", RETURN);
		keywords.put("debug", DEBUG);
		keywords.put("do", DO);
		keywords.put("end", END);
		keywords.put("match", MATCH);
		keywords.put("case", CASE);
		keywords.put("default", DEFAULT);
		keywords.put("import", IMPORT);
		keywords.put("enum", DEFENUM);
	}
	
	public TScanner(String source) {
		this.source = source;
	}
	
	public List<TToken> scan() {
		while(!atEnd()) {
			start = current;
			checkToken();
		}
		tokens.add(new TToken(EOF, "", null, line));
		return tokens;
	}
	
	// Helpers
	
	private void checkToken() {
		char token = advance();
		switch(token) {
		case '#':
		{
			break; // we ignore comments
		}
		case '{':
		{
			receiveToken(LEFT_BRACE);
			break;
		}
		case '}':
		{
			receiveToken(RIGHT_BRACE);
			break;
		}
		case '(':
		{
			receiveToken(LEFT_PAREN);
			break;
		}
		case ')':
		{
			receiveToken(RIGHT_PAREN);
			break;
		}
		case ',':
		{
			receiveToken(COMMA);
			break;
		}
		case '@':
		{
			receiveToken(AT);
			break;
		}
		case '.':
		{
			receiveToken(DOT);
			break;
		}
		case ':':
		{
			receiveToken(COLON);
			break;
		}
		case ';':
		{
			break;
		}
		case '+':
		{
			receiveToken(PLUS);
			break;
		}
		case '-':
		{
			receiveToken(MINUS);
			break;
		}
		case '/':
		{
			receiveToken(SLASH);
			break;
		}
		case '*':
		{
			receiveToken(STAR);
			break;
		}
		case '[':
		{
			receiveToken(match('[') ? DOUBLE_LBRACK : LEFT_BRACK);
			break;
		}
		case ']':
		{
			receiveToken(match(']') ? DOUBLE_RBRACK : RIGHT_BRACK);
			break;
		}
		case '!':
		{
			receiveToken(match('=') ? BANG_EQUAL : BANG);
			break;
		}
		case '=':
		{
			receiveToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		}
		case '>':
		{
			receiveToken(match('=') ? GREATER_EQUAL : EQUAL);
			break;
		}
		case '<':
		{
			receiveToken(match('=') ? LESS_EQUAL : EQUAL);
			break;
		}
		case ' ':
		case '\r':
		case '\t':
		{
			break;
		}
		case '\n':
		{
			line++;
			break;
		}
		case '"':
		{
			string();
			break;
		}
		default:
		{
			if(isDigit(token)) {
				number();
			} else if(isAlpha(token)) {
				identifier();
			} else {
				Error.error(line, "Character is unrecognized by Tau.");
			}
			break;
		}
		}
	}
	
	private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        // See if the identifier is a reserved word
        String text = source.substring(start, current);
        TType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        receiveToken(type);
	}
	
	private void number() {
		while(isDigit(peek()))
			advance();
		if(peek() == '.' && isDigit(peekNext())) {
			advance();
			while(isDigit(peek()))
				advance();
		}
		receiveToken(NUMBER,
				Double.parseDouble(source.substring(
						start, current)));
	}
	
	private void string() {
        // TODO: handle escape sequences, i.e. \n etc
        while (peek() != '"' && !atEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (atEnd()) {
            Error.error(line, "Unterminated string.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        receiveToken(STRING, value);
    }
	
	private boolean atEnd() {
		return current >= source.length();
	}
	
	private boolean match(char ex) {
		if(atEnd()) return false;
		if(source.charAt(current) != ex) return false;
		current++;
		return true;
	}
	
	private char advance() {
		current++;
		return source.charAt(current - 1); // return previous token
	}
	
	private char peek() {
		if(atEnd()) return '\0';
		return source.charAt(current);
	}
	
	private char peekNext() {
		if(current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}
	
	private boolean isAlpha(char ex) {
		return (ex >= 'a' && ex <= 'z') ||
				(ex >= 'A' && ex <= 'Z') ||
				(ex == '_');
	}
	
	private boolean isDigit(char ex) {
		return ex >= '0' && ex <= '9';
	}
	
	private boolean isAlphaNumeric(char ex) {
		return isAlpha(ex) || isDigit(ex);
	}
	
	private void receiveToken(TType type) {
		receiveToken(type, null);
	}
	
	private void receiveToken(TType type,
			Object literal) {
		String t = source.substring(start, current); // get lexeme of token
		tokens.add(new TToken(type, t, literal, line));
	}
}
