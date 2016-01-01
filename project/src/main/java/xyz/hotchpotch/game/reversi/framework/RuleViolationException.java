package xyz.hotchpotch.game.reversi.framework;

import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * リバーシのルールに違反したことを表す基底例外です。<br>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class RuleViolationException extends Exception {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /** ルールに違反したプレーヤーの色 */
    public final Color violator;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param cause {@link Player} 実装クラスがスローした例外
     * @throws NullPointerException {@code violator} が {@code null} の場合
     */
    public RuleViolationException(String message, Color violator, Throwable cause) {
        super(message, cause);
        this.violator = Objects.requireNonNull(violator);
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @throws NullPointerException {@code violator} が {@code null} の場合
     */
    public RuleViolationException(String message, Color violator) {
        super(message);
        this.violator = Objects.requireNonNull(violator);
    }
    
    /**
     * 例外をコピーして生成します。<br>
     * 
     * @param original 元の例外
     * @throws NullPointerException {@code original} が {@code null} の場合
     */
    /*package*/ RuleViolationException(RuleViolationException original) {
        super(original.getMessage(), original.getCause());
        violator = original.violator;
    }
}
