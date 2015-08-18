package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.function.Function;

import xyz.hotchpotch.game.reversi.framework.GameCondition;

class CommonUtil {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    static <T> T getParameter(
            GameCondition gameCondition,
            String key,
            Function<String, T> converter,
            T defaultValue) {
            
        String str = gameCondition.getParam(key);
        try {
            return converter.apply(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private CommonUtil() {
    }
}
