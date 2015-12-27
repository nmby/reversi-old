package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
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
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /*package*/ static final String KEY_PLAYER_A = "player.a";
    /*package*/ static final String KEY_PLAYER_B = "player.b";
    /*package*/ static final String KEY_MILLIS_PER_TURN = GameCondition.KEY_MILLIS_PER_TURN;
    /*package*/ static final String KEY_MILLIS_IN_GAME = GameCondition.KEY_MILLIS_IN_GAME;
    /*package*/ static final String KEY_TIMES = "times";
    /*package*/ static final String KEY_PRINT_LEVEL = "print.level";
    /*package*/ static final String KEY_AUTO = "auto";
    
    private static final long serialVersionUID = 1L;
    
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Map<String, String> params;
        
        private SerializationProxy(MatchCondition matchCondition) {
            params = matchCondition.params;
        }
        
        private Object readResolve() throws ObjectStreamException {
            try {
                return of(params);
            } catch (RuntimeException e) {
                throw new InvalidObjectException(
                        String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }
    
    /**
     * 個々の必須パラメータを指定してマッチ実施条件を生成します。<br>
     * 
     * @param playerA プレーヤーAのクラス
     * @param playerB プレーヤーBのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @return マッチ実施条件
     * @throws NullPointerException {@code playerA}、{@code playerB} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                  のいずれかが正の整数でない場合
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
     * 個々の必須パラメータと追加のパラメータを指定してマッチ実施条件を生成します。<br>
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定した値が優先されます。<br>
     * 
     * @param playerA プレーヤーAのクラス
     * @param playerB プレーヤーBのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times 対戦回数
     * @param params 追加のパラメータが格納された {@code Map}
     * @return マッチ実施条件
     * @throws NullPointerException {@code playerA}、{@code playerB}、{@code params}
     *                              のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame}、{@code times}
     *                                  のいずれかが正の整数でない場合
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
        copy.put(KEY_PLAYER_A, playerA.getName());
        copy.put(KEY_PLAYER_B, playerB.getName());
        copy.put(KEY_MILLIS_PER_TURN, String.valueOf(givenMillisPerTurn));
        copy.put(KEY_MILLIS_IN_GAME, String.valueOf(givenMillisInGame));
        copy.put(KEY_TIMES, String.valueOf(times));
        
        return new MatchCondition(
                playerA,
                playerB,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    /**
     * パラメータを一括指定してマッチ実施条件を生成します。<br>
     * {@code params} は以下の必須パラメータを含む必要があります。<br>
     * <table border="1">
     *   <tr><th>パラメータ名</th><th>内容</th><th>例</th></tr>
     *   <tr><td>{@code player.a}</td><td>プレーヤーAの完全修飾クラス名</td><td>{@code xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer}</td></tr>
     *   <tr><td>{@code player.b}</td><td>プレーヤーBの完全修飾クラス名</td><td>{@code xyz.hotchpotch.game.reversi.aiplayers.RandomAIPlayer}</td></tr>
     *   <tr><td>{@code givenMillisPerTurn}</td><td>一手あたりの制限時間（ミリ秒）</td><td>1000</td></tr>
     *   <tr><td>{@code givenMillisInGame}</td><td>ゲーム全体での持ち時間（ミリ秒）</td><td>15000</td></tr>
     *   <tr><td>{@code times}</td><td>対戦回数</td><td>10</td></tr>
     * </table>
     * 
     * @param params パラメータが格納された {@code Map}
     * @return マッチ実施条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 必須パラメータが設定されていない場合や各パラメータの設定内容が不正な場合
     */
    public static MatchCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        
        Map<String, String> copy = new HashMap<>(params);
        
        Class<? extends Player> playerA = ConditionUtil.getPlayerClass(copy, KEY_PLAYER_A);
        Class<? extends Player> playerB = ConditionUtil.getPlayerClass(copy, KEY_PLAYER_B);
        long givenMillisPerTurn = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_PER_TURN);
        long givenMillisInGame = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_IN_GAME);
        int times = (int) ConditionUtil.getLongPositiveValue(copy, KEY_TIMES);
        
        return new MatchCondition(
                playerA,
                playerB,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
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
    
    /** ゲーム実施条件が格納された {@code Map} */
    public transient final Map<Entrant, GameCondition> gameConditions;
    
    private transient final Map<String, String> params;
    
    private MatchCondition(
            Class<? extends Player> playerClassA,
            Class<? extends Player> playerClassB,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        assert playerClassA != null;
        assert playerClassB != null;
        assert 0 < givenMillisPerTurn;
        assert 0 < givenMillisInGame;
        assert 0 < times;
        assert params != null;
        
        Map<Entrant, Class<? extends Player>> playerClasses = new EnumMap<>(Entrant.class);
        playerClasses.put(Entrant.A, playerClassA);
        playerClasses.put(Entrant.B, playerClassB);
        this.playerClasses = Collections.unmodifiableMap(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.times = times;
        this.params = Collections.unmodifiableMap(params);
        
        Map<String, String> gameParams = new HashMap<>(params);
        
        if (!gameParams.containsKey(KEY_PRINT_LEVEL)) {
            gameParams.put(KEY_PRINT_LEVEL, "MATCH");
        }
        if (!gameParams.containsKey(KEY_AUTO)) {
            gameParams.put(KEY_AUTO, "true");
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
