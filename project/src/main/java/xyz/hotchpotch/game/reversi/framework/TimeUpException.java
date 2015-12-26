package xyz.hotchpotch.game.reversi.framework;

import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * プレーヤーの思考時間が制限時間を超過したことを表す例外です。<br>
 * 
 * @author nmby
 */
public class TimeUpException extends RuleViolationException {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @throws NullPointerException {@code violator} が {@code null} の場合
     */
    public TimeUpException(String message, Color violator) {
        super(message, Objects.requireNonNull(violator));
    }
    
    /**
     * 例外をコピーして生成します。<br>
     * 
     * @param original 元の例外
     * @throws NullPointerException {@code original} が {@code null} の場合
     */
    /*package*/ TimeUpException(TimeUpException original) {
        super(original.getMessage(), original.violator, original.getCause());
    }
}
