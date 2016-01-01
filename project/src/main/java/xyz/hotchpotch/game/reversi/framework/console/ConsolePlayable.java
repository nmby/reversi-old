package xyz.hotchpotch.game.reversi.framework.console;

import xyz.hotchpotch.game.reversi.framework.Playable;
import xyz.hotchpotch.game.reversi.framework.Result;

/**
 * 標準入出力を用いた実行クラスを表します。<br>
 * {@link ConsoleGame}､{@link ConsoleMatch}､{@link ConsoleLeague} の基底インタフェースです。<br>
 * 
 * @param <P> ターゲット {@link Playable}
 * @since 1.0.0
 * @author nmby
 */
public interface ConsolePlayable<P extends Playable> extends Playable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 実行し、結果を返します。<br>
     * 
     * @return 実行結果
     */
    public Result<P> play();
}
