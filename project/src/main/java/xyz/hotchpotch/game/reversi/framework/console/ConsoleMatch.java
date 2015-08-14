package xyz.hotchpotch.game.reversi.framework.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import xyz.hotchpotch.game.reversi.aiplayers.CrazyAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.RandomAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SlowpokeAIPlayer;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.framework.GameResult;
import xyz.hotchpotch.game.reversi.framework.Match;
import xyz.hotchpotch.game.reversi.framework.Match.Entrant;
import xyz.hotchpotch.game.reversi.framework.MatchCondition;
import xyz.hotchpotch.game.reversi.framework.MatchResult;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.game.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入出力を用いたマッチ実行クラスです。<br>
 * 
 * @author nmby
 */
public class ConsoleMatch implements ConsolePlayable<Match> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    /**
     * マッチ条件を指定してマッチ実行クラスを生成します。<br>
     * 
     * @param matchCondition マッチ条件
     * @return マッチ実行クラス
     * @throws NullPointerException {@code matchCondition} が {@code null} の場合
     */
    public static ConsoleMatch of(MatchCondition matchCondition) {
        return new ConsoleMatch(Objects.requireNonNull(matchCondition));
    }
    
    /**
     * マッチ条件を標準入力から指定することによりマッチ実行クラスを生成します。<br>
     * 
     * @return マッチ実行クラス
     */
    public static ConsoleMatch arrange() {
        return new ConsoleMatch(arrangeMatchCondition());
    }
    
    private static MatchCondition arrangeMatchCondition() {
        Class<? extends Player> playerA = arrangePlayer(Entrant.A);
        Class<? extends Player> playerB = arrangePlayer(Entrant.B);
        
        ConsoleScanner<Long> scGivenMillisPerTurn = ConsoleScanner
                .longBuilder(1, 60000)
                .prompt("一手あたりの制限時間（ミリ秒）を 1～60000（1分） の範囲で指定してください" + BR + "> ")
                .build();
        ConsoleScanner<Long> scGivenMillisInGame = ConsoleScanner
                .longBuilder(1, 1800000)
                .prompt("ゲーム内での持ち時間（ミリ秒）を 1～1800000（30分） の範囲で指定してください" + BR + "> ")
                .build();
        ConsoleScanner<Integer> scTimes = ConsoleScanner
                .intBuilder(1, 30)
                .prompt("対戦回数を 1～30 の範囲で指定してください" + BR + "> ")
                .build();
        long givenMillisPerTurn = scGivenMillisPerTurn.get();
        long givenMillisInGame = scGivenMillisInGame.get();
        int times = scTimes.get();
        
        return MatchCondition.of(playerA, playerB, givenMillisPerTurn, givenMillisInGame, times);
    }
    
    private static Class<? extends Player> arrangePlayer(Entrant entrant) {
        List<Class<? extends Player>> playerClasses = playerClasses();
        StringBuilder prompt1 = new StringBuilder();
        prompt1.append(String.format("プレーヤー%sを番号で選択してください。", entrant)).append(BR);
        int n = 0;
        for (Class<? extends Player> playerClass : playerClasses) {
            prompt1.append(String.format("\t%d : %s", ++n, playerClass.getName())).append(BR);
        }
        prompt1.append(String.format("\t0 : その他（自作クラス）")).append(BR);
        prompt1.append("> ");
        
        ConsoleScanner<Integer> scIdx = ConsoleScanner
                .intBuilder(0, playerClasses.size())
                .prompt(prompt1.toString())
                .build();
        int idx = scIdx.get();
        if (0 < idx) {
            return playerClasses.get(idx - 1);
        }
        
        Predicate<String> judge = s -> {
            try {
                @SuppressWarnings({ "unchecked", "unused" })
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return true;
            } catch (ClassNotFoundException | ClassCastException e) {
                return false;
            }
        };
        Function<String, Class<? extends Player>> converter = s -> {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return playerClass;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        };
        String prompt2 = "プレーヤークラスの完全修飾クラス名を指定してください（例：jp.co.hoge.MyAIPlayer）" + BR + "> ";
        String complaint = String.format(
                "クラスが見つからないか、%s を implements していません。", Player.class.getName()) + BR;
        ConsoleScanner<Class<? extends Player>> scPlayerClass = ConsoleScanner
                .builder(judge, converter, prompt2, complaint).build();
        return scPlayerClass.get();
    }
    
    private static List<Class<? extends Player>> playerClasses() {
        // TODO: このバカチョン実装はそのうちどうにかしたい...
        return Arrays.asList(
                ConsolePlayer.class,
                SimplestAIPlayer.class,
                RandomAIPlayer.class,
                SlowpokeAIPlayer.class,
                CrazyAIPlayer.class);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final MatchCondition matchCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    
    
    
    
    
    
    
    
    
    
    private Map<Entrant, Player> players;
    private Board board;
    private Color currColor;
    private Map<Color, Long> remainingMillisInGame;
    
    private ConsoleMatch(MatchCondition matchCondition) {
        this.matchCondition = matchCondition;
        
        String printLevel = matchCondition.getProperty("print.level");
        Level level;
        try {
            level = Enum.valueOf(Level.class, printLevel);
        } catch (IllegalArgumentException | NullPointerException e) {
            level = Level.MATCH;
        }
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
            
            GameResult gameResult = games.get(currEntrant).play();
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
