package xyz.hotchpotch.game.reversi.framework.console;

import java.util.Objects;

/**
 * 標準出力への簡易ロガーです。<br>
 * 
 * @author nmby
 */
// MEMO: java.util.logging の仲間たちについて要お勉強
// ここでは車輪の再発明をしちゃう。許して。
/*package*/ class ConsolePrinter {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * ログ出力レベルを表す列挙型です。<br>
     * 
     * @author nmby
     */
    public static enum Level {
        /** ゲームで出力すべきレベル */
        GAME,
        
        /** マッチで出力すべきレベル */
        MATCH,
        
        /** リーグで出力すべきレベル */
        LEAGUE;
        
        private boolean hasObligation(Level level) {
            return ordinal() <= level.ordinal();
        }
    }
    
    /**
     * ログ出力レベルを指定してロガーを生成します。<br>
     * 
     * @param level ログ出力レベル
     * @return ロガー
     * @throws NullPointerException {@code level} が {@code null} の場合
     */
    public static ConsolePrinter of(Level level) {
        return new ConsolePrinter(Objects.requireNonNull(level));
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /** このロガーのログ出力レベル */
    public final Level level;
    
    private ConsolePrinter(Level level) {
        assert level != null;
        
        this.level = level;
    }
    
    /**
     * 文字列を標準出力へ出力します。<br>
     * 但し、このロガーのログ出力レベルよりも {@code level} の詳細度が高い場合は出力しません。<br>
     * 
     * @param level ログ出力レベル
     * @param str 文字列
     * @throws NullPointerException {@code level} が {@code null} の場合
     */
    public void print(Level level, String str) {
        Objects.requireNonNull(level);
        if (this.level.hasObligation(level)) {
            System.out.print(str);
        }
    }
    
    /**
     * 文字列を標準出力へ出力し、改行します。<br>
     * 但し、このロガーのログ出力レベルよりも {@code level} の詳細度が高い場合は出力も改行もしません。<br>
     * 
     * @param level ログ出力レベル
     * @param str 文字列
     * @throws NullPointerException {@code level} が {@code null} の場合
     */
    public void println(Level level, String str) {
        print(level, str + System.lineSeparator());
    }
}
