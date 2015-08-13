package xyz.hotchpotch.game.reversi.framework;

import java.util.Properties;

/**
 * ゲーム等の実行条件を表します。<br>
 * 
 * @param <P> ターゲット {@code Playable}
 * @author nmby
 */
public interface Condition<P extends Playable> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 指定されたプロパティの値を返します。<br>
     * 
     * @param key プロパティ名
     * @return プロパティ値
     */
    String getProperty(String key);
    
    /**
     * 全条件が格納された {@list Properties} を返します。<br>
     * 
     * @return 全条件が格納された {@list Properties}
     */
    Properties getProperties();
    
    /**
     * この実行条件の文字列表現を返します。<br>
     * 人間にとって分かり易い、複数行形式の文字列です。<br>
     * 
     * @return この実行条件の文字列表現（複数行形式）
     */
    String toStringKindly();
    
    /**
     * この実行条件の文字列表現を返します。<br>
     * ログ出力等に便利な、単一行形式の文字列です。<br>
     * 
     * @return この実行条件の文字列表現（単一行形式）
     */
    String toStringInLine();
}
