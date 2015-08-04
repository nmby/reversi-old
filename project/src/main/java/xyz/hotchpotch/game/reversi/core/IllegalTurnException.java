package xyz.hotchpotch.game.reversi.core;

/**
 * 順番とは異なる色の手が指定されたことを示す例外です。<br>
 * 
 * @author nmby
 */
public class IllegalTurnException extends IllegalMoveException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final Color properColor;
    
    /**
     * 例外を生成します。<br>
     * 
     * @param message 詳細メッセージ
     * @param violator ルールに違反したプレーヤーの色
     * @param properColor 本来の手番
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalTurnException(String message, Color violator, Color properColor, Move move, Board board) {
        super(String.format("%s properColor=%s", message, properColor), violator, move, board);
        this.properColor = properColor;
    }
    
    /**
     * 例外を生成します。<br>
     * 
     * @param violator ルールに違反したプレーヤーの色
     * @param properColor 本来の手番
     * @param move ルール違反の手
     * @param board そのときのリバーシ盤
     */
    public IllegalTurnException(Color violator, Color properColor, Move move, Board board) {
        this("本来の手番と異なる色が指定されました。", violator, properColor, move, board);
    }
}
