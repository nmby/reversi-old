package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
    
    private transient final Map<Color, Class<? extends Player>> playerClasses;
    private transient final long givenMillisPerTurn;
    private transient final long givenMillisInGame;
    private transient final Properties properties;
    
    private GameCondition(
            Class<? extends Player> playerClassBlack,
            Class<? extends Player> playerClassWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Properties properties) {
        
        playerClasses = new EnumMap<>(Color.class);
        playerClasses.put(Color.BLACK, playerClassBlack);
        playerClasses.put(Color.WHITE, playerClassWhite);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.properties = properties;
    }
    
    public Class<? extends Player> playerClass(Color color) {
        Objects.requireNonNull(color);
        return playerClasses.get(color);
    }
    
    public long givenMillisPerTurn() {
        return givenMillisPerTurn;
    }
    
    public long givenMillisInGame() {
        return givenMillisInGame;
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
