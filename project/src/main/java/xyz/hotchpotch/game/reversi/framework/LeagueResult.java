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
 * @author nmby
 */
public class LeagueResult implements Result<League> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    /**
     * リーグ条件とマッチ結果からリーグ結果を生成します。<br>
     * 
     * @param leagueCondition リーグ条件
     * @param matchResults マッチ結果が格納された {@code Map}
     * @return リーグ結果
     * @throws NullPointerException {@code leagueCondition}、{@code matchResults} のいずれかが {@code null} の場合
     */
    public static LeagueResult of(LeagueCondition leagueCondition, Map<Pair, MatchResult> matchResults) {
        Objects.requireNonNull(leagueCondition);
        Objects.requireNonNull(matchResults);
        
        // TODO: 防御的コピーをとるレイヤと不変ビューをとるレイヤがクラスごとにバラついてる気がするので合わせる
        return new LeagueResult(leagueCondition, new HashMap<>(matchResults));
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final LeagueCondition leagueCondition;
    public final Map<Pair, Map<String, Integer>> counts;
    private final String description;
    
    private LeagueResult(LeagueCondition leagueCondition, Map<Pair, MatchResult> matchResults) {
        this.leagueCondition = leagueCondition;
        
        Map<Pair, Map<String, Integer>> counts = new HashMap<>();
        
        int num = leagueCondition.playerClasses.size();
        for (int idx1 = 0; idx1 < num - 1; idx1++) {
            for (int idx2 = idx1 + 1; idx2 < num; idx2++) {
                MatchResult matchResult = matchResults.get(Pair.of(idx1, idx2));
                for (Entrant entrant : Entrant.values()) {
                    Map<String, Integer> count = new HashMap<>();
                    count.put("win", matchResult.counts.get(entrant));
                    count.put("lose", matchResult.counts.get(entrant.opposite()));
                    count.put("draw", matchResult.counts.get(null));
                    counts.put(entrant == Entrant.A ? Pair.of(idx1, idx2) : Pair.of(idx2, idx1), count);
                }
            }
        }
        this.counts = Collections.unmodifiableMap(counts);
        
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
        for (int idx1 = 0; idx1 < num; idx1++) {
            str.append(String.format("%5s", "[" + (idx1 + 1) + "]"));
            int win = 0;
            int draw = 0;
            int lose = 0;
            
            for (int idx2 = 0; idx2 < num; idx2++) {
                if (idx1 == idx2) {
                    str.append(String.format("     -/  -/  -"));
                } else {
                    Map<String, Integer> count = counts.get(Pair.of(idx1, idx2));
                    int w = count.get("win");
                    int d = count.get("draw");
                    int l = count.get("lose");
                    str.append(String.format("   %3d/%3d/%3d", w, d, l));
                    win += w;
                    draw += d;
                    lose += l;
                }
            }
            str.append(String.format("     %4d/%4d/%4d", win, draw, lose)).append(BR);
        }
        
        description = str.toString();
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
