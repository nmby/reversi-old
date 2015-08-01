package xyz.hotchpotch.game.reversi.core;

/**
 * ルールに反する手が指定されたことを表す例外です。<br>
 * 
 * @author nmby
 */
public class IllegalMoveException extends RuleViolationException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // どちらも不変なので公開しちゃって問題ない（はず）。
    public final Move move;
    public final Board board;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalMoveException(String message, Move move, Board board) {
        super(String.format("%s move=%s, board=%s", message, move, board));
        this.move = move;
        this.board = BoardSnapshot.of(board);
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalMoveException(Move move, Board board) {
        this("不正な手が指定されました。", move, board);
    }
}