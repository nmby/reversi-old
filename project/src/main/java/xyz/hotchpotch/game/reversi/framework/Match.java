package xyz.hotchpotch.game.reversi.framework;

/**
 * 2プレーヤーが黒白交代しながら複数回対戦する、マッチを表します。<br>
 * 複数のゲームを束ねたものです。<br>
 * 
 * @see Game
 * @see League
 * @author nmby
 */
public interface Match extends Playable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * マッチの対戦者を表す列挙型です。<br>
     */
    static enum Entrant {
        A, B;
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
}
