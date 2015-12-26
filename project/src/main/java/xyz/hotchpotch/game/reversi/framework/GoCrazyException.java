package xyz.hotchpotch.game.reversi.framework;

import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * {@link Player} 実装クラスにより実行時例外がスローされたことを表す例外です。<br>
 * 
 * @author nmby
 */
public class GoCrazyException extends RuleViolationException {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param cause プレーヤーが投げた例外
     * @throws NullPointerException {@code violator}、{@code cause} のいずれかが {@code null} の場合
     */
    public GoCrazyException(String message, Color violator, Throwable cause) {
        super(message, Objects.requireNonNull(violator), Objects.requireNonNull(cause));
    }
}
