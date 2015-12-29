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
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * リーグにおける個々のマッチの組み合わせを表すための不変クラスです。<br>
     * 
     * @author nmby
     */
    public static class Pair {
        
        /**
         * @param idxA リーグ参加プレーヤーのリストにおける、プレーヤーAのインデックス
         * @param idxB リーグ参加プレーヤーのリストにおける、プレーヤーBのインデックス
         * @return プレーヤーA, B の組み合わせを表す {@code Pair} オブジェクト
         */
        public static Pair of(int idxA, int idxB) {
            return new Pair(idxA, idxB);
        }
        
        /*package*/ final int idxA;
        /*package*/ final int idxB;
        
        private Pair(int idxA, int idxB) {
            this.idxA = idxA;
            this.idxB = idxB;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Pair) {
                Pair other = (Pair) o;
                return idxA == other.idxA && idxB == other.idxB;
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(idxA, idxB);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("(%d, %d)", idxA, idxB);
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
}
