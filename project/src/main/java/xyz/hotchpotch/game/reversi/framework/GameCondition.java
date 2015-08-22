package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * ゲームの実施条件を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class GameCondition implements Condition<Game>, Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Map<String, String> params;
        
        private SerializationProxy(GameCondition gameCondition) {
            params = gameCondition.params;
        }
        
        private Object readResolve() {
            return of(params);
        }
    }
    
    /**
     * 個々の必須パラメータを指定してゲーム実施条件を生成します。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @return ゲーム実施条件
     * @throws NullPointerException {@code playerBlack}、{@code playerWhite} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame} のいずれかが正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame) {
            
        return of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame, new HashMap<>());
    }
    
    /**
     * 個々の必須パラメータと追加のパラメータを指定してゲーム実施条件を生成します。<br>
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定された値が優先されます。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param params 追加のパラメータが格納された {@code Map}
     * @return ゲーム実施条件
     * @throws NullPointerException {@code playerBlack}、{@code playerWhite}、{@code params}
     *                              のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame} のいずれかが正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Map<String, String> params) {
            
        Objects.requireNonNull(playerBlack);
        Objects.requireNonNull(playerWhite);
        Objects.requireNonNull(params);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d",
                            givenMillisPerTurn, givenMillisInGame));
        }
        
        Map<String, String> copy = new HashMap<>(params);
        copy.put("player.black", playerBlack.getName());
        copy.put("player.white", playerWhite.getName());
        copy.put("givenMillisPerTurn", String.valueOf(givenMillisPerTurn));
        copy.put("givenMillisInGame", String.valueOf(givenMillisInGame));
        
        return new GameCondition(
                playerBlack,
                playerWhite,
                givenMillisPerTurn,
                givenMillisInGame,
                copy);
    }
    
    /**
     * パラメータを一括指定してゲーム実施条件を生成します。<br>
     * {@code params} は以下の必須パラメータを含む必要があります。<br>
     * <ul>
     *   <li>{@code player.black} ： 黒プレーヤーの完全修飾クラス名</li>
     *   <li>{@code player.white} ： 白プレーヤーの完全修飾クラス名</li>
     *   <li>{@code givenMillisPerTurn} ： 一手あたりの制限時間（ミリ秒）</li>
     *   <li>{@code givenMillisInGame} ： ゲーム全体での持ち時間（ミリ秒）</li>
     * </ul>
     * 
     * @param params パラメータが格納された {@code Map}
     * @return ゲーム実施条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 各パラメータの設定内容が不正な場合
     */
    @SuppressWarnings("unchecked")
    public static GameCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        Map<String, String> copy = new HashMap<>(params);
        
        String strPlayerBlack = copy.get("player.black");
        String strPlayerWhite = copy.get("player.white");
        String strGivenMillisPerTurn = copy.get("givenMillisPerTurn");
        String strGivenMillisInGame = copy.get("givenMillisInGame");
        if (strPlayerBlack == null
                || strPlayerWhite == null
                || strGivenMillisPerTurn == null
                || strGivenMillisInGame == null) {
            throw new IllegalArgumentException(
                    String.format("必須パラメータが指定されていません。"
                            + "player.black=%s, player.white=%s, "
                            + "givenMillisPerTurn=%s, givenMillisInGame=%s",
                            strPlayerBlack, strPlayerWhite,
                            strGivenMillisPerTurn, strGivenMillisInGame));
        }
        
        Class<? extends Player> playerBlack;
        Class<? extends Player> playerWhite;
        try {
            playerBlack = (Class<? extends Player>) Class.forName(strPlayerBlack);
            playerWhite = (Class<? extends Player>) Class.forName(strPlayerWhite);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    String.format("プレーヤークラスをロードできません。player.black=%s, player.white=%s",
                            strPlayerBlack, strPlayerWhite),
                    e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("プレーヤークラスは %s を実装する必要があります。"
                            + "player.black=%s, player.white=%s",
                            Player.class.getName(), strPlayerBlack, strPlayerWhite),
                    e);
        }
        
        long givenMillisPerTurn;
        long givenMillisInGame;
        try {
            givenMillisPerTurn = Long.parseLong(strGivenMillisPerTurn);
            givenMillisInGame = Long.parseLong(strGivenMillisInGame);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("整数値が必要です。givenMillisPerTurn=%s, givenMillisInGame=%s",
                            strGivenMillisPerTurn, strGivenMillisInGame));
        }
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d",
                            givenMillisPerTurn, givenMillisInGame));
        }
        
        return new GameCondition(
                playerBlack,
                playerWhite,
                givenMillisPerTurn,
                givenMillisInGame,
                copy);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** プレーヤークラスが格納された {@code Map} */
    public transient final Map<Color, Class<? extends Player>> playerClasses;
    
    /** 一手あたりの制限時間（ミリ秒） */
    public transient final long givenMillisPerTurn;
    
    /** ゲーム全体での持ち時間（ミリ秒） */
    public transient final long givenMillisInGame;
    
    private transient final Map<String, String> params;
    
    private GameCondition(
            Class<? extends Player> playerClassBlack,
            Class<? extends Player> playerClassWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Map<String, String> params) {
            
        Map<Color, Class<? extends Player>> playerClasses = new EnumMap<>(Color.class);
        playerClasses.put(Color.BLACK, playerClassBlack);
        playerClasses.put(Color.WHITE, playerClassWhite);
        this.playerClasses = Collections.unmodifiableMap(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.params = Collections.unmodifiableMap(params);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParams() {
        // Collections#unmodifiableMap でラップしているので直接返して問題ない
        return params;
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
