package xyz.hotchpotch.game.reversi.framework;

import java.util.Map;

class ConditionUtil {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    static String getValue(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "必須パラメータが指定されていません。" + "%s=%s", key, value));
        }
        return value;
    }
    
    static Class<? extends Player> getPlayerClass(Map<String, String> map, String key) {
        String playerClassName = getValue(map, key);
        return getPlayerClass(playerClassName);
    }
    
    @SuppressWarnings("unchecked")
    static Class<? extends Player> getPlayerClass(String playerClassName) {
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
    
    static long getLongPositiveValue(Map<String, String> map, String key) {
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
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private ConditionUtil() {
    }
}
