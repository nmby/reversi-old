package xyz.hotchpotch.game.reversi.framework;

import java.lang.reflect.Constructor;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;

/**
 * リバーシゲームのプレーヤーを表します。<br>
 * <br>
 * {@code Player} 実装クラスは次のいずれかの public なコンストラクタを持つ必要があります。<br>
 * <ol>
 *   <li>{@link Color} と {@link GameCondition} を引数にとるコンストラクタ</li>
 *   <li>引数なしのコンストラクタ</li>
 * </ol>
 * ゲーム実行フレームワークは、ゲーム開始時に {@code Player.}{@link #getPlayerInstance(Class, Color, GameCondition)}
 * を使用して {@code Player} 実装クラスのインスタンスを生成します。<br>
 * 1. のコンストラクタでは、そのプレーヤーの駒の色と、制限時間等のゲーム実施条件が伝えられます。
 * {@code Player} 実装クラスはこれらの情報を自身の戦略に役立ててもよいですし、単に無視しても構いません。<br>
 * <br>
 * ゲームの間中、同じプレーヤーインスタンスが利用され、ゲームの終了とともに破棄されます。
 * <br>
 * {@code Player} は、自身の番になるたびに、ゲーム実行フレームワークからの要求に応じて打つ手を返さなければなりません。
 * 打てる手がない場合は、正しくパスを宣言しなければなりません。<br>
 * ルール違反の手を指定したり持ち時間をオーバーしたりした場合は、その時点で負けとなります。<br>
 * <br>
 * ゲーム実行フレームワークがひとつの {@code Player} インスタンスを複数のスレッドから操作することはありません。<br>
 * 
 * @author nmby
 */
public interface Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * {@code Player} 実装クラスのインスタンスを返します。<br>
     * <br>
     * まず、({@link Color}, {@link GameCondition}) を引数にとるコンストラクタでインスタンス化を試みます。<br>
     * 次に、引数なしのコンストラクタでインスタンス化を試みます。<br>
     * インスタンス化できた場合はそのインスタンスを返し、できなかった場合は例外をスローします。<br>
     * 
     * @param playerClass インスタンス化する {@code Player} 実装クラス
     * @param color インスタンス化するプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     * @return Player 実装クラスのインスタンス
     * @throws NullPointerException {@code playerClass}, {@code color}, {@code gameCondition} のいずれかが
     *                              {@code null} の場合
     * @throws ReflectiveOperationException {@code Player} 実装クラスのインスタンス化に失敗した場合
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
     * プレーヤーが自身の手を指定するためのメソッドです。<br>
     * このメソッドは、このプレーヤーの番になるたびに、ゲーム実行フレームワークから呼ばれます。<br>
     * {@code Player} 実装クラスは正しい手（駒を打つ位置）を返さなければなりません。
     * 指せる手がない場合は、{@code null} を返すことにより正しくパスを宣言しなければなりません。<br>
     * ルール違反の手を返した場合、制限時間をオーバーした場合、実行時例外を発生させた場合は
     * いずれもこのプレーヤーの負けとなります。<br>
     * 
     * @param board 現在のリバーシ盤の状態（このリバーシ盤に対する更新操作は行えません）
     * @param color このプレーヤーの駒の色（同じゲーム中、毎回同じ値が渡されます）
     * @param givenMillisPerTurn 一手ごとの制限時間（ミリ秒）（同じゲーム中、毎回同じ値が渡されます）
     * @param remainingMillisInGame ゲーム内での残り持ち時間（ミリ秒）（自身の消費に応じて、徐々に減っていきます）
     * @return 駒を打つ位置（パスの場合は {@code null}）
     */
    Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame);
}
