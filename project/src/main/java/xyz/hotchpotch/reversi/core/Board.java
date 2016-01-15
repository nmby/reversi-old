package xyz.hotchpotch.reversi.core;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * リバーシ盤を表すインタフェースです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public interface Board {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 2つのリバーシ盤が等しいかを返します。<br>
     * 各マスの石の状態が同じであるとき、もしくは2つのリバーシ盤がともに {@code null} のとき、2つのリバーシ盤は等しいと判定されます。<br>
     * 
     * @param board1 リバーシ盤1
     * @param board2 リバーシ盤2
     * @return 2つのリバーシ盤が等しいとき {@code true}
     * @see #equals(Object)
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(Board board1, Board board2) {
        if (board1 == null && board2 == null) {
            return true;
        }
        if (board1 == null || board2 == null) {
            return false;
        }
        return Point.stream().allMatch(p -> board1.colorAt(p) == board2.colorAt(p));
    }
    
    /**
     * リバーシ盤のハッシュコードを返します。<br>
     * リバーシ盤のハッシュコードは、{@link Point} クラスの自然順序付けに従って全てのマスの石（null を含む）を格納した一次元配列（{@link Color}[]）
     * のハッシュコードとして定義されます。<br>
     * {@code board} が {@code null} の場合は {@code 0} を返します。<br>
     * 
     * @param board リバーシ盤
     * @return {@code board} のハッシュコード（{@code null} の場合は {@code 0}）
     * @see #hashCode()
     * @see Arrays#hashCode(Object[])
     */
    public static int hashCode(Board board) {
        if (board == null) {
            return 0;
        }
        return Arrays.hashCode(Point.stream().sorted().map(board::colorAt).toArray(Color[]::new));
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 指定された位置の石の色を返します。
     * 石が置かれていない場合は {@code null} を返します。<br>
     * 
     * @param point 盤上の位置
     * @return 指定された位置の石の色（石が置かれていない場合は {@code null}）
     */
    Color colorAt(Point point);
    
    /**
     * このリバーシ盤に指定された手を適用します。<br>
     * <br>
     * 適用のされ方は実装に依存します。
     * すなわち、リバーシのルールに従い周囲の石をひっくり返す実装もあり得れば、
     * 単に {@link Map#put(Object, Object) Map.put(key, value)} のように振る舞う実装もあり得ます。<br>
     * 
     * @param move 適用する手
     */
    void apply(Move move);
    
    // 今回は「practice of java8」ということで、これ以降のメソッドをデフォルト実装してみた。
    // デフォルトメソッドは便利なんだけど、色々と混乱しそうな予感...
    // abstract な基底クラス（この場合 BaseBoard）を完全に代替出来るわけではないし。棲み分けをどうしたものか...
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * デフォルト実装では、人間にとって分かり易い、改行を含む次の形式です。<br>
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
        StringBuilder str = new StringBuilder("   ");
        
        for (int j = 0; j < Point.WIDTH; j++) {
            str.append(String.format("%c ", 'a' + j));
        }
        str.append(System.lineSeparator());
        
        for (int i = 0; i < Point.HEIGHT; i++) {
            str.append(String.format("%2c ", '1' + i));
            for (int j = 0; j < Point.WIDTH; j++) {
                str.append(Color.toString(colorAt(Point.of(i, j))));
            }
            str.append(System.lineSeparator());
        }
        
        return str.toString();
    }
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * デフォルト実装では、ログファイルへの出力等に便利な、改行を含まない次の単一行形式です。<br>
     * {@code ・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・}
     * 
     * @return このリバーシ盤の文字列表現（単一行形式）
     */
    default String toStringInLine() {
        return Point.stream().map(p -> Color.toString(colorAt(p))).collect(Collectors.joining());
    }
    
    /**
     * 指定されたオブジェクトがこのリバーシ盤と等しいかを返します。<br>
     * 指定されたオブジェクトもリバーシ盤であり、2つのリバーシ盤の各マスの石の状態が同じであるとき、2つのリバーシ盤は等しいと判定されます。<br>
     * 
     * @param o 比較対象のオブジェクト
     * @return 指定されたオブジェクトがこのリバーシ盤と等しい場合は {@code true}
     * @see #equals(Board, Board)
     */
    @Override
    boolean equals(Object o);
    
    /**
     * このリバーシ盤のハッシュコードを返します。<br>
     * リバーシ盤のハッシュコードは、{@link Point} クラスの自然順序付けに従って全てのマスの石（null を含む）を格納した一次元配列（{@link Color}[]）
     * のハッシュコードとして定義されます。<br>
     * 
     * @return このリバーシ盤のハッシュコード
     * @see #hashCode(Board)
     */
    @Override
    int hashCode();
}
