package xyz.hotchpotch.reversi.framework;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link Playable} の実施条件を表します。<br>
 * 
 * @param <P> ターゲット {@code Playable}
 * @since 2.0.0
 * @author nmby
 */
public interface Condition<P extends Playable> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 指定されたパラメータの値を返します。<br>
     * 
     * @param key パラメータ名
     * @return パラメータ値（パラメータが存在しない場合は {@code null}）
     * @throws NullPointerException {@code key} が {@code null} の場合
     */
    public default String getParam(String key) {
        Objects.requireNonNull(key);
        return getParams().get(key);
    }
    
    /**
     * 全パラメータが格納された {@code Map} を返します。<br>
     * 
     * @return 全パラメータが格納された {@code Map}
     */
    public Map<String, String> getParams();
    
    /**
     * この実施条件の文字列表現を返します。<br>
     * 人間にとって分かり易い、次の複数行形式の文字列です。<br>
     * <pre>
     * key1=value1
     * key2=value2
     * key3=value3
     * </pre>
     * 
     * @return この実施条件の文字列表現（複数行形式）
     */
    public default String toStringKindly() {
        return getParams().entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator();
    }
    
    /**
     * この実施条件の文字列表現を返します。<br>
     * ログ出力等に便利な、単一行形式の文字列です。<br>
     * 
     * @return この実施条件の文字列表現（単一行形式）
     */
    public default String toStringInLine() {
        return getParams().toString();
    }
}
