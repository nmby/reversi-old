package xyz.hotchpotch.reversi.framework;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.BoardSnapshot;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;

/**
 * ゲームの結果を表す不変クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class GameResult implements Result<Game> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * ルール違反なくゲームが正常終了した場合のゲーム結果を生成します。<br>
     * 
     * @param gameCondition ゲーム実施条件
     * @param board ゲーム終了時のリバーシ盤
     * @param remainingMillisInGame 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map}
     * @return ゲーム結果
     * @throws NullPointerException {@code gameCondition}、{@code board}、{@code remainingMillisInGame}
     *                              のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code board} がゲーム終了状態にない場合
     */
    public static GameResult of(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame) {
            
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(board);
        Objects.requireNonNull(remainingMillisInGame);
        
        if (Rule.isGameOngoing(board)) {
            throw new IllegalArgumentException("ゲームが終了していません。board=" + board.toStringInLine());
        }
        
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
     * @param violation ルール違反を表す例外
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
        assert original != null;
        
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
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** ゲーム実施条件 */
    public final GameCondition gameCondition;
    
    /** ゲーム終了時のリバーシ盤 */
    public final Board board;
    
    /** 黒白それぞれの残り持ち時間（ミリ秒）が格納された {@code Map} */
    public final Map<Color, Long> remainingMillisInGame;
    
    /** 勝者の色（引き分けの場合は {@code null}） */
    public final Color winner;
    
    private final RuleViolationException violation;
    private final String description;
    
    // ルール違反なくゲームが正常終了した場合のコンストラクタ
    private GameResult(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame) {
            
        assert gameCondition != null;
        assert board != null;
        assert !Rule.isGameOngoing(board);
        assert remainingMillisInGame != null;
        
        // 防御的コピーをとるのは static メソッドのレイヤ、public で公開するために不変化ラップするのはコンストラクタのレイヤとする。
        this.gameCondition = gameCondition;
        this.board = board;
        this.remainingMillisInGame = Collections.unmodifiableMap(remainingMillisInGame);
        this.violation = null;
        
        winner = Rule.winner(this.board);
        int black = (int) Point.stream().map(this.board::colorAt).filter(c -> c == Color.BLACK).count();
        int white = (int) Point.stream().map(this.board::colorAt).filter(c -> c == Color.WHITE).count();
        
        description = String.format("%s %s:%d（残り %d ms）, %s:%d（残り %d ms）",
                winner == null ? "引き分けです。" : String.format("%s:%s の勝ちです。",
                        winner, gameCondition.playerClasses.get(winner).getSimpleName()),
                Color.BLACK, black, this.remainingMillisInGame.get(Color.BLACK),
                Color.WHITE, white, this.remainingMillisInGame.get(Color.WHITE));
    }
    
    // ルール違反によりゲームが終了した場合のコンストラクタ
    private GameResult(
            GameCondition gameCondition,
            Board board,
            Map<Color, Long> remainingMillisInGame,
            RuleViolationException violation) {
            
        assert gameCondition != null;
        assert board != null;
        assert remainingMillisInGame != null;
        assert violation != null;
        
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
