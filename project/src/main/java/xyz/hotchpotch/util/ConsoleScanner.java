package xyz.hotchpotch.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ConsoleScanner<T> implements Supplier<T> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    public static class Builder<T> {
        private final Predicate<String> judge;
        private final Function<String, T> converter;
        private String prompt;
        private String rePrompt;
        
        private Builder(
                Predicate<String> judge,
                Function<String, T> converter,
                String prompt,
                String rePrompt) {
                
            this.judge = judge;
            this.converter = converter;
            this.prompt = prompt;
            this.rePrompt = rePrompt;
        }
        
        private Builder(
                Predicate<String> judge,
                Function<String, T> converter,
                String prompt) {
                
            this(judge, converter, prompt, prompt);
        }
        
        public Builder<T> prompt(String prompt, String rePrompt) {
            this.prompt = Objects.requireNonNull(prompt);
            this.rePrompt = Objects.requireNonNull(rePrompt);
            return this;
        }
        
        public Builder<T> prompt(String prompt) {
            return prompt(prompt, prompt);
        }
        
        public ConsoleScanner<T> build() {
            return new ConsoleScanner<T>(judge, converter, prompt, rePrompt);
        }
    }
    
    public static Builder<String> stringBuilder(Predicate<String> judge) {
        Objects.requireNonNull(judge);
        return new Builder<String>(
                judge,
                Function.identity(),
                "> ",
                "入力形式が不正です。再入力してください > ");
    }
    
    public static Builder<String> stringBuilder(Pattern pattern) {
        Objects.requireNonNull(pattern);
        return stringBuilder(pattern.asPredicate());
    }
    
    public static Builder<String> stringBuilder(String regex) {
        Objects.requireNonNull(regex);
        return stringBuilder(Pattern.compile(regex));
    }
    
    public static Builder<Integer> intBuilder(int lower, int upper) {
        if (upper < lower) {
            throw new IllegalArgumentException(String.format("lower=%d, upper=%d", lower, upper));
        }
        Predicate<String> judge = s -> {
            try {
                int n = Integer.parseInt(s);
                return lower <= n && n <= upper;
            } catch (NumberFormatException e) {
                return false;
            }
        };
        Function<String, Integer> converter = Integer::valueOf;
        String prompt = String.format("%d～%dの範囲で指定してください > ", lower, upper);
        return new Builder<Integer>(judge, converter, prompt);
    }
    
    public static <T> Builder<T> listBuilder(List<T> list) {
        Objects.requireNonNull(list);
        Predicate<String> judge = s -> {
            try {
                int idx = Integer.parseInt(s);
                return 0 <= idx && idx < list.size();
            } catch (NumberFormatException e) {
                return false;
            }
        };
        Function<String, T> converter = s -> {
            int idx = Integer.parseInt(s);
            return list.get(idx);
        };
        StringBuilder tmp = new StringBuilder();
        tmp.append("次の中から番号で指定してください。").append(BR);
        for (int i = 0; i < list.size(); i++) {
            tmp.append(String.format("\t%d : %s", i, list.get(i))).append(BR);
        }
        tmp.append("> ");
        String prompt = tmp.toString();
        return new Builder<T>(judge, converter, prompt);
    }
    
    public static <E extends Enum<E>> Builder<E> enumBuilder(Class<E> type) {
        Objects.requireNonNull(type);
        return listBuilder(Arrays.asList(type.getEnumConstants()));
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Predicate<String> judge;
    private final Function<String, T> converter;
    private final String prompt;
    private final String rePrompt;
    
    private ConsoleScanner(
            Predicate<String> judge,
            Function<String, T> converter,
            String prompt,
            String rePrompt) {
            
        this.judge = judge;
        this.converter = converter;
        this.prompt = prompt;
        this.rePrompt = rePrompt;
    }
    
    @Override
    public T get() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print(prompt);
            String str = sc.nextLine();
            while (!judge.test(str)) {
                System.out.print(rePrompt);
                str = sc.nextLine();
            }
            return converter.apply(str);
        }
    }
}
