package Parser_Java11;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


/**
 * The Syntax Analyzer.
 */
class Parser {

// The lexer which will provide the tokens
    private final LexicalAnalyzer lexer;

    // the actual "code generator"
    private final CodeGenerator codeGenerator;

    /**
     * The constructor initializes the terminal literals in their vectors.
     *
     * @param lexer The Lexer Object
     */
    public Parser(LexicalAnalyzer lexer, CodeGenerator codeGenerator) {
        this.lexer = lexer;
        this.codeGenerator = codeGenerator;
    }

    /**
     * Since the "Compiler" portion of the code knows nothing about the start rule, the "analyze" method
     * must invoke the start rule.
     *
     * Begin analyzing...
     */
    public void analyze() {
        try {
            // Generate header for our output
            var startNode = codeGenerator.writeHeader("PARSE TREE");

            // THIS IS OUR START RULE
            // TODO: Change if necessary!
            PROGRAM(startNode);

            // generate footer for our output
            codeGenerator.writeFooter();

        } catch (ParseException ex) {
            final String msg = String.format("%s\n", ex.getMessage());
            Logger.getAnonymousLogger().severe(msg);
        }
    }

    // <PROGRAM> ::= <STMT_LIST> <$$> 
    protected void PROGRAM(final ParseNode fromNode) throws ParseException {
        final var nodeName = codeGenerator.addNonTerminalToTree(fromNode);

        STMT_LIST(nodeName);
        EOS(nodeName);
    }

