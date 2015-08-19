package xyz.hotchpotch.game.reversi.framework;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.BoardSnapshot;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;

/**
 * ゲームの結果を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class GameResult implements Result<Game> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * ルール違反なくゲームが正常終了した場合のゲーム結果を生成します。<br>
     * 
     * @param gameCondition ゲーム条件
     * @param board ゲーム終了時のリバーシ盤
     * @param remainingMillisInGame 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map}
     * @return ゲーム結果
     * @throws NullPointerException {@code gameCondition}、{@code board}、{@code remainingMillisInGame}
     *                              のいずれかが {@code null} の場合
     */
    public static GameResult of(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame) {
            
        return new GameResult(
                Objects.requireNonNull(gameCondition),
                Objects.requireNonNull(board),
                new EnumMap<>(Objects.requireNonNull(remainingMillisInGame)));
    }
    
    /**
     * ルール違反によりゲームが終了した場合のゲーム結果を生成します。<br>
     * 
     * @param gameCondition ゲーム条件
     * @param board ゲーム終了時のリバーシ盤
     * @param remainingMillisInGame 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map}
     * @param violation ルール違反
     * @return ゲーム結果
     * @throws NullPointerException {@code gameCondition}、{@code board}、{@code remainingMillisInGame}、{@code violation}
     *                              のいずれかが {@code null} の場合
     */
    public static GameResult of(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame,
            RuleViolationException violation) {
            
        return new GameResult(
                Objects.requireNonNull(gameCondition),
                Objects.requireNonNull(board),
                new EnumMap<>(Objects.requireNonNull(remainingMillisInGame)),
                Objects.requireNonNull(violation));
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /** ゲーム条件 */
    public final GameCondition gameCondition;
    
    /** ゲーム終了時のリバーシ盤 */
    public final Board board;
    
    /** 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map} */
    public final Map<Color, Long> remainingMillisInGame;
    
    /** ルール違反 */
    public final RuleViolationException violation;
    
    /** 勝者（引き分けの場合は {@code null}） */
    public final Color winner;
    
    private final String description;
    
    private GameResult(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame) {
            
        this.gameCondition = gameCondition;
        this.board = BoardSnapshot.of(board);
        this.remainingMillisInGame = Collections.unmodifiableMap(remainingMillisInGame);
        this.violation = null;
        
        winner = Rule.winner(this.board);
        int black = (int) Point.stream().filter(p -> this.board.colorAt(p) == Color.BLACK).count();
        int white = (int) Point.stream().filter(p -> this.board.colorAt(p) == Color.WHITE).count();
        
        description = String.format("%s %s:%d（残り %d ms）, %s:%d（残り %d ms）",
                winner == null ? "引き分けです。" : String.format("%s:%s の勝ちです。",
                        winner, gameCondition.playerClasses.get(winner).getSimpleName()),
                Color.BLACK, black, this.remainingMillisInGame.get(Color.BLACK),
                Color.WHITE, white, this.remainingMillisInGame.get(Color.WHITE));
    }
    
    private GameResult(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame,
            RuleViolationException violation) {
            
        this.gameCondition = gameCondition;
        this.board = BoardSnapshot.of(board);
        this.remainingMillisInGame = Collections.unmodifiableMap(remainingMillisInGame);
        this.violation = violation;
        winner = violation.violator.opposite();
        description = String.format(
                "%s:%s の反則負けです。%s（残り %s:%d ms, %s:%d ms）",
                violation.violator,
                gameCondition.playerClasses.get(violation.violator).getSimpleName(),
                violation.getMessage(),
                Color.BLACK, remainingMillisInGame.get(Color.BLACK),
                Color.WHITE, remainingMillisInGame.get(Color.WHITE));
    }
    
    /**
     * このゲーム結果の文字列表現を返します。<br>
     * 
     * @return このゲーム結果の文字列表現
     */
    @Override
    public String toString() {
        return description;
    }
}
