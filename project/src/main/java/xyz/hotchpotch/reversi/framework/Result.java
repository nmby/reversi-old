package xyz.hotchpotch.reversi.framework;

/**
 * {@link Playable} の実行結果を表します。<br>
 * 
 * @param <P> ターゲット {@code Playable}
 * @since 2.0.0
 * @author nmby
 */
public interface Result<P extends Playable> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 対戦成績を表す不変クラスです。<br>
     * 
     * @since 2.0.0
     * @author nmby
     */
    public static class ResultCount {
        
        /** 勝ち数 */
        public final int win;
        
        /** 引き分け数 */
        public final int draw;
        
        /** 負け数 */
        public final int lose;
        
        /*package*/ ResultCount(int win, int draw, int lose) {
            this.win = win;
            this.draw = draw;
            this.lose = lose;
        }
        
        /*package*/ ResultCount sum(ResultCount count2) {
            return new ResultCount(win + count2.win, draw + count2.draw, lose + count2.lose);
        }
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
}
