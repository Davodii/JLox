package Lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Read a lox file and execute from the file system.
     * @param fileName
     * @throws IOException
     */
    private static void runFile(String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(fileName));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code;
        if (hadError) System.exit(65);
    }

    /**
     * Open a prompt that allows you to enter and execute code.
     * I.e. start a REPL.
     * @throws IOException
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break; // Close the REPL with a null line.
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error
        if (hadError) return;

        System.out.println(new AstPrinter().print(expression));
    }

    //TODO: A better error system would be to have an actual abstraction, like a "ErrorReporter".
    static void error(int line, String message) {
        report(line, "", message);
    }

    //TODO: Show where in the line the error occured.
    private static void report(int line, String where, String message) {
        System.err.println("[line" +line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", " " + message);
        }
    }
}
