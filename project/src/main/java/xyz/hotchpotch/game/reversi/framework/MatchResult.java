package xyz.hotchpotch.game.reversi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * @param gameResults 黒白それぞれにとってのゲーム結果が格納された {@code Map}
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
    
    /** マッチ条件 */
    public final MatchCondition matchCondition;
    
    /** 勝者（引き分けの場合は {@code null}） */
    public final Entrant winner;
    
    /** プレーヤーA, Bそれぞれの勝利回数が格納された {@code Map}（{@code key=null} は引き分け回数） */
    public final Map<Entrant, Integer> wins;
    
    /** プレーヤーA, Bそれぞれの累計残り持ち時間（ミリ秒）が格納された {@code Map} */
    public final Map<Entrant, Long> totalRemainingMillisThroughMatch;
    
    private final String description;
    
    private MatchResult(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        this.matchCondition = matchCondition;
        
        int winA = (int) gameResults.get(Entrant.A).stream().filter(r -> r.winner == Color.BLACK).count()
                + (int) gameResults.get(Entrant.B).stream().filter(r -> r.winner == Color.WHITE).count();
        int winB = (int) gameResults.get(Entrant.A).stream().filter(r -> r.winner == Color.WHITE).count()
                + (int) gameResults.get(Entrant.B).stream().filter(r -> r.winner == Color.BLACK).count();
        int draw = (int) gameResults.get(Entrant.A).stream().filter(r -> r.winner == null).count()
                + (int) gameResults.get(Entrant.B).stream().filter(r -> r.winner == null).count();
                
        if (winB < winA) {
            winner = Entrant.A;
        } else if (winA < winB) {
            winner = Entrant.B;
        } else {
            winner = null;
        }
        
        Map<Entrant, Integer> wins = new HashMap<>();
        wins.put(Entrant.A, winA);
        wins.put(Entrant.B, winB);
        wins.put(null, draw);
        this.wins = Collections.unmodifiableMap(wins);
        
        // どうやら Collectors#summingLong は ArithmeticException を投げないらしい... まぁいいや...
        // しかし酷い変数名だ...
        long totalRemainingMillisThroughMatchA = gameResults.get(Entrant.A).stream()
                .collect(Collectors.summingLong(r -> r.remainingMillisInGame.get(Color.BLACK)))
                + gameResults.get(Entrant.B).stream()
                        .collect(Collectors.summingLong(r -> r.remainingMillisInGame.get(Color.WHITE)));
                        
        long totalRemainingMillisThroughMatchB = gameResults.get(Entrant.A).stream()
                .collect(Collectors.summingLong(r -> r.remainingMillisInGame.get(Color.WHITE)))
                + gameResults.get(Entrant.B).stream()
                        .collect(Collectors.summingLong(r -> r.remainingMillisInGame.get(Color.BLACK)));
                        
        Map<Entrant, Long> totalRemainingMillisThroughMatch = new EnumMap<>(Entrant.class);
        totalRemainingMillisThroughMatch.put(Entrant.A, totalRemainingMillisThroughMatchA);
        totalRemainingMillisThroughMatch.put(Entrant.B, totalRemainingMillisThroughMatchB);
        this.totalRemainingMillisThroughMatch = Collections.unmodifiableMap(totalRemainingMillisThroughMatch);
        
        description = String.format("%s:%s, %s:%s\t>> %s %sの勝ち:%d, %sの勝ち:%d, 引き分け:%d "
                + "（累計残り時間 %s:%d ms, %s:%d ms）",
                Entrant.A, matchCondition.playerClasses.get(Entrant.A).getSimpleName(),
                Entrant.B, matchCondition.playerClasses.get(Entrant.B).getSimpleName(),
                winner == null ? "引き分けです。" : winner + "の勝ちです。",
                Entrant.A, winA, Entrant.B, winB, draw,
                Entrant.A, totalRemainingMillisThroughMatchA, Entrant.B, totalRemainingMillisThroughMatchB);
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
