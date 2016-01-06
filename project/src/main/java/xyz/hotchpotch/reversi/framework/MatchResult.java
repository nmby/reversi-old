package xyz.hotchpotch.reversi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.framework.Match.Entrant;

/**
 * マッチの結果を表す不変クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class MatchResult implements Result<Match> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * マッチ実施条件と個々のゲーム結果からマッチ結果を生成します。<br>
     * 
     * @param matchCondition マッチ実施条件
     * @param gameResults ゲーム実施条件A, Bそれぞれにおけるゲーム結果のリストが格納された {@code Map}
     * @return マッチ結果
     * @throws NullPointerException {@code matchCondition}、{@code gameResults} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code gameResults} の内容が不正な場合
     */
    public static MatchResult of(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        Objects.requireNonNull(matchCondition);
        Objects.requireNonNull(gameResults);
        
        if (!gameResults.containsKey(Entrant.A) || !gameResults.containsKey(Entrant.B)) {
            throw new IllegalArgumentException(String.format(
                    "ゲーム結果が格納されていません。contains key? A:%b, B:%b",
                    gameResults.containsKey(Entrant.A), gameResults.containsKey(Entrant.B)));
        }
        if (gameResults.get(Entrant.A).size() + gameResults.get(Entrant.B).size() != matchCondition.times) {
            throw new IllegalArgumentException(String.format(
                    "ゲーム結果の数が不正です。times=%d, resultA.size=%d, resultB.size=%d",
                    matchCondition.times, gameResults.get(Entrant.A).size(), gameResults.get(Entrant.B).size()));
        }
        
        Map<Entrant, List<GameResult>> copy = new EnumMap<>(Entrant.class);
        copy.put(Entrant.A, new ArrayList<>(gameResults.get(Entrant.A)));
        copy.put(Entrant.B, new ArrayList<>(gameResults.get(Entrant.B)));
        return new MatchResult(matchCondition, copy);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** マッチ実施条件 */
    public final MatchCondition matchCondition;
    
    /** 勝者（引き分けの場合は {@code null}） */
    public final Entrant winner;
    
    /** プレーヤーA, Bそれぞれの対戦成績が格納された {@code Map} */
    public final Map<Entrant, ResultCount> resultCounts;
    
    private final String description;
    
    private MatchResult(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        assert matchCondition != null;
        assert gameResults != null;
        assert gameResults.containsKey(Entrant.A);
        assert gameResults.containsKey(Entrant.B);
        assert gameResults.get(Entrant.A).size() + gameResults.get(Entrant.B).size() == matchCondition.times;
        
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
        ResultCount countB = resultCounts.get(Entrant.B);
        assert countA.win + countA.draw + countA.lose == matchCondition.times;
        assert countA.win == countB.lose;
        assert countA.lose == countB.win;
        assert countA.draw == countB.draw;
        
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
