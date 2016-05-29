package xyz.hotchpotch.reversi.framework.console;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.BoardSnapshot;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;
import xyz.hotchpotch.reversi.core.StrictBoard;
import xyz.hotchpotch.reversi.framework.Game;
import xyz.hotchpotch.reversi.framework.GameCondition;
import xyz.hotchpotch.reversi.framework.GameResult;
import xyz.hotchpotch.reversi.framework.GoCrazyException;
import xyz.hotchpotch.reversi.framework.IllegalMoveException;
import xyz.hotchpotch.reversi.framework.Player;
import xyz.hotchpotch.reversi.framework.RuleViolationException;
import xyz.hotchpotch.reversi.framework.TimeUpException;
import xyz.hotchpotch.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.console.ConsoleScanner;

/**
 * 標準入出力を用いたゲーム実行クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class ConsoleGame implements ConsolePlayable<Game> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * ゲーム実施条件を指定してゲーム実行クラスを生成します。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @return ゲーム実行クラス
     * @throws NullPointerException {@code gameCondition} が {@code null} の場合
     */
    public static ConsoleGame of(GameCondition gameCondition) {
        return new ConsoleGame(Objects.requireNonNull(gameCondition));
    }
    
    /**
     * ゲーム実施条件を標準入力から指定することによりゲーム実行クラスを生成します。<br>
     * 
     * @return ゲーム実行クラス
     */
    public static ConsoleGame arrange() {
        return new ConsoleGame(arrangeGameCondition());
    }
    
    private static GameCondition arrangeGameCondition() {
        Class<? extends Player> playerBlack = CommonUtil.arrangePlayerClass(Color.BLACK + "のプレーヤー", true);
        Class<? extends Player> playerWhite = CommonUtil.arrangePlayerClass(Color.WHITE + "のプレーヤー", true);
        long givenMillisPerTurn = CommonUtil.arrangeGivenMillisPerTurn();
        long givenMillisInGame = CommonUtil.arrangeGivenMillisInGame();
        
        Map<String, String> params = new HashMap<>();
        boolean auto = CommonUtil.arrangeAuto();
        params.put("auto", Boolean.toString(auto));
        params = CommonUtil.arrangeAdditionalParams(params);
        
        return GameCondition.of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame, params);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final GameCondition gameCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    private final boolean auto;
    
    private Map<Color, Player> players;
    private Board board;
    private Color currColor;
    private Map<Color, Long> remainingMillisInGame;
    
    private ConsoleGame(GameCondition gameCondition) {
        assert gameCondition != null;
        
        this.gameCondition = gameCondition;
        
        Level level;
        if (NeedsUserInput.class.isAssignableFrom(gameCondition.playerClasses.get(Color.BLACK))
                || NeedsUserInput.class.isAssignableFrom(gameCondition.playerClasses.get(Color.WHITE))) {
                
            level = Level.GAME;
        } else {
            level = CommonUtil.getParameter(
                    gameCondition,
                    "print.level",
                    Level::valueOf,
                    Level.GAME);
        }
        printer = ConsolePrinter.of(level);
        auto = CommonUtil.getParameter(gameCondition, "auto", Boolean::valueOf, false);
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
                        "%s が選択されました。（%d ミリ秒経過、残り持ち時間 %d ミリ秒）",
                        move, remainingBefore - remainingAfter, remainingAfter));
                if (!auto) {
                    waiter.get();
                }
                
                applyMove(move);
            }
            
            printer.println(Level.GAME, "");
            printer.println(Level.GAME, board.toStringKindly());
            gameResult = GameResult.of(gameCondition, board, remainingMillisInGame);
            
        } catch (RuleViolationException e) {
            printer.println(Level.GAME, "");
            gameResult = GameResult.of(gameCondition, board, remainingMillisInGame, e);
        }
        
        // ゲーム結果を各プレーヤーに通知する。
        try {
            players.get(Color.BLACK).notifyOfResult(gameResult);
        } catch (RuntimeException e) {
        }
        try {
            players.get(Color.WHITE).notifyOfResult(gameResult);
        } catch (RuntimeException e) {
        }
        
        if (printer.level != Level.GAME) {
            printer.println(Level.MATCH, board.toStringKindly());
        }
        printer.println(Level.GAME, "****************************************************************");
        printer.println(Level.GAME, "ゲームが終了しました。");
        printer.println(Level.MATCH, gameResult.toString());
        printer.println(Level.GAME, "****************************************************************");
        printer.println(Level.GAME, "");
        if (!auto) {
            waiter.get();
        }
        printer.println(Level.MATCH, "");
        
        cleanUp();
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
        remainingMillisInGame.put(Color.BLACK, gameCondition.givenMillisInGame);
        remainingMillisInGame.put(Color.WHITE, gameCondition.givenMillisInGame);
    }
    
    private void cleanUp() {
        // なんかVBAのコードみたいだ... orz
        players = null;
        board = null;
        currColor = null;
        remainingMillisInGame = null;
    }
    
    private Point getPoint() throws RuleViolationException {
        long timeLimit1 = gameCondition.givenMillisPerTurn;
        long timeLimit2 = remainingMillisInGame.get(currColor);
        long timeLimit3 = Long.min(timeLimit1, timeLimit2);
        
        Player player = players.get(currColor);
        Board snapshot = BoardSnapshot.of(board);
        FutureTask<Point> task = new FutureTask<>(() -> player.decide(snapshot, currColor, timeLimit1, timeLimit2));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Instant start = null;
        Instant end = null;
        Point point = null;
        long elapsed;
        
        try {
            start = Instant.now();
            executor.execute(task);
            point = task.get(timeLimit3, TimeUnit.MILLISECONDS);
            
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
            // 例外で負けの場合も残り時間を差し引く。
            end = Instant.now();
            elapsed = Long.min(timeLimit3, Duration.between(start, end).toMillis());
            remainingMillisInGame.put(currColor, timeLimit2 - elapsed);
            
            // TODO: java.util.concurrent.ExecutorService 周りがよく分かってないので要お勉強
            // これで良いのか？？
            if (!task.isDone()) {
                task.cancel(true);
            }
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
        
        if (timeLimit2 <= elapsed) {
            throw new TimeUpException("ゲーム内での持ち時間が無くなりました。", currColor);
        } else if (timeLimit1 <= elapsed) {
            throw new TimeUpException("一手あたりの制限時間を超過しました。", currColor);
        }
        
        return point;
    }
    
    private void applyMove(Move move) throws RuleViolationException {
        assert move != null;
        
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
