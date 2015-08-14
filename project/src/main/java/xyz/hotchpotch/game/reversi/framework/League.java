package xyz.hotchpotch.game.reversi.framework;

import java.util.Objects;

/**
 * 複数のプレーヤーが総当たりでそれぞれ複数回対戦する、リーグを表します。<br>
 * 複数のマッチを束ねたものです。<br>
 * 
 * @see Game
 * @see Match
 * @author nmby
 */
public interface League extends Playable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * リーグにおける個々のマッチの組み合わせを表すための不変クラスです。<br>
     */
    static class Pair {
        
        public static Pair of(int idx1, int idx2) {
            return new Pair(idx1, idx2);
        }
        
        final int idx1;
        final int idx2;
        
        private Pair(int idx1, int idx2) {
            this.idx1 = idx1;
            this.idx2 = idx2;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof Pair) {
                Pair other = (Pair) o;
                return idx1 == other.idx1 && idx2 == other.idx2;
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(idx1, idx2);
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
}
