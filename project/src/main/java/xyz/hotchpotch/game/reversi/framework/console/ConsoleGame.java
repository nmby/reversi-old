package xyz.hotchpotch.game.reversi.framework.console;

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
        Class<? extends Player> playerBlack = CommonUtil.arrangePlayerClass(Color.BLACK + "のプレーヤー");
        Class<? extends Player> playerWhite = CommonUtil.arrangePlayerClass(Color.WHITE + "のプレーヤー");
        long givenMillisPerTurn = CommonUtil.arrangeGivenMillisPerTurn();
        long givenMillisInGame = CommonUtil.arrangeGivenMillisInGame();
        Map<String, String> params = new HashMap<>();
        boolean auto = CommonUtil.arrangeAuto();
        params.put("auto", Boolean.toString(auto));
        params = CommonUtil.arrangeAdditionalParams(params);
        
        return GameCondition.of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame, params);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final GameCondition gameCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    private final boolean auto;
    
    private Map<Color, Player> players;
    private Board board;
    private Color currColor;
    private Map<Color, Long> remainingMillisInGame;
    
    private ConsoleGame(GameCondition gameCondition) {
        this.gameCondition = gameCondition;
        
        Level level = CommonUtil.getParameter(
                gameCondition,
                "print.level",
                s -> Enum.valueOf(Level.class, s),
                Level.GAME);
        
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
                        "%s が指定されました。（%d ミリ秒経過、残り持ち時間 %d ミリ秒）",
                        move, remainingBefore - remainingAfter, remainingAfter));
                if (!auto) {
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
        if (!auto) {
            waiter.get();
        }
        printer.println(Level.MATCH, "");
        
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
