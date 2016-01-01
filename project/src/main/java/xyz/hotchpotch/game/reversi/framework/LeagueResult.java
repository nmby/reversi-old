package xyz.hotchpotch.game.reversi.framework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.framework.League.Pair;
import xyz.hotchpotch.game.reversi.framework.Match.Entrant;

/**
 * リーグの結果を表す不変クラスです。<br>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class LeagueResult implements Result<League> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    /**
     * リーグ実施条件と個々のマッチ結果からリーグ結果を生成します。<br>
     * 
     * @param leagueCondition リーグ実施条件
     * @param matchResults マッチ結果が格納された {@code Map}
     * @return リーグ結果
     * @throws NullPointerException {@code leagueCondition}、{@code matchResults} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code matchResults} の内容が不正な場合
     */
    public static LeagueResult of(
            LeagueCondition leagueCondition,
            Map<Pair, MatchResult> matchResults) {
            
        Objects.requireNonNull(leagueCondition);
        Objects.requireNonNull(matchResults);
        
        int num = leagueCondition.playerClasses.size();
        for (int idxA = 0; idxA < num - 1; idxA++) {
            for (int idxB = idxA + 1; idxB < num; idxB++) {
                Pair pair = Pair.of(idxA, idxB);
                
                if (!matchResults.containsKey(pair)) {
                    throw new IllegalArgumentException(String.format("マッチ結果が格納されていません。pair=%d", pair));
                }
            }
        }
        
        return new LeagueResult(leagueCondition, new HashMap<>(matchResults));
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** リーグ実施条件 */
    public final LeagueCondition leagueCondition;
    
    /** ペアごとの対戦成績が格納された {@code Map}（{@link Pair#idxA} から見た成績） */
    public final Map<Pair, ResultCount> counts;
    
    private final String description;
    
    private LeagueResult(LeagueCondition leagueCondition, Map<Pair, MatchResult> matchResults) {
        assert leagueCondition != null;
        assert matchResults != null;
        
        this.leagueCondition = leagueCondition;
        
        Map<Pair, ResultCount> counts = new HashMap<>();
        int num = leagueCondition.playerClasses.size();
        
        for (int idxA = 0; idxA < num - 1; idxA++) {
            for (int idxB = idxA + 1; idxB < num; idxB++) {
                MatchResult matchResult = matchResults.get(Pair.of(idxA, idxB));
                
                for (Entrant entrant : Entrant.values()) {
                    ResultCount count = matchResult.resultCounts.get(entrant);
                    counts.put(entrant == Entrant.A ? Pair.of(idxA, idxB) : Pair.of(idxB, idxA), count);
                }
            }
        }
        this.counts = Collections.unmodifiableMap(counts);
        
        // これ以降、表示用文字列の組み立て。
        // ↓↓↓ 文字列イメージ ↓↓↓
        // [1] xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer
        // [2] xyz.hotchpotch.game.reversi.aiplayers.RandomAIPlayer
        // [3] xyz.hotchpotch.game.reversi.aiplayers.DepthFirstAIPlayer
        // 
        // ＜対戦成績（勝/分/負）＞
        //         対 [1]        対 [2]        対 [3]          [total]
        //   [1]     -/  -/  -     2/  0/  8     5/  0/  5        7/   0/  13
        //   [2]     8/  0/  2     -/  -/  -     1/  0/  9        9/   0/  11
        //   [3]     5/  0/  5     9/  0/  1     -/  -/  -       14/   0/   6
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < num; i++) {
            str.append(String.format("[%d] %s", i + 1, leagueCondition.playerClasses.get(i).getName())).append(BR);
        }
        str.append(BR);
        str.append("＜対戦成績（勝/分/負）＞").append(BR);
        str.append("     ");
        for (int i = 0; i < num; i++) {
            str.append(String.format("   %-10s", "対 [" + (i + 1) + "]"));
        }
        str.append("     [total]").append(BR);
        
        for (int idxA = 0; idxA < num; idxA++) {
            str.append(String.format("%5s", "[" + (idxA + 1) + "]"));
            ResultCount total = new ResultCount(0, 0, 0);
            
            for (int idxB = 0; idxB < num; idxB++) {
                if (idxA == idxB) {
                    str.append(String.format("     -/  -/  -"));
                } else {
                    ResultCount count = counts.get(Pair.of(idxA, idxB));
                    str.append(String.format("   %3d/%3d/%3d", count.win, count.draw, count.lose));
                    total = total.sum(count);
                }
            }
            str.append(String.format("     %4d/%4d/%4d", total.win, total.draw, total.lose)).append(BR);
        }
        
        description = str.toString();
    }
    
    /**
     * このリーグ結果の文字列表現を返します。<br>
     * 
     * @return このリーグ結果の文字列表現
     */
    @Override
    public String toString() {
        return description;
    }
}
