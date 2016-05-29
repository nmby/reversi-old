package xyz.hotchpotch.reversi.framework.console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.reversi.framework.League;
import xyz.hotchpotch.reversi.framework.League.Pair;
import xyz.hotchpotch.reversi.framework.LeagueCondition;
import xyz.hotchpotch.reversi.framework.LeagueResult;
import xyz.hotchpotch.reversi.framework.MatchCondition;
import xyz.hotchpotch.reversi.framework.MatchResult;
import xyz.hotchpotch.reversi.framework.Player;
import xyz.hotchpotch.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.console.ConsoleScanner;

/**
 * 標準入出力を用いたリーグ実行クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class ConsoleLeague implements ConsolePlayable<League> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * リーグ実施条件を指定してリーグ実行クラスを生成します。<br>
     * 
     * @param leagueCondition リーグ実施条件
     * @return リーグ実行クラス
     * @throws NullPointerException {@code leagueCondition} が {@code null} の場合
     * @throws IllegalArgumentException {@code leagueCondition.playerClasses} に {@link NeedsUserInput} 実装クラスが含まれる場合
     */
    public static ConsoleLeague of(LeagueCondition leagueCondition) {
        Objects.requireNonNull(leagueCondition);
        if (leagueCondition.playerClasses.stream().anyMatch(NeedsUserInput.class::isAssignableFrom)) {
            throw new IllegalArgumentException(String.format("%s 実装クラスは指定できません。", NeedsUserInput.class.getSimpleName()));
        }
        return new ConsoleLeague(leagueCondition);
    }
    
    /**
     * リーグ実施条件を標準入力から指定することによりリーグ実行クラスを生成します。<br>
     * 
     * @return リーグ実行クラス
     */
    public static ConsoleLeague arrange() {
        return new ConsoleLeague(arrangeLeagueCondition());
    }
    
    private static LeagueCondition arrangeLeagueCondition() {
        List<Class<? extends Player>> players = CommonUtil.arrangePlayerClassList(false);
        long givenMillisPerTurn = CommonUtil.arrangeGivenMillisPerTurn();
        long givenMillisInGame = CommonUtil.arrangeGivenMillisInGame();
        int times = CommonUtil.arrangeTimes();
        
        Map<String, String> params = new HashMap<>();
        boolean dispDetail = CommonUtil.arrangeDispDetail();
        if (dispDetail) {
            params.put("print.level", "GAME");
        }
        params = CommonUtil.arrangeAdditionalParams(params);
        
        return LeagueCondition.of(players, givenMillisPerTurn, givenMillisInGame, times, params);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final LeagueCondition leagueCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    private ConsoleLeague(LeagueCondition leagueCondition) {
        assert leagueCondition != null;
        
        this.leagueCondition = leagueCondition;
        
        Level level = CommonUtil.getParameter(
                leagueCondition,
                "print.level",
                Level::valueOf,
                Level.LEAGUE);
        printer = ConsolePrinter.of(level);
    }
    
    /**
     * リーグを実行します。<br>
     * 
     * @return リーグ結果
     */
    @Override
    public synchronized LeagueResult play() {
        printer.println(Level.LEAGUE, "");
        printer.println(Level.LEAGUE, "****************************************************************");
        printer.println(Level.LEAGUE, "リーグを開始します。");
        printer.print(Level.LEAGUE, leagueCondition.toStringKindly());
        printer.println(Level.LEAGUE, "****************************************************************");
        printer.println(Level.LEAGUE, "");
        
        Map<Pair, MatchResult> matchResults = new HashMap<>();
        
        int num = leagueCondition.playerClasses.size();
        for (int idx1 = 0; idx1 < num - 1; idx1++) {
            for (int idx2 = idx1 + 1; idx2 < num; idx2++) {
                MatchCondition matchCondition = leagueCondition.matchConditions.get(Pair.of(idx1, idx2));
                ConsoleMatch match = ConsoleMatch.of(matchCondition);
                MatchResult matchResult = match.play();
                matchResults.put(Pair.of(idx1, idx2), matchResult);
            }
        }
        
        LeagueResult leagueResult = LeagueResult.of(leagueCondition, matchResults);
        
        printer.println(Level.LEAGUE, "");
        printer.println(Level.LEAGUE, "****************************************************************");
        printer.println(Level.LEAGUE, "リーグが終了しました。");
        printer.println(Level.LEAGUE, leagueResult.toString());
        printer.println(Level.LEAGUE, "****************************************************************");
        printer.println(Level.LEAGUE, "");
        if (printer.level == Level.LEAGUE) {
            waiter.get();
        }
        printer.println(Level.LEAGUE, "");
        
        return leagueResult;
    }
}
