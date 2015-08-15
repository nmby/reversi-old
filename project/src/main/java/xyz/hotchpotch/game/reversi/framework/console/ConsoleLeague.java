package xyz.hotchpotch.game.reversi.framework.console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.framework.League;
import xyz.hotchpotch.game.reversi.framework.League.Pair;
import xyz.hotchpotch.game.reversi.framework.LeagueCondition;
import xyz.hotchpotch.game.reversi.framework.LeagueResult;
import xyz.hotchpotch.game.reversi.framework.MatchCondition;
import xyz.hotchpotch.game.reversi.framework.MatchResult;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.game.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入出力を用いたリーグ実行クラスです。<br>
 * 
 * @author nmby
 */
public class ConsoleLeague implements ConsolePlayable<League> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * リーグ条件を指定してリーグ実行クラスを生成します。<br>
     * 
     * @param leagueCondition リーグ条件
     * @return リーグ実行クラス
     * @throws NullPointerException {@code leagueCondition} が {@code null} の場合
     */
    public static ConsoleLeague of(LeagueCondition leagueCondition) {
        return new ConsoleLeague(Objects.requireNonNull(leagueCondition));
    }
    
    /**
     * リーグ条件を標準入力から指定することによりリーグ実行クラスを生成します。<br>
     * 
     * @return リーグ実行クラス
     */
    public static ConsoleLeague arrange() {
        return new ConsoleLeague(arrangeLeagueCondition());
    }
    
    private static LeagueCondition arrangeLeagueCondition() {
        List<Class<? extends Player>> players = CommonUtil.arrangePlayerClassList();
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
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final LeagueCondition leagueCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    private ConsoleLeague(LeagueCondition leagueCondition) {
        this.leagueCondition = leagueCondition;
        
        Level level = CommonUtil.getParameter(
                leagueCondition,
                "print.level",
                s -> Enum.valueOf(Level.class, s),
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
        
        final int num = leagueCondition.playerClasses.size();
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
