package xyz.hotchpotch.game.reversi.framework;

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
     * @return ゲーム結果
     * @throws NullPointerException {@code gameCondition}、{@code board} のいずれかが {@code null} の場合
     */
    public static GameResult of(GameCondition gameCondition, Board board) {
        return new GameResult(
                Objects.requireNonNull(gameCondition),
                Objects.requireNonNull(board));
    }
    
    /**
     * ルール違反によりゲームが終了した場合のゲーム結果を生成します。<br>
     * 
     * @param gameCondition ゲーム条件
     * @param board ゲーム終了時のリバーシ盤
     * @param violation ルール違反
     * @return ゲーム結果
     * @throws NullPointerException {@code gameCondition}、{@code board}、{@code violation} のいずれかが
     *                              {@code null} の場合
     */
    public static GameResult of(GameCondition gameCondition, Board board, RuleViolationException violation) {
        return new GameResult(
                Objects.requireNonNull(gameCondition),
                Objects.requireNonNull(board),
                Objects.requireNonNull(violation));
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final GameCondition gameCondition;
    public final Board board;
    public final RuleViolationException violation;
    public final Color winner;
    private final String description;
    
    private GameResult(GameCondition gameCondition, Board board) {
        this.gameCondition = gameCondition;
        this.board = BoardSnapshot.of(board);
        this.violation = null;
        
        winner = Rule.winner(board);
        int black = (int) Point.stream().filter(p -> board.colorAt(p) == Color.BLACK).count();
        int white = (int) Point.stream().filter(p -> board.colorAt(p) == Color.WHITE).count();
        
        description = String.format("%s %s:%d, %s:%d",
                winner == null ? "引き分けです。" : String.format("%s:%s の勝ちです。",
                        winner, gameCondition.playerClasses.get(winner).getSimpleName()),
                Color.BLACK, black, Color.WHITE, white);
    }
    
    private GameResult(GameCondition gameCondition, Board board, RuleViolationException violation) {
        this.gameCondition = gameCondition;
        this.board = BoardSnapshot.of(board);
        this.violation = violation;
        winner = violation.violator.opposite();
        description = String.format(
                "%s:%s の反則負けです。%s",
                violation.violator,
                gameCondition.playerClasses.get(violation.violator).getSimpleName(),
                violation.getMessage());
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