    // <STMT_LIST> ::= <STMT> <STMT_LIST> | <EPSILON>
    void STMT_LIST(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(TOKEN.ID) || lexer.isCurrentToken(TOKEN.IF) || lexer.isCurrentToken(TOKEN.READ) || lexer.isCurrentToken(TOKEN.WHILE) || lexer.isCurrentToken(TOKEN.WRITE)) {
            STMT(treeNode);
            STMT_LIST(treeNode);
        } else {
            EMPTY(treeNode);
        }
    }

    // <STMT> ::= <ID> := <EXPR> | <READ> <ID> | <WRITE> <EXPR> | <IF> <CONDITION> <DO> <STMT_LIST> <FI> | <WHILE> <CONDITION> <DO> <STMT_LIST> <OD>
    void STMT(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if(lexer.isCurrentToken(TOKEN.ID)){
            ID(treeNode);
            EQUAL(treeNode);
            EXPR(treeNode);
        } else if(lexer.isCurrentToken(TOKEN.READ)) {
            READ(treeNode);
            ID(treeNode);
        } else if(lexer.isCurrentToken(TOKEN.WRITE)){
            WRITE(treeNode);
            EXPR(treeNode);
        } else if(lexer.isCurrentToken(TOKEN.IF)) {
            IF(treeNode);
            CONDITION(treeNode);
            THEN(treeNode);
            STMT_LIST(treeNode);
            FI(treeNode);
        } else {
            WHILE(treeNode);
            CONDITION(treeNode);
            DO(treeNode);
            STMT_LIST(treeNode);
            OD(treeNode);
        }
    }

    // <EXPR> ::= <TERM> <TERM_TAIL> 
    void EXPR(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        TERM(treeNode);
        TERM_TAIL(treeNode);
    }

    // <TERM_TAIL> ::= <ADD_OP> <TERM> <TERM_TAIL> | <EPSILON>
    void TERM_TAIL(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(TOKEN.ADD_OP)) {
            ADD_OP(treeNode);
            TERM(treeNode);
            TERM_TAIL(treeNode);
        } else {
            EMPTY(treeNode);
        }
    }
    
    // <TERM> ::= <FACTOR> <FACTOR_TAIL>
    void TERM(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        FACTOR(treeNode);
        FACTOR_TAIL(treeNode);
    }

    // <FACTOR_TAIL> ::= <MULT_OP> <FACTOR> <FACTOR_TAIL> | <EMPTY>
    void FACTOR_TAIL(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(TOKEN.MULT_OP)) {
            MULT_OP(treeNode);
            FACTOR(treeNode);
            FACTOR_TAIL(treeNode);
        } else {
            EMPTY(treeNode);
        }
    }
    
    // <FACTOR> ::= <LPAREN> <EXPR> <RPAREN> | <ID> | <NUMBER>
    void FACTOR(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(TOKEN.LPAREN)) {
            LPAREN(treeNode);
            EXPR(treeNode);
            RPAREN(treeNode);
        } else if (lexer.isCurrentToken(TOKEN.NUMBER)) {
            NUMBER(treeNode);
        } else {
            ID(treeNode);
        }
    }
    
    // <CONDITION> ::= <EXPR> <RELATION> <EXPR>
    void CONDITION(final ParseNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        EXPR(treeNode);
        RELATION(treeNode);
        EXPR(treeNode);
    }
    /////////////////////////////////////////////////////////////////////////////////////
    // For the sake of completeness, each terminal-token has its own method,
    // though they all do the same thing here.  In a "REAL" program, each terminal
    // would likely have unique code associated with it.
    /////////////////////////////////////////////////////////////////////////////////////
    void EMPTY(final ParseNode fromNode) throws ParseException {
        codeGenerator.addEmptyToTree(fromNode);
    }

    // <EOS>
    void EOS(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.EOF)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("End of Program", fromNode);
        }
    }

    // <ADD_OP>
    void ADD_OP(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.ADD_OP)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("an Addition/Subtraction Operator", fromNode);
        }
    }

    // <MULT_OP> 
    void MULT_OP(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.MULT_OP)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Multiplication/Division Operator", fromNode);
        }
    }

    // <RELATION> 
    void RELATION(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.RELATION)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Relation Operator", fromNode);
        }
    }

    // <ID> 
    void ID(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.ID)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("an ID", fromNode);
        }
    }

    // <EQUAL>
    void EQUAL(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.EQUAL)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a :=", fromNode);
        }
    }

    // <NUMBER>
    void NUMBER(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.NUMBER)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Number", fromNode);
        }
    }

    // <READ>
    void READ(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.READ)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Read", fromNode);
        }
    }

    // <WHILE>
    void WHILE(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.WHILE)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a While", fromNode);
        }
    }
    
    // <WRITE>
    void WRITE(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.WRITE)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Write", fromNode);
        }
    }
    
    // <IF>
    void IF(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.IF)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("an If", fromNode);
        }
    }
    
    // <THEN>
    void THEN(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.THEN)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Then", fromNode);
        }
    }
    
    // <FI>
    void FI(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.FI)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Fi", fromNode);
        }
    }
    
    // <DO>
    void DO(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.DO)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Do", fromNode);
        }
    }
    
    // <OD>
    void OD(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.OD)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("an Od", fromNode);
        }
    }

    // <LPAREN>
    void LPAREN(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.LPAREN)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Left Parentheses", fromNode);
        }
    }
    
    // <RPAREN>
    void RPAREN(final ParseNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(TOKEN.RPAREN)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("a Right Parentheses", fromNode);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Terminal:
    // Test its type and continue if we really have a terminal node, syntax error if fails.
    void addTerminalAndAdvanceToken(final ParseNode fromNode) throws ParseException {
        final var currentTerminal = lexer.getCurrentToken();
        final var terminalNode = codeGenerator.addNonTerminalToTree(fromNode, String.format("<%s>", currentTerminal));

        codeGenerator.addTerminalToTree(terminalNode, lexer.getCurrentLexeme());
        lexer.advanceToken();
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // The code below this point is just a bunch of "helper functions" to keep 
    // the parser code (above) a bit cleaner.
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //
    // Handle all the errors in one place for cleaner parser code.
    private void raiseException(String expected, ParseNode fromNode) throws ParseException {
        final var template = "SYNTAX ERROR: '%s' was expected but '%s' was found.";
        final var err = String.format(template, expected, lexer.getCurrentLexeme());
        codeGenerator.syntaxError(err, fromNode);
    }
}

/**
 * All the Tokens/Terminals Used by the parser. The purpose of the enum type
 * here is to eliminate the need for direct string comparisons which is generally
 * slow, as being difficult to maintain. (We want Java's "static type checking"
 * to do as much work for us as it can!)
 *
 * !!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!
 * -----------------------------------------------------------------------------
 * IN MOST REAL CASES, THERE WILL BE ONLY ONE LEXEME PER TOKEN. !!!
 * -----------------------------------------------------------------------------
 * !!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!
 *
 * The fact that several lexemes exist per token in this example is because this
 * is to parse simple English sentences where most of the token types have many
 * words (lexemes) that could fit. This is generally NOT the case in most
 * programming languages!!!
 */
enum TOKEN {
    ID,
    NUMBER("0","1","2","3","4","5","6","7","8","9"),
    EMPTY, // Epsilon
    KILL("$$"),
    EQUAL(":="),
    READ("read"),
    WRITE("write"),
    ADD_OP("+", "-"),
    MULT_OP("*", "/"),
    LPAREN("("),
    RPAREN(")"),
    IF("if"),
    THEN("then"),
    FI("fi"),
    WHILE("while"),
    DO("do"),
    OD("od"),
    EOF,
    RELATION("<", ">", "<=", ">=", "=", "!=");
    
    /* ARTICLE("a", "the"),
    CONJUNCTION("and", "or"),
    NOUN("dog", "cat", "rat", "house", "tree"),
    VERB("loves", "hates", "eats", "chases", "stalks"),
    ADJECTIVE("fast", "slow", "furry", "sneaky", "lazy", "tall"),
    ADJ_SEP(","),
    ADVERB("quickly", "secretly", "silently"),
    PREPOSITION("of", "on", "around", "with", "up"),
    EOS(".", "!"),
    // THESE ARE NOT USED IN THE GRAMMAR, BUT MIGHT BE USEFUL...  :)
    EOF, // End of file
    OTHER, // Could be "ID" in a "real programming language"
    NUMBER; // A sequence of digits.
    */
    /**
     * A list of all lexemes for each token.
     */
    private final List<String> lexemeList;

    TOKEN(final String... tokenStrings) {
        lexemeList = new ArrayList<>(tokenStrings.length);
        lexemeList.addAll(Arrays.asList(tokenStrings));
    }

    /**
     * Get a TOKEN object from the Lexeme string.
     *
     * @param string The String (lexeme) to convert to a TOKEN
     *
     * @return A TOKEN object based on the input String (lexeme)
     */
    public static TOKEN fromLexeme(final String string) {
        // Just to be safe...
        final var lexeme = string.trim();

        // An empty string should mean no more tokens to process.
        if (lexeme.isEmpty()) {
            return EOF;
        }

        // Regex for one or more digits optionally followed by . and more digits. 
        // (doesn't handle "-", "+" etc., only digits)       
        if (lexeme.matches("\\d+(?:\\.\\d+)?")) {
            return NUMBER;
        }

        // Search through ALL lexemes looking for a match with early bailout.
        for (var token : TOKEN.values()) {
            if (token.lexemeList.contains(lexeme)) {
                // early bailout from for loop.
                return token;
            }
        }

        // NOTE: Other could represent an ID, for example.
        return ID;
    }
}
