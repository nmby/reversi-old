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

import xyz.hotchpotch.game.reversi.framework.League.Pair;

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
        
        private final Map<String, String> params;
        
        private SerializationProxy(LeagueCondition leagueCondition) {
            params = leagueCondition.params;
        }
        
        private Object readResolve() {
            return of(params);
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
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                  のいずれかが正の整数でない場合
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
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定された値が優先されます。<br>
     * 
     * @param players リーグに参加するプレーヤークラスのリスト
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @param params 追加のパラメータが格納された {@code Map}
     * @return リーグ条件
     * @throws NullPointerException {@code players}、{@code params} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                  のいずれかが正の整数でない場合
     */
    public static LeagueCondition of(
            List<Class<? extends Player>> players,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        Objects.requireNonNull(players);
        Objects.requireNonNull(params);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0 || times <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d, times=%d",
                            givenMillisPerTurn, givenMillisInGame, times));
        }
        
        Map<String, String> copy = new HashMap<>(params);
        for (int i = 0; i < players.size(); i++) {
            copy.put("player." + (i + 1), players.get(i).getName());
        }
        copy.put("givenMillisPerTurn", String.valueOf(givenMillisPerTurn));
        copy.put("givenMillisInGame", String.valueOf(givenMillisInGame));
        copy.put("times", String.valueOf(times));
        
        return new LeagueCondition(
                players,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    /**
     * パラメータを一括指定してリーグ条件を生成します。<br>
     * {@code params} は以下の必須パラメータを含む必要があります。<br>
     * <ul>
     *   <li>{@code player.?} ： プレーヤーの完全修飾クラス名（{@code ?} 部分は数字）</li>
     *   <li>{@code givenMillisPerTurn} ： 一手あたりの制限時間（ミリ秒）</li>
     *   <li>{@code givenMillisInGame} ： ゲーム全体での持ち時間（ミリ秒）</li>
     *   <li>{@code times} ： 対戦回数</li>
     * </ul>
     * 
     * @param params リーグ条件が設定された {@code Map}
     * @return リーグ条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 各条件の設定内容が不正の場合
     */
    @SuppressWarnings("unchecked")
    public static LeagueCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        Map<String, String> copy = new HashMap<>(params);
        
        List<String> strPlayers = new ArrayList<>();
        for (String key : copy.keySet()) {
            if (key.matches("player.\\d+")) {
                strPlayers.add(copy.get(key));
            }
        }
        String strGivenMillisPerTurn = copy.get("givenMillisPerTurn");
        String strGivenMillisInGame = copy.get("givenMillisInGame");
        String strTimes = copy.get("times");
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
    
    /** マッチ条件が格納された {@code Map} */
    public transient final Map<Pair, MatchCondition> matchConditions;
    
    private transient final Map<String, String> params;
    
    private LeagueCondition(
            List<Class<? extends Player>> playerClasses,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        this.playerClasses = Collections.unmodifiableList(new ArrayList<>(playerClasses));
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.times = times;
        this.params = Collections.unmodifiableMap(params);
        
        Map<String, String> matchParams = new HashMap<>(params);
        
        if (!matchParams.containsKey("print.level")) {
            matchParams.put("print.level", "LEAGUE");
        }
        if (!matchParams.containsKey("auto")) {
            matchParams.put("auto", "true");
        }
        
        Map<Pair, MatchCondition> matchConditions = new HashMap<>();
        for (int idx1 = 0; idx1 < playerClasses.size() - 1; idx1++) {
            for (int idx2 = idx1 + 1; idx2 < playerClasses.size(); idx2++) {
                MatchCondition matchCondition = MatchCondition.of(
                    playerClasses.get(idx1),
                    playerClasses.get(idx2),
                    givenMillisPerTurn,
                    givenMillisInGame,
                    times,
                    matchParams);
                
                matchConditions.put(Pair.of(idx1, idx2), matchCondition);
            }
        }
        this.matchConditions = Collections.unmodifiableMap(matchConditions);
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
