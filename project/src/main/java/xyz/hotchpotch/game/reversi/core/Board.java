package xyz.hotchpotch.game.reversi.core;

import java.util.Map;

/**
 * リバーシ盤を表します。<br>
 * 
 * @author nmby
 */
public interface Board {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 指定された位置の駒の色を返します。
     * 駒が置かれていない場合は null を返します。<br>
     * 
     * @param point 盤上の位置
     * @return 指定された位置の駒の色
     */
    Color colorAt(Point point);
    
    /**
     * このリバーシ盤に指定された手を適用します。<br>
     * 適用のされ方は実装に依存します。
     * すなわち、リバーシのルールに従い周囲の駒をひっくりかえす実装もあり得れば、
     * 単に Map#put(key, value) のように振る舞う実装もあり得ます。<br>
     * 
     * @param move 適用する手
     */
    void apply(Move move);
    
    /**
     * 色ごとの駒の数が格納された Map を返します。<br>
     * 
     * @return 色ごとの駒の数が格納された Map
     */
    Map<Color, Integer> counts();
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * 文字列は、ログファイルへの出力等に便利な、改行を含まない単一行形式です。<br>
     * 
     * @return このリバーシ盤の文字列表現
     */
    @Override
    String toString();
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * 文字列は、人間にとって分かり易い、改行を含む次の形式です。<br>
     * <br>
     *   a b c d e f g h <br>
     * 1 ・・・・・・・・<br>
     * 2 ・・・・・・・・<br>
     * 3 ・・・・・・・・<br>
     * 4 ・・・○●・・・<br>
     * 5 ・・・●○・・・<br>
     * 6 ・・・・・・・・<br>
     * 7 ・・・・・・・・<br>
     * 8 ・・・・・・・・<br>
     * 
     * @return このリバーシ盤の文字列表現
     */
    String toStringKindly();
}
