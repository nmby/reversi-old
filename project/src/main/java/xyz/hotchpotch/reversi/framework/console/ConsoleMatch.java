package xyz.hotchpotch.reversi.framework.console;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.reversi.framework.GameResult;
import xyz.hotchpotch.reversi.framework.Match;
import xyz.hotchpotch.reversi.framework.Match.Entrant;
import xyz.hotchpotch.reversi.framework.MatchCondition;
import xyz.hotchpotch.reversi.framework.MatchResult;
import xyz.hotchpotch.reversi.framework.Player;
import xyz.hotchpotch.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.console.ConsoleScanner;

/**
 * 標準入出力を用いたマッチ実行クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class ConsoleMatch implements ConsolePlayable<Match> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * マッチ実施条件を指定してマッチ実行クラスを生成します。<br>
     * 
     * @param matchCondition マッチ実施条件
     * @return マッチ実行クラス
     * @throws NullPointerException {@code matchCondition} が {@code null} の場合
     * @throws IllegalArgumentException {@code matchCondition.playerClasses} に {@link NeedsUserInput} 実装クラスが含まれる場合
     */
    public static ConsoleMatch of(MatchCondition matchCondition) {
        Objects.requireNonNull(matchCondition);
        if (NeedsUserInput.class.isAssignableFrom(matchCondition.playerClasses.get(Entrant.A))
                || NeedsUserInput.class.isAssignableFrom(matchCondition.playerClasses.get(Entrant.B))) {
                
            throw new IllegalArgumentException(String.format("%s 実装クラスは指定できません。", NeedsUserInput.class.getSimpleName()));
        }
        return new ConsoleMatch(matchCondition);
    }
    
    /**
     * マッチ実施条件を標準入力から指定することによりマッチ実行クラスを生成します。<br>
     * 
     * @return マッチ実行クラス
     */
    public static ConsoleMatch arrange() {
        return new ConsoleMatch(arrangeMatchCondition());
    }
    
    private static MatchCondition arrangeMatchCondition() {
        Class<? extends Player> playerA = CommonUtil.arrangePlayerClass("プレーヤー" + Entrant.A, false);
        Class<? extends Player> playerB = CommonUtil.arrangePlayerClass("プレーヤー" + Entrant.B, false);
        long givenMillisPerTurn = CommonUtil.arrangeGivenMillisPerTurn();
        long givenMillisInGame = CommonUtil.arrangeGivenMillisInGame();
        int times = CommonUtil.arrangeTimes();
        
        Map<String, String> params = new HashMap<>();
        boolean dispDetail = CommonUtil.arrangeDispDetail();
        if (dispDetail) {
            params.put("print.level", "GAME");
        }
        params = CommonUtil.arrangeAdditionalParams(params);
        
        return MatchCondition.of(playerA, playerB, givenMillisPerTurn, givenMillisInGame, times, params);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final MatchCondition matchCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    private ConsoleMatch(MatchCondition matchCondition) {
        assert matchCondition != null;
        
        this.matchCondition = matchCondition;
        
        Level level = CommonUtil.getParameter(
                matchCondition,
                "print.level",
                Level::valueOf,
                Level.MATCH);
        printer = ConsolePrinter.of(level);
    }
    
    /**
     * マッチを実行します。<br>
     * 
     * @return マッチ結果
     */
    @Override
    public synchronized MatchResult play() {
        printer.println(Level.MATCH, "");
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "マッチを開始します。");
        printer.print(Level.MATCH, matchCondition.toStringKindly());
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "");
        
        Map<Entrant, ConsoleGame> games = new EnumMap<>(Entrant.class);
        Map<Entrant, List<GameResult>> gameResults = new EnumMap<>(Entrant.class);
        for (Entrant entrant : Entrant.values()) {
            games.put(entrant, ConsoleGame.of(matchCondition.gameConditions.get(entrant)));
            gameResults.put(entrant, new ArrayList<>());
        }
        
        Entrant currEntrant = Entrant.A;
        for (int n = 0; n < matchCondition.times; n++) {
            
            ConsoleGame game = games.get(currEntrant);
            GameResult gameResult = game.play();
            gameResults.get(currEntrant).add(gameResult);
            
            currEntrant = currEntrant.opposite();
        }
        
        MatchResult matchResult = MatchResult.of(matchCondition, gameResults);
        
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "マッチが終了しました。");
        printer.println(Level.LEAGUE, matchResult.toString());
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "");
        if (printer.level == Level.MATCH) {
            waiter.get();
        }
        printer.println(Level.MATCH, "");
        
        return matchResult;
    }
}
