package xyz.hotchpotch.game.reversi.core;

/**
 * リバーシのルールに違反したことを示す基底例外です。<br>
 * 
 * @author nmby
 */
// この例外を Exception とするか RuntimeException とするかは悩みどころだが
// 一旦 RuntimeException にしてみた。
// Exception にすると、Board#apply(Move) の宣言に "throws RuleViolationException" を
// 付けなければならず、それはどうにもおかしいような気がしたため。
public class RuleViolationException extends RuntimeException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public RuleViolationException(String message) {
        super(message);
    }
    
    public RuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RuleViolationException(Throwable cause) {
        super(cause);
    }
}
