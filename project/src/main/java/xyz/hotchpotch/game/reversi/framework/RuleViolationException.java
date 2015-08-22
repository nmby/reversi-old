package xyz.hotchpotch.game.reversi.framework;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * リバーシのルールに違反したことを表す基底例外です。<br>
 * 
 * @author nmby
 */
public class RuleViolationException extends Exception {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /** ルールに違反したプレーヤーの色 */
    public final Color violator;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param cause プレーヤーが投げた例外
     */
    public RuleViolationException(String message, Color violator, Throwable cause) {
        super(message, cause);
        this.violator = violator;
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     */
    public RuleViolationException(String message, Color violator) {
        super(message);
        this.violator = violator;
    }
    
    /**
     * 例外をコピーして生成します。<br>
     * 
     * @param original 元の例外
     */
    RuleViolationException(RuleViolationException original) {
        super(original.getMessage(), original.getCause());
        violator = original.violator;
    }
}
