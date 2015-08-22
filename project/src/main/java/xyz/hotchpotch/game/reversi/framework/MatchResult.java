package xyz.hotchpotch.game.reversi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
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
     * マッチ実施条件とゲーム結果からマッチ結果を生成します。<br>
     * 
     * @param matchCondition マッチ実施条件
     * @param gameResults ゲーム実施条件A, Bそれぞれにおけるゲーム結果が格納された {@code Map}
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
    
    /** マッチ実施条件 */
    public final MatchCondition matchCondition;
    
    /** 勝者（引き分けの場合は {@code null}） */
    public final Entrant winner;
    
    /** プレーヤーA, Bそれぞれの対戦成績が格納された {@code Map} */
    public final Map<Entrant, ResultCount> resultCounts;
    
    private final String description;
    
    private MatchResult(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        this.matchCondition = matchCondition;
        
        Map<Entrant, ResultCount> resultCounts = new EnumMap<>(Entrant.class);
        for (Entrant entrant : Entrant.values()) {
            List<GameResult> blackResults = gameResults.get(entrant);
            List<GameResult> whiteResults = gameResults.get(entrant.opposite());
            
            int win = (int) blackResults.stream().filter(r -> r.winner == Color.BLACK).count()
                    + (int) whiteResults.stream().filter(r -> r.winner == Color.WHITE).count();
            int draw = (int) blackResults.stream().filter(r -> r.winner == null).count()
                    + (int) whiteResults.stream().filter(r -> r.winner == null).count();
            int lose = (int) blackResults.stream().filter(r -> r.winner == Color.WHITE).count()
                    + (int) whiteResults.stream().filter(r -> r.winner == Color.BLACK).count();
            
            resultCounts.put(entrant, new ResultCount(win, draw, lose));
        }
        this.resultCounts = Collections.unmodifiableMap(resultCounts);
        
        ResultCount countA = resultCounts.get(Entrant.A);
        if (countA.lose < countA.win) {
            winner = Entrant.A;
        } else if (countA.win < countA.lose) {
            winner = Entrant.B;
        } else {
            winner = null;
        }
        
        description = String.format("%s:%s, %s:%s\t>> %s %sの勝ち:%d, %sの勝ち:%d, 引き分け:%d ",
                Entrant.A, matchCondition.playerClasses.get(Entrant.A).getSimpleName(),
                Entrant.B, matchCondition.playerClasses.get(Entrant.B).getSimpleName(),
                winner == null ? "引き分けです。" : winner + "の勝ちです。",
                Entrant.A, countA.win, Entrant.B, countA.lose, countA.draw);
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
