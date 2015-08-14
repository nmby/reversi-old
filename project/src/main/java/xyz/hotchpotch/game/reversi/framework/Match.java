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
        A,
        B;
        
        /**
         * 自身と反対の要素を返します。<br>
         * 
         * @return 自身と反対の要素
         */
        public Entrant opposite() {
            return this == A ? B : A;
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
}
