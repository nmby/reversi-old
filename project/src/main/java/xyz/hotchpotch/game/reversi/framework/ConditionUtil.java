package xyz.hotchpotch.game.reversi.framework;

import java.util.Map;

/**
 * {@link Condition} に関係する共通的な処理を集めたユーティリティクラスです。<br>
 * 
 * @author nmby
 */
/*package*/ class ConditionUtil {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /*package*/ static String getValue(Map<String, String> map, String key) {
        assert map != null;
        assert key != null;
        
        String value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "必須パラメータが指定されていません。" + "%s=%s", key, value));
        }
        return value;
    }
    
    /*package*/ static Class<? extends Player> getPlayerClass(Map<String, String> map, String key) {
        assert map != null;
        assert key != null;
        
        String playerClassName = getValue(map, key);
        assert playerClassName != null;
        
        return getPlayerClass(playerClassName);
    }
    
    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends Player> getPlayerClass(String playerClassName) {
        assert playerClassName != null;
        
        try {
            return (Class<? extends Player>) Class.forName(playerClassName);
            
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format(
                    "プレーヤークラスをロードできません。Class=%s", playerClassName), e);
                    
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "プレーヤークラスは %s を実装する必要があります。Class=%s", Player.class.getName(), playerClassName), e);
        }
    }
    
    /*package*/ static long getLongPositiveValue(Map<String, String> map, String key) {
        assert map != null;
        assert key != null;
        
        String str = getValue(map, key);
        long value;
        
        try {
            value = Long.parseLong(str);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "整数値が必要です。%s=%s", key, str));
        }
        if (value <= 0) {
            throw new IllegalArgumentException(String.format(
                    "正の整数値が必要です。%s=%d", key, value));
        }
        return value;
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private ConditionUtil() {
    }
}
