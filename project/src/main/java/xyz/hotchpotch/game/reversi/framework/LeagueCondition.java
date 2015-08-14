package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeSet;

/**
 * リーグの実施条件を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class LeagueCondition implements Condition<League>, Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Properties properties;
        
        private SerializationProxy(LeagueCondition leagueCondition) {
            properties = leagueCondition.properties;
        }
        
        private Object readResolve() {
            return of(properties);
        }
    }
    
    /**
     * 個々の必須パラメータを指定してリーグ条件を生成します。<br>
     * 
     * @param players リーグに参加するプレーヤークラスのリスト
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @return リーグ条件
     * @throws NullPointerException {@code players} が {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                 のいずれかが正の整数でない場合
     */
    public static LeagueCondition of(
            List<Class<? extends Player>> players,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times) {
            
        return of(players, givenMillisPerTurn, givenMillisInGame, times, new HashMap<>());
    }
    
    /**
     * 個々の必須パラメータと追加のパラメータを指定してリーグ条件を生成します。<br>
     * {@code map} に必須パラメータが含まれる場合は、個別に引数で指定された値が優先されます。<br>
     * 
     * @param players リーグに参加するプレーヤークラスのリスト
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @param map 追加のパラメータが格納された {@code Map}
     * @return リーグ条件
     * @throws NullPointerException {@code players}、{@code map} のいずれかが {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                 のいずれかが正の整数でない場合
     */
    public static LeagueCondition of(
            List<Class<? extends Player>> players,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> map) {
            
        Objects.requireNonNull(players);
        Objects.requireNonNull(map);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0 || times <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d, times=%d",
                            givenMillisPerTurn, givenMillisInGame, times));
        }
        
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        for (int i = 0; i < players.size(); i++) {
            properties.setProperty("player." + (i + 1), players.get(i).getName());
        }
        properties.setProperty("givenMillisPerTurn", String.valueOf(givenMillisPerTurn));
        properties.setProperty("givenMillisInGame", String.valueOf(givenMillisInGame));
        properties.setProperty("times", String.valueOf(times));
        
        return new LeagueCondition(
                players,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                properties);
    }
    
    /**
     * パラメータを一括指定してリーグ条件を生成します。<br>
     * {@code properties} は以下のプロパティを含む必要があります。<br>
     * <ul>
     *   <li>{@code player.?} ： プレーヤーの完全修飾クラス名（{@code ?} 部分は数字）</li>
     *   <li>{@code givenMillisPerTurn} ： 一手あたりの制限時間（ミリ秒）</li>
     *   <li>{@code givenMillisInGame} ： ゲーム全体での持ち時間（ミリ秒）</li>
     *   <li>{@code times} ： 対戦回数</li>
     * </ul>
     * 
     * @param properties リーグ条件が設定されたプロパティセット
     * @return リーグ条件
     * @throws NullPointerException {@code properties} が {@code null} の場合
     * @throws IllegalArgumentException 各条件の設定内容が不正の場合
     */
    @SuppressWarnings("unchecked")
    public static LeagueCondition of(Properties properties) {
        Objects.requireNonNull(properties);
        Properties copy = new Properties(properties);
        
        List<String> strPlayers = new ArrayList<>();
        for (Map.Entry<?, ?> entry : copy.entrySet()) {
            String key = (String) entry.getKey();
            if (key.matches("player.\\d+")) {
                strPlayers.add((String) entry.getValue());
            }
            
        }
        String strGivenMillisPerTurn = copy.getProperty("givenMillisPerTurn");
        String strGivenMillisInGame = copy.getProperty("givenMillisInGame");
        String strTimes = copy.getProperty("times");
        if (strPlayers.size() < 2) {
            throw new IllegalArgumentException(String.format(
                    "2つ以上のプレーヤークラスを指定する必要があります。strPlayers=%s", strPlayers));
        }
        if (strGivenMillisPerTurn == null
                || strGivenMillisInGame == null
                || strTimes == null) {
            throw new IllegalArgumentException(
                    String.format("必須パラメータが指定されていません。"
                            + "givenMillisPerTurn=%s, givenMillisInGame=%s, times=%s",
                            strGivenMillisPerTurn, strGivenMillisInGame, strTimes));
        }
        
        List<Class<? extends Player>> players = new ArrayList<>();
        String tmp = null;
        try {
            for (String strPlayer : strPlayers) {
                tmp = strPlayer;
                Class<? extends Player> player = (Class<? extends Player>) Class.forName(strPlayer);
                players.add(player);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format(
                    "プレーヤークラスをロードできません。player.?=%s", tmp), e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "プレーヤークラスは %s を実装する必要があります。player.?=%s", Player.class.getName(), tmp), e);
        }
        
        long givenMillisPerTurn;
        long givenMillisInGame;
        int times;
        try {
            givenMillisPerTurn = Long.parseLong(strGivenMillisPerTurn);
            givenMillisInGame = Long.parseLong(strGivenMillisInGame);
            times = Integer.parseInt(strTimes);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("整数値が必要です。givenMillisPerTurn=%s, givenMillisInGame=%s, times=%s",
                            strGivenMillisPerTurn, strGivenMillisInGame, strTimes));
        }
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0 || times <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d, times=%d",
                            givenMillisPerTurn, givenMillisInGame, times));
        }
        
        return new LeagueCondition(
                players,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** プレーヤークラスが格納された {@code List} */
    public transient final List<Class<? extends Player>> playerClasses;
    
    /** 一手あたりの制限時間（ミリ秒） */
    public transient final long givenMillisPerTurn;
    
    /** ゲーム全体での持ち時間（ミリ秒） */
    public transient final long givenMillisInGame;
    
    /** 対戦回数 */
    public transient final int times;
    
    /** マッチ実行条件が格納された {@code List} */
    public transient final List<MatchCondition> matchConditions;
    
    private transient final Properties properties;
    
    private LeagueCondition(
            List<Class<? extends Player>> playerClasses,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Properties properties) {
            
        this.playerClasses = Collections.unmodifiableList(new ArrayList<>(playerClasses));
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.times = times;
        this.properties = properties;
        
        @SuppressWarnings("unchecked")
        Map<String, String> matchProperties = new HashMap<>((Map<String, String>) properties.clone());
        matchProperties.put("print.level", "LEAGUE");
        List<MatchCondition> matchConditions = new ArrayList<>();
        for (int i = 0; i < playerClasses.size() - 1; i++) {
            MatchCondition matchCondition = MatchCondition.of(
                    playerClasses.get(i),
                    playerClasses.get(i + 1),
                    givenMillisPerTurn,
                    givenMillisInGame,
                    times,
                    matchProperties);
            matchConditions.add(matchCondition);
        }
        this.matchConditions = Collections.unmodifiableList(matchConditions);
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
        StringBuilder str = new StringBuilder();
        for (Map.Entry<?, ?> entry : new TreeSet<>(properties.entrySet())) {
            str.append(String.format("%s=%s", entry.getKey(), entry.getValue())).append(System.lineSeparator());
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
