package xyz.hotchpotch.reversi.aiplayers;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;
import xyz.hotchpotch.reversi.framework.GameCondition;
import xyz.hotchpotch.reversi.framework.Player;

/**
 * {@link Player} 実装クラスを作成する際に便利な機能を集めたユーティリティクラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class AIPlayerUtil {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * パラメータチェック等を省き必要最小限の機能に絞った、リバーシ盤の軽量な実装です。
     * {@link Player} 実装クラスの思考ロジックでシミュレーションを行う際などに便利です。<br>
     * このクラスのメソッドでは、処理速度を優先するために各種パラメータチェックを省略しています。
     * 各メソッドの API ドキュメントに従い、正しいパラメータ値のみを渡すようにしてください。
     * 違反した場合の処理結果は保証されません。<br>
     * <br>
     * この実装は同期されません。複数のスレッドから同じインスタンスを操作することはしないでください。<br>
     * 
     * @since 2.0.0
     * @author nmby
     */
    public static final class LightweightBoard implements Board {
        
        /** リバーシ盤の実体 */
        /*package*/ final Color[] colors;
        
        /**
         * 指定された {@code board} と同じ内容を持つ、新しい {@code LightweightBoard} を生成します。<br>
         * 
         * @param board 新しいリバーシ盤の内容を指定するリバーシ盤
         * @throws NullPointerException {@code board} が {@code null} の場合
         */
        public LightweightBoard(Board board) {
            assert board != null;
            
            if (board instanceof LightweightBoard) {
                LightweightBoard lBoard = (LightweightBoard) board;
                colors = Arrays.copyOf(lBoard.colors, Point.HEIGHT * Point.WIDTH);
            } else {
                colors = new Color[Point.HEIGHT * Point.WIDTH];
                Point.parallelStream().forEach(p -> {
                    colors[p.ordinal()] = board.colorAt(p);
                });
            }
        }
        
        /**
         * 指定された {@code map} と同じ内容を持つ、新しい {@code LightweightBoard} を生成します。<br>
         * 
         * @param map 新しいリバーシ盤の内容を指定する {@link Map}
         * @throws NullPointerException {@code map} が {@code null} の場合
         */
        public LightweightBoard(Map<Point, Color> map) {
            assert map != null;
            colors = new Color[Point.HEIGHT * Point.WIDTH];
            Point.parallelStream().forEach(p -> {
                colors[p.ordinal()] = map.get(p);
            });
        }
        
        /**
         * {@inheritDoc}
         * <br>
         * {@code point} が {@code null} の場合の挙動は保証されません。
         * {@code point} に {@code null} を指定しないでください。<br>
         */
        @Override
        public Color colorAt(Point point) {
            assert point != null;
            return colors[point.ordinal()];
        }
        
        /**
         * このリバーシ盤に指定された手を適用し、周囲の駒をひっくり返します。<br>
         * <br>
         * このメソッドでは、ルールに照らした手の妥当性チェックを行いません。次の規約を守ってください。
         * <ul>
         *   <li>ルールに反する手は指定しないでください。</li>
         *   <li>パスの手は指定しないでください。</li>
         * </ul>
         * これらの規約に反した場合の挙動は保証されません。<br>
         * 
         * @throws NullPointerException {@code move} が {@code null} の場合
         */
        @Override
        public void apply(Move move) {
            assert move != null;
            assert move.point != null;
            assert Rule.canApply(this, move);
            
            Rule.reversibles(this, move).forEach(p -> colors[p.ordinal()] = move.color);
            colors[move.point.ordinal()] = move.color;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof LightweightBoard) {
                return Arrays.equals(colors, ((LightweightBoard) o).colors);
            }
            if (o instanceof Board) {
                return Board.equals(this, (Board) o);
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(colors);
        }
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得して返します。<br>
     * <br>
     * このメソッドは、次の優先順で {@code gameCondition} オブジェクトからパラメータ値の取得を試みます。
     * <ol>
     *   <li>"<i>呼出元完全修飾クラス名.key</i>" というキーに紐付くパラメータ値</li>
     *   <li>"<i>key</i>" というキーに紐付くパラメータ値</li>
     * </ol>
     * <br>
     * 例えば、{@code xyz.hotchpotch.reversi.aiplayers.RandomAIPlayer} というクラスのコンストラクタ内で
     * {@code getParameter(gameCondition, "seed")} という呼び出しを実行した場合、このメソッドは次の順でパラメータの取得を試みます。
     * <ol>
     *   <li>"xyz.hotchpotch.reversi.aiplayers.RandomAIPlayer.seed" というキーに紐付くパラメータ値</li>
     *   <li>"seed" というキーに紐付くパラメータ値</li>
     * </ol>
     * <br>
     * パラメータ値が見つかった場合はその値を格納した {@code Optional} オブジェクトを、見つからなかった場合は空の {@code Optional} オブジェクトを返します。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return パラメータ値を格納した {@code Optional} オブジェクト（パラメータ値が存在しない場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<String> getParameter(GameCondition gameCondition, String key) {
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(key);
        
        Optional<String> caller = Stream.of(Thread.currentThread().getStackTrace())
                .skip(1)
                .map(StackTraceElement::getClassName)
                .filter(s -> !s.equals(AIPlayerUtil.class.getName()))
                .findFirst();
        Map<String, String> map = gameCondition.getParams();
        String value;
        
        if (caller.isPresent() && map.containsKey(caller.get() + "." + key)) {
            value = map.get(caller.get() + "." + key);
        } else if (map.containsKey(key)) {
            value = map.get(key);
        } else {
            value = null;
        }
        
        return Optional.ofNullable(value);
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、任意の型に変換して返します。<br>
     * パラメータ値の取得に関しては {@link #getParameter(GameCondition, String)} の説明を参照してください。<br>
     * このメソッドは、取得したパラメータ値を {@code converter} によって {@code String} から任意の型に変換します。<br>
     * 変換できた場合はその値を格納した {@code Optional} オブジェクトを、変換に失敗した場合は空の {@code Optional} オブジェクトを返します。<br>
     * 
     * @param <T> パラメータの変換後の型
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @param converter パラメータ値の型を変換する関数
     * @return 任意の型に変換されたパラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key}、{@code converter} のいずれかが {@code null} の場合
     */
    public static <T> Optional<T> getParameter(
            GameCondition gameCondition,
            String key,
            Function<String, ? extends T> converter) {
            
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(key);
        Objects.requireNonNull(converter);
        
        try {
            return getParameter(gameCondition, key).map(converter);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、{@link Integer} 型に変換して返します。<br>
     * 次のふたつの呼び出しは同値です。
     * <pre>
     *     getIntParameter(gameCondition, key);
     *     getParameter(gameCondition, key, Integer::valueOf);
     * </pre>
     * 詳細は {@link #getParameter(GameCondition, String, Function)} の説明を参照してください。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return {@code Integer} 型パラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<Integer> getIntParameter(GameCondition gameCondition, String key) {
        return getParameter(gameCondition, key, Integer::valueOf);
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、{@link Long} 型に変換して返します。<br>
     * 次のふたつの呼び出しは同値です。
     * <pre>
     *     getLongParameter(gameCondition, key);
     *     getParameter(gameCondition, key, Long::valueOf);
     * </pre>
     * 詳細は {@link #getParameter(GameCondition, String, Function)} の説明を参照してください。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return {@code Long} 型パラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<Long> getLongParameter(GameCondition gameCondition, String key) {
        return getParameter(gameCondition, key, Long::valueOf);
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、{@link Float} 型に変換して返します。<br>
     * 次のふたつの呼び出しは同値です。
     * <pre>
     *     getFloatParameter(gameCondition, key);
     *     getParameter(gameCondition, key, Float::valueOf);
     * </pre>
     * 詳細は {@link #getParameter(GameCondition, String, Function)} の説明を参照してください。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return {@code Float} 型パラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<Float> getFloatParameter(GameCondition gameCondition, String key) {
        return getParameter(gameCondition, key, Float::valueOf);
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、{@link Double} 型に変換して返します。<br>
     * 次のふたつの呼び出しは同値です。
     * <pre>
     *     getDoubleParameter(gameCondition, key);
     *     getParameter(gameCondition, key, Double::valueOf);
     * </pre>
     * 詳細は {@link #getParameter(GameCondition, String, Function)} の説明を参照してください。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return {@code Double} 型パラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<Double> getDoubleParameter(GameCondition gameCondition, String key) {
        return getParameter(gameCondition, key, Double::valueOf);
    }
    
    /**
     * {@link GameCondition} オブジェクトからパラメータ値を取得し、{@link Boolean} 型に変換して返します。<br>
     * 次のふたつの呼び出しは同値です。
     * <pre>
     *     getBooleanParameter(gameCondition, key);
     *     getParameter(gameCondition, key, Boolean::valueOf);
     * </pre>
     * 詳細は {@link #getParameter(GameCondition, String, Function)} の説明を参照してください。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param key パラメータのキー
     * @return {@code Boolean} 型パラメータ値を格納した {@code Optional} オブジェクト
     *         （パラメータ値が存在しない場合や型の変換に失敗した場合は空の {@code Optional} オブジェクト）
     * @throws NullPointerException {@code gameCondition}、{@code key} のいずれかが {@code null} の場合
     */
    public static Optional<Boolean> getBooleanParameter(GameCondition gameCondition, String key) {
        return getParameter(gameCondition, key, Boolean::valueOf);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private AIPlayerUtil() {
    }
}
