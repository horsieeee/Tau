package tau;

import java.io.*;
import java.util.*;
import tau.scanner.*;
import tau.literal.*;
import tau.parser.*;
import tau.ast.*;
import tau.interpreter.*;
import tau.analyzer.*;

public class Tau {
	public static boolean hadError = false;
	public static boolean hadRuntimeError = false;
	public static String[] arguments;
	private static final Interpreter interpreter = 
			new Interpreter();
	
	public static void main(String[] args) throws IOException {
		if(args.length > 1 && !args[0].endsWith(".tau")) {
			System.out.println("Usage: tau ?FILE");
		} else if(args.length > 1 && args[0].endsWith(".tau")) {
			arguments = args;
			runFile(args[0]);
		} else if(args.length == 0) {
			arguments = null;
			runPrompt();
		}
	}
	
	private static void runPrompt() throws IOException {
		InputStreamReader reader = new InputStreamReader(System.in);
		BufferedReader buff = new BufferedReader(reader);
		for(;;) {
			System.out.println("> ");
			run(buff.readLine());
		}
	}
	
	private static void runFile(String filePath) {
		StringBuilder sb = 
				new StringBuilder("");
		String line = null;
        try {
            FileReader fileReader = 
                new FileReader(filePath);
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                sb.append(line + " ");
            }   
            bufferedReader.close();         
        } catch(IOException e) {
        		System.exit(1);
        }
        run(sb.toString());
        if(hadError)
        		System.exit(65);
        if(hadRuntimeError)
        		System.exit(70);
	}
	
	private static void run(String source) {
		TScanner scanner = new TScanner(source);
		List<TToken> tok = scanner.scan();
		Parser parse = new Parser(tok);
		List<Stmt> statements = parse.parseTokens();
		if(hadError)
			return;
		Analyzer ana = new Analyzer(interpreter);
		ana.resolve(statements);
		if(hadError)
			return;
		interpreter.interpret(statements);
		if(hadError)
			return;
	}
}
