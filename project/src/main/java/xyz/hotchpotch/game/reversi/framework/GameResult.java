package xyz.hotchpotch.game.reversi.framework;

import java.lang.reflect.Constructor;
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
     * @param gameCondition ゲーム実施条件
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
            
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(board);
        Objects.requireNonNull(remainingMillisInGame);
        
        return new GameResult(
                gameCondition,
                BoardSnapshot.of(board),
                new EnumMap<>(remainingMillisInGame));
    }
    
    /**
     * ルール違反によりゲームが終了した場合のゲーム結果を生成します。<br>
     * 
     * @param gameCondition ゲーム実施条件
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
            
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(board);
        Objects.requireNonNull(remainingMillisInGame);
        Objects.requireNonNull(violation);
        
        return new GameResult(
                gameCondition,
                BoardSnapshot.of(board),
                new EnumMap<>(remainingMillisInGame),
                copyOf(violation));
    }
    
    private static RuleViolationException copyOf(RuleViolationException original) {
        // このクラスを不変にするために、インスタンス生成時には防御的コピーをとり、クライアントへはコピーを返す。
        // 出来るだけ violation インスタンスの実際の型が備えるコピーコンストラクタを利用し、
        // 失敗した場合は RuleViolationException のコピーコンストラクタを利用する。
        // 
        // 何かもうちょっとスマートなやり方はないんだろうか... Collections#unmodifiableThrowable とか無いのかな...
        // それとも例外オブジェクトをこんなふうに使うことがそもそも間違っている...？
        Class<? extends RuleViolationException> type = original.getClass();
        try {
            Constructor<? extends RuleViolationException> copyConstructor = type.getConstructor(type);
            return copyConstructor.newInstance(original);
        } catch (Exception e) {
            return new RuleViolationException(original);
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /** ゲーム実施条件 */
    public final GameCondition gameCondition;
    
    /** ゲーム終了時のリバーシ盤 */
    public final Board board;
    
    /** 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map} */
    public final Map<Color, Long> remainingMillisInGame;
    
    /** 勝者（引き分けの場合は {@code null}） */
    public final Color winner;
    
    private final RuleViolationException violation;
    private final String description;
    
    private GameResult(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame) {
            
        // 防御的コピーをとるのは static メソッドのレイヤ、public で公開するために不変化ラップするのはコンストラクタのレイヤとする。
        this.gameCondition = gameCondition;
        this.board = board;
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
        this.board = board;
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
     * ゲーム終了の原因となったルール違反の内容を表す例外オブジェクトを返します。<br>
     * ルール違反なくゲームが正常終了した場合は {@code null} を返します。<br>
     * 
     * @return ルール違反の内容を表す例外オブジェクト
     */
    public RuleViolationException violation() {
        return violation == null ? null : copyOf(violation);
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
