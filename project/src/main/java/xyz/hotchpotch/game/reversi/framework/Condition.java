package xyz.hotchpotch.game.reversi.framework;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link Playable} の実行条件を表します。<br>
 * 
 * @param <P> ターゲット {@code Playable}
 * @author nmby
 */
public interface Condition<P extends Playable> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 指定されたパラメータの値を返します。<br>
     * 
     * @param key パラメータ名
     * @return パラメータ値
     * @throws NullPointerException {@code key} が {@code null} の場合
     */
    default String getParam(String key) {
        Objects.requireNonNull(key);
        return getParams().get(key);
    }
    
    /**
     * 全パラメータが格納された {@list Map} を返します。<br>
     * 
     * @return 全パラメータが格納された {@list Map}
     */
    Map<String, String> getParams();
    
    /**
     * この実行条件の文字列表現を返します。<br>
     * 人間にとって分かり易い、複数行形式の文字列です。<br>
     * 
     * @return この実行条件の文字列表現（複数行形式）
     */
    default String toStringKindly() {
        return getParams().entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator();
    }
    
    /**
     * この実行条件の文字列表現を返します。<br>
     * ログ出力等に便利な、単一行形式の文字列です。<br>
     * 
     * @return この実行条件の文字列表現（単一行形式）
     */
    default String toStringInLine() {
        return getParams().toString();
    }
}
