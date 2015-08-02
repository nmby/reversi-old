package xyz.hotchpotch.game.reversi;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;

/**
 * リバーシゲームのプレーヤーを表します。<br>
 * <br>
 * Player 実装クラスは引数なしのコンストラクタを持つ必要があります。<br>
 * ゲーム実行フレームワークは、ゲーム開始時に Player インスタンスを生成し、ゲームの間中、そのインスタンスを利用します。
 * ゲームの終了とともに、インスタンスは破棄されます。<br>
 * 言い換えると、同じゲーム中は同じインスタンスが使用されること、ひとつのインスタンスが複数のゲームで使いまわされないことが保証されます。<br>
 * <br>
 * ゲーム実行フレームワークは、Player をインスタンス化したのち、一度だけ Player#init(Color, GameCondition) メソッドを実行し、
 * Player にそのプレーヤーの駒の色と、一手当たりの制限時間やゲーム全体での持ち時間、相手の Player クラスなどの
 * ゲームの条件を伝えます。<br>
 * Player 実装クラスはこれらの情報を自身の戦略に役立ててもよいですし、無視しても構いません。<br>
 * <br>
 * Player は、自身の番になるたびに、ゲーム実行フレームワークからの要求に応じて打つ手を返さなければなりません。<br>
 * ルール違反の手を指定したり持ち時間をオーバーしたりした場合は、その時点で負けとなります。<br>
 * <br>
 * ゲーム実行フレームワークが複数スレッドから Player インスタンスを操作することはありません。<br>
 * 
 * @author nmby
 */
public interface Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * プレーヤーにゲームの条件を伝えるためのメソッドです。<br>
     * このメソッドは、ゲーム開始時に一度だけゲーム実行フレームワークから実行されます。<br>
     * Player 実装クラスは、パラメータとして渡された情報を自身の戦略に役立てることができます。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲームの条件
     */
    void init(Color color, GameCondition gameCondition);
    
    /**
     * プレーヤーが自分の手を指定するためのメソッドです。<br>
     * このメソッドは、このプレーヤーの番になるたびに、ゲーム実行フレームワークから実行されます。<br>
     * Player 実装クラスは、正しい手を返さなければなりません。指せる手がない場合は、正しくパスを宣言しなければなりません。<br>
     * ルール違反の手を返した場合、制限時間をオーバーした場合、実行時例外を発生させた場合は
     * いずれもこのプレーヤーの負けとなります。<br>
     * 
     * @param board 現在のリバーシ盤の状態（このリバーシ盤に対する更新操作は行えません）
     * @param color このプレーヤーの駒の色
     * @param givenMillisPerTurn 一手ごとに与えられた時間（ミリ秒）（同じゲーム中、毎回同じ値が渡されます）
     * @param remainingMillisInGame ゲーム内での残り持ち時間（ミリ秒）（自身の消費に応じて、徐々に減っていきます）
     * @return 選択した手
     */
    Move move(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame);
}
