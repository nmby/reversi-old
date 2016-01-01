package xyz.hotchpotch.game.reversi.framework;

import java.util.Objects;

/**
 * 複数のプレーヤーが総当たりでそれぞれ複数回対戦する、リーグを表します。<br>
 * 複数のマッチを束ねたものです。<br>
 * 
 * @see Game
 * @see Match
 * @since 1.0.0
 * @author nmby
 */
public interface League extends Playable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * リーグにおける個々のマッチの組み合わせを表すための不変クラスです。<br>
     * 
     * @since 1.0.0
     * @author nmby
     */
    public static class Pair {
        
        /**
         * {@code Pair} オブジェクトを生成します。<br>
         * 
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
         * 指定されたオブジェクトがこのオブジェクトと等しいかを返します。<br>
         * 指定されたオブジェクトも {@code Pair} であり、ペアの内容が同じであるとき、2つのペアは等しいと判定されます。<br>
         * 
         * @param o 比較対象のオブジェクト
         * @return 指定されたオブジェクトがこのペアと等しい場合は {@code true}
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
         * このペアのハッシュコードを返します。<br>
         * 
         * @return このペアのハッシュコード
         */
        @Override
        public int hashCode() {
            return Objects.hash(idxA, idxB);
        }
        
        /**
         * このペアの文字列表現を返します。<br>
         * 
         * @return このペアの文字列表現
         */
        @Override
        public String toString() {
            return String.format("(%d, %d)", idxA, idxB);
        }
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
}
