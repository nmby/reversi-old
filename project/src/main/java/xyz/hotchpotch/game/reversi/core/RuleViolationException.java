package xyz.hotchpotch.game.reversi.core;

/**
 * リバーシのルールに違反したことを示す基底例外です。<br>
 * 
 * @author nmby
 */
// この例外を Exception とするか RuntimeException とするかは悩みどころだが
// 一旦 RuntimeException にしてみた。
public class RuleViolationException extends RuntimeException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // 不変なので公開しちゃう
    public final Color violator;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     */
    public RuleViolationException(String message, Color violator) {
        super(String.format("%s violator=%s", message, violator));
        this.violator = violator;
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param violator ルールに違反したプレーヤーの色
     */
    public RuleViolationException(Color violator) {
        this("ルール違反が発生しました。", violator);
    }
}
