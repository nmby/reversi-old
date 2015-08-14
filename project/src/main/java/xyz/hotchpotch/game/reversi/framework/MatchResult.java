package xyz.hotchpotch.game.reversi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.framework.Match.Entrant;

/**
 * マッチの結果を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class MatchResult implements Result<Match> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * マッチ条件とゲーム結果からマッチ結果を生成します。<br>
     * 
     * @param matchCondition マッチ条件
     * @param gameResults ゲーム結果が格納された {@code Map}
     * @return マッチ結果
     * @throws NullPointerException {@code matchCondition}、{@code gameResults} のいずれかが {@code null} の場合
     */
    public static MatchResult of(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        Objects.requireNonNull(matchCondition);
        Objects.requireNonNull(gameResults);
        
        Map<Entrant, List<GameResult>> copy = new EnumMap<>(Entrant.class);
        copy.put(Entrant.A, new ArrayList<>(gameResults.get(Entrant.A)));
        copy.put(Entrant.B, new ArrayList<>(gameResults.get(Entrant.B)));
        return new MatchResult(matchCondition, copy);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final MatchCondition matchCondition;
    public final Entrant winner;
    public final Map<Entrant, Integer> counts;
    private final String description;
    
    private MatchResult(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        this.matchCondition = matchCondition;
        
        int winA = 0;
        int winB = 0;
        int draw = 0;
        for (Entrant entrant : Entrant.values()) {
            List<GameResult> results = gameResults.get(entrant);
            for (GameResult result : results) {
                if ((entrant == Entrant.A && result.winner == Color.BLACK)
                        || (entrant == Entrant.B && result.winner == Color.WHITE)) {
                    winA++;
                } else if ((entrant == Entrant.A && result.winner == Color.WHITE)
                        || (entrant == Entrant.B && result.winner == Color.BLACK)) {
                    winB++;
                } else {
                    draw++;
                }
            }
        }
        if (winB < winA) {
            winner = Entrant.A;
        } else if (winA < winB) {
            winner = Entrant.B;
        } else {
            winner = null;
        }
        
        Map<Entrant, Integer> counts = new HashMap<>();
        counts.put(Entrant.A, winA);
        counts.put(Entrant.B, winB);
        counts.put(null, draw);
        this.counts = Collections.unmodifiableMap(counts);
        
        description = String.format("%s:%s, %s:%s\t>> %s %sの勝ち:%d, %sの勝ち:%d, 引き分け:%d",
                Entrant.A, matchCondition.playerClasses.get(Entrant.A).getSimpleName(),
                Entrant.B, matchCondition.playerClasses.get(Entrant.B).getSimpleName(),
                winner == null ? "引き分けです。" : winner + "の勝ちです。",
                Entrant.A, winA, Entrant.B, winB, draw);
    }
    
    /**
     * このマッチ結果の文字列表現を返します。<br>
     * 
     * @return このマッチ結果の文字列表現
     */
    @Override
    public String toString() {
        return description;
    }
}
