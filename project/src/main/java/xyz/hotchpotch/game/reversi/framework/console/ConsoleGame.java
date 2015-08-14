package xyz.hotchpotch.game.reversi.framework.console;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

import xyz.hotchpotch.game.reversi.aiplayers.CrazyAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.RandomAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SlowpokeAIPlayer;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.BoardSnapshot;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.core.StrictBoard;
import xyz.hotchpotch.game.reversi.framework.Game;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.GameResult;
import xyz.hotchpotch.game.reversi.framework.GoCrazyException;
import xyz.hotchpotch.game.reversi.framework.IllegalMoveException;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.game.reversi.framework.RuleViolationException;
import xyz.hotchpotch.game.reversi.framework.TimeUpException;
import xyz.hotchpotch.game.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入出力を用いたゲーム実行クラスです。<br>
 * 
 * @author nmby
 */
public class ConsoleGame implements ConsolePlayable<Game> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    /**
     * ゲーム条件を指定してゲーム実行クラスを生成します。<br>
     * 
     * @param gameCondition ゲーム条件
     * @return ゲーム実行クラス
     * @throws NullPointerException {@code gameCondition} が {@code null} の場合
     */
    public static ConsoleGame of(GameCondition gameCondition) {
        return new ConsoleGame(Objects.requireNonNull(gameCondition));
    }
    
    /**
     * ゲーム条件を標準入力から指定することによりゲーム実行クラスを生成します。<br>
     * 
     * @return ゲーム実行クラス
     */
    public static ConsoleGame arrange() {
        return new ConsoleGame(arrangeGameCondition());
    }
    
    private static GameCondition arrangeGameCondition() {
        Class<? extends Player> playerBlack = arrangePlayer(Color.BLACK);
        Class<? extends Player> playerWhite = arrangePlayer(Color.WHITE);
        
        ConsoleScanner<Long> scGivenMillisPerTurn = ConsoleScanner
                .longBuilder(1, 60000)
                .prompt("一手あたりの制限時間（ミリ秒）を 1～60000（1分） の範囲で指定してください" + BR + "> ")
                .build();
        ConsoleScanner<Long> scGivenMillisInGame = ConsoleScanner
                .longBuilder(1, 1800000)
                .prompt("ゲーム内での持ち時間（ミリ秒）を 1～1800000（30分） の範囲で指定してください" + BR + "> ")
                .build();
        long givenMillisPerTurn = scGivenMillisPerTurn.get();
        long givenMillisInGame = scGivenMillisInGame.get();
        
        return GameCondition.of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame);
    }
    
    private static Class<? extends Player> arrangePlayer(Color color) {
        List<Class<? extends Player>> playerClasses = playerClasses();
        StringBuilder prompt1 = new StringBuilder();
        prompt1.append(String.format("%sのプレーヤーを番号で選択してください。", color)).append(BR);
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
    
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    private final GameCondition gameCondition;
    private final ConsolePrinter printer;
    
    private Map<Color, Player> players;
    private Board board;
    private Color currColor;
    private Map<Color, Long> remainingMillisInGame;
    
    private ConsoleGame(GameCondition gameCondition) {
        this.gameCondition = gameCondition;
        
        String printLevel = gameCondition.getProperty("print.level");
        Level level;
        try {
            level = Enum.valueOf(Level.class, printLevel);
        } catch (IllegalArgumentException | NullPointerException e) {
            level = Level.GAME;
        }
        printer = ConsolePrinter.of(level);
    }
    
    /**
     * ゲームを実行します。<br>
     * 
     * @return ゲーム結果
     */
    @Override
    public synchronized GameResult play() {
        printer.println(Level.GAME, "");
        printer.println(Level.GAME, "****************************************************************");
        printer.println(Level.GAME, "ゲームを開始します。");
        printer.print(Level.GAME, gameCondition.toStringKindly());
        printer.println(Level.GAME, "****************************************************************");
        
        GameResult gameResult;
        try {
            init();
            
            while (Rule.isGameOngoing(board)) {
                
                printer.println(Level.GAME, "");
                printer.println(Level.GAME, board.toStringKindly());
                
                long remainingBefore = remainingMillisInGame.get(currColor);
                Point point = getPoint();
                Move move = Move.of(currColor, point);
                long remainingAfter = remainingMillisInGame.get(currColor);
                
                printer.println(Level.GAME, String.format(
                        "%s が指定されました。（%d ミリ秒経過、残り持ち時間 %d ミリ秒）",
                        move, remainingBefore - remainingAfter, remainingAfter));
                if (printer.level == Level.GAME) {
                    waiter.get();
                }
                
                applyMove(move);
            }
            
            printer.println(Level.GAME, "");
            printer.println(Level.GAME, board.toStringKindly());
            gameResult = GameResult.of(gameCondition, board);
            
        } catch (RuleViolationException e) {
            
            printer.println(Level.GAME, "");
            gameResult = GameResult.of(gameCondition, board, e);
            
        }
        
        if (printer.level != Level.GAME) {
            printer.println(Level.MATCH, board.toStringKindly());
        }
        printer.println(Level.GAME, "****************************************************************");
        printer.println(Level.GAME, "ゲームが終了しました。");
        printer.println(Level.MATCH, gameResult.toString());
        printer.println(Level.GAME, "****************************************************************");
        printer.println(Level.GAME, "");
        if (printer.level == Level.GAME) {
            waiter.get();
        }
        printer.println(Level.GAME, "");
        
        return gameResult;
    }
    
    private void init() throws RuleViolationException {
        players = new EnumMap<>(Color.class);
        for (Color color : Color.values()) {
            Class<? extends Player> playerClass = gameCondition.playerClasses.get(color);
            try {
                Player player = Player.getPlayerInstance(playerClass, color, gameCondition);
                players.put(color, player);
            } catch (ReflectiveOperationException e) {
                throw new GoCrazyException("インスタンス化に失敗しました。" + e.getMessage(), color, e);
            }
        }
        
        board = StrictBoard.initializedBoard();
        currColor = Color.BLACK;
        
        remainingMillisInGame = new EnumMap<>(Color.class);
        Color.stream().forEach(c -> {
            remainingMillisInGame.put(c, gameCondition.givenMillisInGame);
        });
    }
    
    private Point getPoint() throws RuleViolationException {
        long timeLimit1 = gameCondition.givenMillisPerTurn;
        long timeLimit2 = remainingMillisInGame.get(currColor);
        long timeLimit3 = Long.min(timeLimit1, timeLimit2);
        
        Player player = players.get(currColor);
        Board snapshot = BoardSnapshot.of(board);
        FutureTask<Point> task = new FutureTask<>(() -> player.decide(snapshot, currColor, timeLimit1, timeLimit2));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Instant start;
        Instant end;
        Point point;
        try {
            start = Instant.now();
            executor.execute(task);
            point = task.get(timeLimit3, TimeUnit.MILLISECONDS);
            end = Instant.now();
            
        } catch (TimeoutException e) {
            if (timeLimit2 == timeLimit3) {
                throw new TimeUpException("ゲーム内での持ち時間が無くなりました。", currColor);
            } else {
                throw new TimeUpException("一手あたりの制限時間を超過しました。", currColor);
            }
        } catch (ExecutionException e) {
            throw new GoCrazyException("思考中に例外が発生しました。" + e.getMessage(), currColor, e);
        } catch (InterruptedException e) {
            throw new GoCrazyException("思考中に割り込みが発生しました。" + e.getMessage(), currColor, e);
        } finally {
            // MEMO: java.util.concurrent.ExecutorService 周りがよく分かってないので要お勉強
            // これで良いのか？？
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
        
        long elapsed = Duration.between(start, end).toMillis();
        if (timeLimit2 <= elapsed) {
            throw new TimeUpException("ゲーム内での持ち時間が無くなりました。", currColor);
        } else if (timeLimit1 <= elapsed) {
            throw new TimeUpException("一手あたりの制限時間を超過しました。", currColor);
        }
        remainingMillisInGame.put(currColor, timeLimit2 - elapsed);
        
        return point;
    }
    
    private void applyMove(Move move) throws RuleViolationException {
        if (!Rule.canApply(board, move)) {
            if (move.point == null) {
                throw new IllegalMoveException("指せる位置があるのにパスが選択されました。", currColor, move, board);
            } else {
                throw new IllegalMoveException("指せない位置が指定されました。", currColor, move, board);
            }
        }
        
        board.apply(move);
        currColor = currColor.opposite();
    }
}
