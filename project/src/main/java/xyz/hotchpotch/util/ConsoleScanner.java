package xyz.hotchpotch.util;

import java.util.Objects;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ConsoleScanner implements Supplier<String> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String DEFAULT_PROMPT = "> ";
    private static final String DEFAULT_MSG = "入力形式が不正です。";
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Predicate<String> judge;
    private final String prompt;
    private final String rePrompt;
    
    public ConsoleScanner(Predicate<String> judge, String prompt, String rePrompt) {
        this.judge = Objects.requireNonNull(judge);
        this.prompt = Objects.requireNonNull(prompt);
        this.rePrompt = Objects.requireNonNull(rePrompt);
    }
    
    public ConsoleScanner(Predicate<String> judge, String prompt) {
        this(judge, prompt, DEFAULT_MSG + prompt);
    }
    
    public ConsoleScanner(Predicate<String> judge) {
        this(judge, DEFAULT_PROMPT);
    }
    
    public ConsoleScanner(Pattern pattern, String prompt, String rePrompt) {
        this(pattern.asPredicate(), prompt, rePrompt);
    }
    
    public ConsoleScanner(Pattern pattern, String prompt) {
        this(pattern.asPredicate(), prompt);
    }
    
    public ConsoleScanner(Pattern pattern) {
        this(pattern.asPredicate());
    }
    
    public ConsoleScanner(String regex, String prompt, String rePrompt) {
        this(Pattern.compile(regex), prompt, rePrompt);
    }
    
    public ConsoleScanner(String regex, String prompt) {
        this(Pattern.compile(regex), prompt);
    }
    
    public ConsoleScanner(String regex) {
        this(Pattern.compile(regex));
    }
    
    @Override
    public String get() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print(prompt);
            String str = sc.nextLine();
            while (!judge.test(str)) {
                System.out.print(rePrompt);
                str = sc.nextLine();
            }
            return str;
        }
    }
}
