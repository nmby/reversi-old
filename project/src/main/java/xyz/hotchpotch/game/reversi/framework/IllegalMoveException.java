package xyz.hotchpotch.game.reversi.framework;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.BoardSnapshot;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;

/**
 * ルールに反する手が指定されたことを表す例外です。<br>
 * 
 * @author nmby
 */
public class IllegalMoveException extends RuleViolationException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
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
     * @param board そのときのリバーシ盤
     */
    public IllegalMoveException(String message, Color violator, Move move, Board board) {
        super(message, violator);
        this.move = move;
        this.board = BoardSnapshot.of(board);
    }
}
