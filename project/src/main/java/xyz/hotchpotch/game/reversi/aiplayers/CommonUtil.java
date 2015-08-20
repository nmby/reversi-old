package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;

class CommonUtil {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * パラメータチェック等を省き必要最小限の機能に絞った軽量リバーシ盤。<br>
     */
    static class LightweightBoard implements Board {
        
        final Map<Point, Color> map;
        
        LightweightBoard(LightweightBoard board) {
            map = new HashMap<>(board.map);
        }
        
        LightweightBoard(Board board) {
            map = new HashMap<>();
            for (Point p : Point.values()) {
                map.put(p, board.colorAt(p));
            }
        }
        
        @Override
        public Color colorAt(Point point) {
            return map.get(point);
        }
        
        /**
         * {@inheritDoc}
         * 
         * ルールに基づく防御的なチェックは省く。
         * クライアント側でルール妥当性を保証する必要がある。
         */
        @Override
        public void apply(Move move) {
            assert move != null;
            assert move.point != null;
            assert Rule.canApply(this, move);
            
            Set<Point> reversibles = Rule.reversibles(this, move);
            for (Point p : reversibles) {
                map.put(p, move.color);
            }
            map.put(move.point, move.color);
        }
    }
    
    /**
     * {@link GameCondition} オブジェクトから目的のパラメータを取得するためのメソッド。<br>
     * 
     * @param gameCondition ゲーム条件
     * @param key パラメータ名
     * @param converter パラメータ値を目的の型に変換するための {@code Function}
     * @param defaultValue ゲーム条件に目的のパラメータが含まれない場合のためのデフォルト値
     * @return 目的のパラメータ値
     */
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
