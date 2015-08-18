package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.framework.Match.Entrant;

/**
 * マッチの実施条件を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class MatchCondition implements Condition<Match>, Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Map<String, String> params;
        
        private SerializationProxy(MatchCondition matchCondition) {
            params = matchCondition.params;
        }
        
        private Object readResolve() {
            return of(params);
        }
    }
    
    /**
     * 個々の必須パラメータを指定してマッチ条件を生成します。<br>
     * 
     * @param playerA プレーヤーAのクラス
     * @param playerB プレーヤーBのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @return マッチ条件
     * @throws NullPointerException {@code playerA}、{@code playerB} のいずれかが {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                 のいずれかが正の整数でない場合
     */
    public static MatchCondition of(
            Class<? extends Player> playerA,
            Class<? extends Player> playerB,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times) {
            
        return of(playerA, playerB, givenMillisPerTurn, givenMillisInGame, times, new HashMap<>());
    }
    
    /**
     * 個々の必須パラメータと追加のパラメータを指定してマッチ条件を生成します。<br>
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定された値が優先されます。<br>
     * 
     * @param playerA プレーヤーAのクラス
     * @param playerB プレーヤーBのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @param params 追加のパラメータが格納された {@code Map}
     * @return マッチ条件
     * @throws NullPointerException {@code playerA}、{@code playerB}、{@code params}
     *                              のいずれかが {@code null} の場合
     * @throwsIllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                 のいずれかが正の整数でない場合
     */
    public static MatchCondition of(
            Class<? extends Player> playerA,
            Class<? extends Player> playerB,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        Objects.requireNonNull(playerA);
        Objects.requireNonNull(playerB);
        Objects.requireNonNull(params);
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0 || times <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d, times=%d",
                            givenMillisPerTurn, givenMillisInGame, times));
        }
        
        Map<String, String> copy = new HashMap<>(params);
        copy.put("player.a", playerA.getName());
        copy.put("player.b", playerB.getName());
        copy.put("givenMillisPerTurn", String.valueOf(givenMillisPerTurn));
        copy.put("givenMillisInGame", String.valueOf(givenMillisInGame));
        copy.put("times", String.valueOf(times));
        
        return new MatchCondition(
                playerA,
                playerB,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    /**
     * パラメータを一括指定してマッチ条件を生成します。<br>
     * {@code params} は以下のパラメータを含む必要があります。<br>
     * <ul>
     *   <li>{@code player.a} ： プレーヤーAの完全修飾クラス名</li>
     *   <li>{@code player.b} ： プレーヤーBの完全修飾クラス名</li>
     *   <li>{@code givenMillisPerTurn} ： 一手あたりの制限時間（ミリ秒）</li>
     *   <li>{@code givenMillisInGame} ： ゲーム全体での持ち時間（ミリ秒）</li>
     *   <li>{@code times} ： 対戦回数</li>
     * </ul>
     * 
     * @param params マッチ条件が設定された {@code Map}
     * @return マッチ条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 各条件の設定内容が不正の場合
     */
    @SuppressWarnings("unchecked")
    public static MatchCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        Map<String, String> copy = new HashMap<>(params);
        
        String strPlayerA = copy.get("player.a");
        String strPlayerB = copy.get("player.b");
        String strGivenMillisPerTurn = copy.get("givenMillisPerTurn");
        String strGivenMillisInGame = copy.get("givenMillisInGame");
        String strTimes = copy.get("times");
        if (strPlayerA == null
                || strPlayerB == null
                || strGivenMillisPerTurn == null
                || strGivenMillisInGame == null
                || strTimes == null) {
            throw new IllegalArgumentException(
                    String.format("必須パラメータが指定されていません。"
                            + "player.a=%s, player.b=%s, "
                            + "givenMillisPerTurn=%s, givenMillisInGame=%s, "
                            + "times=%s",
                            strPlayerA, strPlayerB,
                            strGivenMillisPerTurn, strGivenMillisInGame,
                            strTimes));
        }
        
        Class<? extends Player> playerA;
        Class<? extends Player> playerB;
        try {
            playerA = (Class<? extends Player>) Class.forName(strPlayerA);
            playerB = (Class<? extends Player>) Class.forName(strPlayerB);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    String.format("プレーヤークラスをロードできません。player.a=%s, player.b=%s",
                            strPlayerA, strPlayerB),
                    e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("プレーヤークラスは %s を実装する必要があります。"
                            + "player.a=%s, player.b=%s",
                            Player.class.getName(), strPlayerA, strPlayerB),
                    e);
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
        
        return new MatchCondition(
                playerA,
                playerB,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** プレーヤークラスが格納された {@code Map} */
    public transient final Map<Entrant, Class<? extends Player>> playerClasses;
    
    /** 一手あたりの制限時間（ミリ秒） */
    public transient final long givenMillisPerTurn;
    
    /** ゲーム全体での持ち時間（ミリ秒） */
    public transient final long givenMillisInGame;
    
    /** 対戦回数 */
    public transient final int times;
    
    /** ゲーム実行条件が格納された {@code Map} */
    public transient final Map<Entrant, GameCondition> gameConditions;
    
    private transient final Map<String, String> params;
    
    private MatchCondition(
            Class<? extends Player> playerClassA,
            Class<? extends Player> playerClassB,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        Map<Entrant, Class<? extends Player>> playerClasses = new EnumMap<>(Entrant.class);
        playerClasses.put(Entrant.A, playerClassA);
        playerClasses.put(Entrant.B, playerClassB);
        this.playerClasses = Collections.unmodifiableMap(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.times = times;
        this.params = Collections.unmodifiableMap(params);
        
        Map<String, String> gameParams = new HashMap<>(params);
        
        if (!gameParams.containsKey("print.level")) {
            gameParams.put("print.level", "MATCH");
        }
        if (!gameParams.containsKey("auto")) {
            gameParams.put("auto", "true");
        }
        
        GameCondition gameConditionA = GameCondition.of(
                playerClassA, playerClassB, givenMillisPerTurn, givenMillisInGame, gameParams);
        GameCondition gameConditionB = GameCondition.of(
                playerClassB, playerClassA, givenMillisPerTurn, givenMillisInGame, gameParams);
        Map<Entrant, GameCondition> gameConditions = new EnumMap<>(Entrant.class);
        gameConditions.put(Entrant.A, gameConditionA);
        gameConditions.put(Entrant.B, gameConditionB);
        this.gameConditions = Collections.unmodifiableMap(gameConditions);
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
