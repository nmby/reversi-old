package xyz.hotchpotch.game.reversi.core;

/**
 * 駒を置くことのできない位置が指定されたことを示す例外です。<br>
 * 
 * @author nmby
 */
public class IllegalPointException extends IllegalMoveException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalPointException(String message, Color violator, Move move, Board board) {
        super(message, violator, move, board);
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalPointException(String message, Move move, Board board) {
        super(message, move.color, move, board);
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalPointException(Move move, Board board) {
        this("その場所には指せません。", move, board);
    }
}
