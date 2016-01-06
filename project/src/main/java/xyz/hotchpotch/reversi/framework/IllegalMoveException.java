package xyz.hotchpotch.reversi.framework;

import java.util.Objects;

import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.BoardSnapshot;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;

/**
 * ルールに反する手が指定されたことを表す例外です。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class IllegalMoveException extends RuleViolationException {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /** ルール違反の手 */
    public final Move move;
    
    /** ルール違反が発生した際のリバーシ盤 */
    public final Board board;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param move ルール違反の手
     * @param board ルール違反が発生した際のリバーシ盤
     * @throws NullPointerException {@code violator}、{@code move}、{@code board} のいずれかが {@code null} の場合
     */
    public IllegalMoveException(String message, Color violator, Move move, Board board) {
        super(message, Objects.requireNonNull(violator));
        this.move = Objects.requireNonNull(move);
        this.board = BoardSnapshot.of(Objects.requireNonNull(board));
    }
    
    /**
     * 例外をコピーして生成します。<br>
     * 
     * @param original 元の例外
     * @throws NullPointerException {@code original} が {@code null} の場合
     */
    /*package*/ IllegalMoveException(IllegalMoveException original) {
        super(original.getMessage(), original.violator, original.getCause());
        move = original.move;
        board = BoardSnapshot.of(original.board);
    }
}
