package xyz.hotchpotch.game.reversi.framework;

import java.lang.reflect.Constructor;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;

/**
 * リバーシゲームのプレーヤーを表します。<br>
 * <br>
 * Player 実装クラスは次のいずれかのコンストラクタを持つ必要があります。<br>
 *     A. Color と GameCondition を引数にとるコンストラクタ<br>
 *     B. 引数なしのコンストラクタ<br>
 * ゲーム実行フレームワークは、ゲーム開始時に Player#getPlayerInstance(Class, Color, GameCondition) を使用して
 * Player 実装クラスのインスタンスを生成します。<br>
 * A のコンストラクタでは、そのプレーヤーの駒の色と、一手当たりの制限時間やゲーム全体での持ち時間、相手の Player クラスなどの
 * ゲームの条件が伝えられます。
 * Player 実装クラスはこれらの情報を自身の戦略に役立ててもよいですし、無視しても構いません。<br>
 * <br>
 * ゲームの間中、同じインスタンスが利用され、ゲームの終了とともに破棄されます。
 * 言い換えると、同じゲーム中は同じインスタンスが使用されること、ひとつのインスタンスが複数のゲームで使いまわされないことが保証されます。<br>
 * <br>
 * Player は、自身の番になるたびに、ゲーム実行フレームワークからの要求に応じて打つ手を返さなければなりません。
 * 打つ手がない場合は、正しくパスを宣言しなければなりません。<br>
 * ルール違反の手を指定したり持ち時間をオーバーしたりした場合は、その時点で負けとなります。<br>
 * <br>
 * ゲーム実行フレームワークがひとつの Player インスタンスを複数のスレッドから操作することはありません。<br>
 * 
 * @author nmby
 */
public interface Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * Player 実装クラスのインスタンスを返します。<br>
     * <br>
     * まず、(Color, GameCondition) を引数にとるコンストラクタでのインスタンス化を試みます。<br>
     * 次に、引数なしのコンストラクタでのインスタンス化を試みます。<br>
     * インスタンス化できた場合はそのインスタンスを返し、できなかった場合は例外をスローします。<br>
     * 
     * @param playerClass インスタンス化する Player 実装クラス
     * @param color インスタンス化するプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     * @return Player 実装クラスのインスタンス
     * @throws NullPointerException playerClass, color, gameCondition のいずれかが null の場合
     * @throws ReflectiveOperationException Player 実装クラスのインスタンス化に失敗した場合
     */
    public static Player getPlayerInstance(
            Class<? extends Player> playerClass, Color color, GameCondition gameCondition)
                    throws ReflectiveOperationException {
            
        Objects.requireNonNull(playerClass);
        Objects.requireNonNull(color);
        Objects.requireNonNull(gameCondition);
        
        ReflectiveOperationException e1;
        
        // まずは、(Color, GameCondition) をとるコンストラクタでのインスタンス化を試みる。
        try {
            Constructor<? extends Player> constructor =
                    playerClass.getConstructor(Color.class, GameCondition.class);
            return constructor.newInstance(color, gameCondition);
        } catch (ReflectiveOperationException e) {
            e1 = e;
        }
        
        // 次に、引数なしのコンストラクタでのインスタンス化を試みる。
        // どちらでもダメだったら例外を投げる。
        try {
            Constructor<? extends Player> constructor = playerClass.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            e.addSuppressed(e1);
            throw e;
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * プレーヤーが自分の手を指定するためのメソッドです。<br>
     * このメソッドは、このプレーヤーの番になるたびに、ゲーム実行フレームワークから呼ばれます。<br>
     * Player 実装クラスは正しい手を返さなければなりません。指せる手がない場合は、正しくパスを宣言しなければなりません。<br>
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
