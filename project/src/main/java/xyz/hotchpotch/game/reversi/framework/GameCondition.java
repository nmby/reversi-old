package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import xyz.hotchpotch.game.reversi.core.Color;

/**
 * ゲームの実施条件を表すクラスです。<br>
 * 
 * @author nmby
 */
public class GameCondition implements Serializable {
    
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
     * 個々のパラメータを指定してゲーム条件を生成します。<br>
     * 
     * @param playerBlack 黒のプレーヤーのクラス
     * @param playerWhite 白のプレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @return ゲーム条件
     * @throws NullPointerException playerBlack または playerWhite が null の場合
     * @throwsIllegalArgumentException givenMillisPerTurn または givenMillisInGame が正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame) {
            
        Objects.requireNonNull(playerBlack);
        Objects.requireNonNull(playerWhite);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d",
                            givenMillisPerTurn, givenMillisInGame));
        }
        
        Properties properties = new Properties();
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
     * properties は以下のプロパティを含む必要があります。<br>
     *     ・player.black<br>
     *     ・player.white<br>
     *     ・givenMillisPerTurn<br>
     *     ・givenMillisInGame<br>
     * 
     * @param properties ゲーム条件が設定されたプロパティセット
     * @return ゲーム条件
     * @throws NullPointerException properties が null の場合
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
                    String.format("プレーヤークラスはPlayerを実装する必要があります。player.black=%s, player.white=%s",
                            strPlayerBlack, strPlayerWhite),
                    e);
        }
        
        long givenMillisPerTurn;
        long givenMillisInGame;
        try {
            givenMillisPerTurn = Long.valueOf(strGivenMillisPerTurn);
            givenMillisInGame = Long.valueOf(strGivenMillisInGame);
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
    
    public transient final Map<Color, Class<? extends Player>> playerClasses;
    public transient final long givenMillisPerTurn;
    public transient final long givenMillisInGame;
    private transient final Properties properties;
    
    private GameCondition(
            Class<? extends Player> playerClassBlack,
            Class<? extends Player> playerClassWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Properties properties) {
            
        Map<Color, Class<? extends Player>> tmp = new EnumMap<>(Color.class);
        tmp.put(Color.BLACK, playerClassBlack);
        tmp.put(Color.WHITE, playerClassWhite);
        playerClasses = Collections.unmodifiableMap(tmp);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.properties = properties;
    }
    
    public String getProperty(String key) {
        Objects.requireNonNull(key);
        return properties.getProperty(key);
    }
    
    @Override
    public String toString() {
        return properties.toString();
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
