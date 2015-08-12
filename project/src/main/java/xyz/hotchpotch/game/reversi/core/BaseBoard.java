package xyz.hotchpotch.game.reversi.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

// このクラスを Serializable にしてみようかとも思ったが、
// 自分の現状の理解では無理なので、今回は止める。
abstract class BaseBoard implements Board {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    final Map<Point, Color> map;
    
    BaseBoard() {
        map = new HashMap<>();
    }
    
    BaseBoard(Map<Point, Color> map) {
        assert map != null;
        this.map = new HashMap<>(map);
    }
    
    BaseBoard(Board board) {
        assert board != null;
        map = new HashMap<>();
        Map<Point, Color> wrapped = Collections.synchronizedMap(map);
        Point.stream().forEach(p -> wrapped.put(p, board.colorAt(p)));
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException point が null の場合
     */
    @Override
    public Color colorAt(Point point) {
        Objects.requireNonNull(point);
        return map.get(point);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Color, Integer> counts() {
        Map<Color, Integer> counts = new EnumMap<>(Color.class);
        int black = (int) Point.stream().filter(p -> map.get(p) == Color.BLACK).count();
        int white = (int) Point.stream().filter(p -> map.get(p) == Color.WHITE).count();
        counts.put(Color.BLACK, black);
        counts.put(Color.WHITE, white);
        return counts;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toStringKindly() {
        StringBuilder str = new StringBuilder();
        str.append("  ");
        
        for (int j = 0; j < Point.WIDTH; j++) {
            str.append('a' + j).append(" ");
        }
        str.append(BR);
        
        for (int i = 0; i < Point.HEIGHT; i++) {
            str.append(String.format("%-2d", i + 1));
            for (int j = 0; j < Point.WIDTH; j++) {
                str.append(Color.toString(map.get(Point.of(i, j))));
            }
            str.append(BR);
        }
        
        return str.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.join("",
                Stream.of(Point.values())
                .map(p -> Color.toString(map.get(p)))
                .toArray(String[]::new));
    }
}
