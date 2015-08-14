package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
        
        private final Properties properties;
        
        private SerializationProxy(GameCondition gameCondition) {
            properties = gameCondition.properties;
        }
        
        private Object readResolve() {
            return of(properties);
        }
    }
    
    /**
     * 個々の必須パラメータを指定してゲーム条件を生成します。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @return ゲーム条件
     * @throws NullPointerException {@code playerBlack} または {@code playerWhite} が {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn} または {@code givenMillisInGame} が正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame) {
            
        return of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame, new HashMap<>());
    }
    
    /**
     * 個々の必須パラメータと追加のパラメータを指定してゲーム条件を生成します。<br>
     * {@code map} に必須パラメータが含まれる場合は、個別に引数で指定された値が優先されます。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param map 追加のパラメータが格納された {@code Map}
     * @return ゲーム条件
     * @throws NullPointerException {@code playerBlack}、{@code playerWhite}、{@code map}
     *                              のいずれかが {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn} または {@code givenMillisInGame} が正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Map<String, String> map) {
            
        Objects.requireNonNull(playerBlack);
        Objects.requireNonNull(playerWhite);
        Objects.requireNonNull(map);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d",
                            givenMillisPerTurn, givenMillisInGame));
        }
        
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        properties.setProperty("player.black", playerBlack.getName());
        properties.setProperty("player.white", playerWhite.getName());
        properties.setProperty("givenMillisPerTurn", String.valueOf(givenMillisPerTurn));
        properties.setProperty("givenMillisInGame", String.valueOf(givenMillisInGame));
        
        return new GameCondition(
                playerBlack,
                playerWhite,
                givenMillisPerTurn,
                givenMillisInGame,
                properties);
    }
    
    /**
     * パラメータを一括指定してゲーム条件を生成します。<br>
     * {@code properties} は以下のプロパティを含む必要があります。<br>
     * <ul>
     *   <li>{@code player.black} ： 黒プレーヤーの完全修飾クラス名</li>
     *   <li>{@code player.white} ： 白プレーヤーの完全修飾クラス名</li>
     *   <li>{@code givenMillisPerTurn} ： 一手あたりの制限時間（ミリ秒）</li>
     *   <li>{@code givenMillisInGame} ： ゲーム全体での持ち時間（ミリ秒）</li>
     * </ul>
     * 
     * @param properties ゲーム条件が設定されたプロパティセット
     * @return ゲーム条件
     * @throws NullPointerException {@code properties} が {@code null} の場合
     * @throws IllegalArgumentException 各条件の設定内容が不正の場合
     */
    @SuppressWarnings("unchecked")
    public static GameCondition of(Properties properties) {
        Objects.requireNonNull(properties);
        Properties copy = new Properties(properties);
        
        String strPlayerBlack = copy.getProperty("player.black");
        String strPlayerWhite = copy.getProperty("player.white");
        String strGivenMillisPerTurn = copy.getProperty("givenMillisPerTurn");
        String strGivenMillisInGame = copy.getProperty("givenMillisInGame");
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
    
    private transient final Properties properties;
    
    private GameCondition(
            Class<? extends Player> playerClassBlack,
            Class<? extends Player> playerClassWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Properties properties) {
            
        Map<Color, Class<? extends Player>> playerClasses = new EnumMap<>(Color.class);
        playerClasses.put(Color.BLACK, playerClassBlack);
        playerClasses.put(Color.WHITE, playerClassWhite);
        this.playerClasses = Collections.unmodifiableMap(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.properties = properties;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException {@code key} が {@code null} の場合
     * @see Properties#getProperty(String)
     */
    @Override
    public String getProperty(String key) {
        Objects.requireNonNull(key);
        return properties.getProperty(key);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getProperties() {
        return new Properties(properties);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toStringKindly() {
        // Properties ってナンでこんなに使いにくいンだ ?!
        StringBuilder str = new StringBuilder();
        
        Set<?> keys = properties.keySet();
        @SuppressWarnings("unchecked")
        SortedSet<String> sortedKeys = new TreeSet<>((Set<String>) keys);
        
        for (String key : sortedKeys) {
            str.append(String.format("%s=%s", key, properties.getProperty(key))).append(System.lineSeparator());
        }
        return str.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toStringInLine() {
        return properties.toString();
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
