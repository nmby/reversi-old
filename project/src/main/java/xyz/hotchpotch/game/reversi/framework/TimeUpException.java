package xyz.hotchpotch.game.reversi.framework;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * プレーヤーの思考時間が制限時間を超過したことを表す例外です。<br>
 * 
 * @author nmby
 */
public class TimeUpException extends RuleViolationException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     */
    public TimeUpException(String message, Color violator) {
        super(message, violator);
    }
    
    /**
     * 例外をコピーして生成します。<br>
     * 
     * @param original 元の例外
     */
    TimeUpException(TimeUpException original) {
        super(original.getMessage(), original.violator, original.getCause());
    }
}
