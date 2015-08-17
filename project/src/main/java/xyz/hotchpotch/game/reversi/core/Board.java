package xyz.hotchpotch.game.reversi.core;

import java.util.Map;
import java.util.stream.Stream;

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
     * 駒が置かれていない場合は {@code null} を返します。<br>
     * 
     * @param point 盤上の位置
     * @return 指定された位置の駒の色（駒が無い場合は {@code null}）
     */
    Color colorAt(Point point);
    
    /**
     * このリバーシ盤に指定された手を適用します。<br>
     * <br>
     * 適用のされ方は実装に依存します。
     * すなわち、リバーシのルールに従い周囲の駒をひっくりかえす実装もあり得れば、
     * 単に {@link Map#put(Object, Object) Map.put(key, value)} のように振る舞う実装もあり得ます。<br>
     * 
     * @param move 適用する手
     */
    void apply(Move move);
    
    // 今回は「practice of java8」ということで、これ以降のメソッドをデフォルト実装してみた。
    // デフォルトメソッドは便利なんだけど、色々と混乱しそうな予感...
    // BaseBoard を完全に代替出来るわけではないし。棲み分けをどうしたものか...
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * デフォルト実装においては、人間にとって分かり易い、改行を含む次の形式です。<br>
     * <pre>
     *   a b c d e f g h 
     * 1 ・・・・・・・・
     * 2 ・・・・・・・・
     * 3 ・・・・・・・・
     * 4 ・・・○●・・・
     * 5 ・・・●○・・・
     * 6 ・・・・・・・・
     * 7 ・・・・・・・・
     * 8 ・・・・・・・・
     * </pre>
     * 
     * @return このリバーシ盤の文字列表現（複数行形式）
     */
    default String toStringKindly() {
        StringBuilder str = new StringBuilder();
        str.append("  ");
        
        for (int j = 0; j < Point.WIDTH; j++) {
            str.append((char) ('a' + j)).append(" ");
        }
        str.append(System.lineSeparator());
        
        for (int i = 0; i < Point.HEIGHT; i++) {
            str.append(String.format("%-2d", i + 1));
            for (int j = 0; j < Point.WIDTH; j++) {
                str.append(Color.toString(colorAt(Point.of(i, j))));
            }
            str.append(System.lineSeparator());
        }
        
        return str.toString();
    }
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * デフォルト実装においては、ログファイルへの出力等に便利な、改行を含まない単一行形式です。<br>
     * 
     * @return このリバーシ盤の文字列表現（単一行形式）
     */
    default String toStringInLine() {
        return String.join("",
                Stream.of(Point.values())
                        .map(p -> Color.toString(colorAt(p)))
                        .toArray(String[]::new));
    }
}
