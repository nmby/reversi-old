package xyz.hotchpotch.game.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
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
 * @since 1.0.0
 * @author nmby
 */
public class LeagueCondition implements Condition<League>, Serializable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /*package*/ static final String KEY_PLAYER_N = "player.";
    /*package*/ static final String KEY_MILLIS_PER_TURN = MatchCondition.KEY_MILLIS_PER_TURN;
    /*package*/ static final String KEY_MILLIS_IN_GAME = MatchCondition.KEY_MILLIS_IN_GAME;
    /*package*/ static final String KEY_TIMES = MatchCondition.KEY_TIMES;
    /*package*/ static final String KEY_PRINT_LEVEL = MatchCondition.KEY_PRINT_LEVEL;
    /*package*/ static final String KEY_AUTO = MatchCondition.KEY_AUTO;
    
    private static final long serialVersionUID = 1L;
    
    /**
     * {@link LeagueCondition} のシリアライゼーションプロキシです。<br>
     * 
     * @serial include
     * @since 1.0.0
     * @author nmby
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** @serial 全パラメータが格納されたマップ */
        private final Map<String, String> params;
        
        private SerializationProxy(LeagueCondition leagueCondition) {
            params = leagueCondition.params;
        }
        
        /**
         * 復元された {@code SerializationProxy} に対応する {@link LeagueCondition} オブジェクトを返します。<br>
         * 
         * @serialData 復元された {@link #params} を用いて {@link LeagueCondition} オブジェクトを構築して返します。<br>
         *             {@link LeagueCondition} オブジェクト構築の過程で例外が発生した場合は例外をスローして復元を中止します。
         * @return 復元された {@code SerializationProxy} オブジェクトに対応する {@code LeagueCondition} オブジェクト
         * @throws ObjectStreamException {@link LeagueCondition} オブジェクト構築の過程で例外が発生した場合
         */
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
     * 個々の必須パラメータを指定してリーグ実施条件を生成します。<br>
     * 
     * @param players リーグに参加するプレーヤークラスのリスト
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times それぞれの組み合わせにおける対戦回数
     * @return リーグ実施条件
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
     * 個々の必須パラメータと追加のパラメータを指定してリーグ実施条件を生成します。<br>
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定した値が優先されます。<br>
     * 
     * @param players リーグに参加するプレーヤークラスのリスト
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param times それぞれの組み合わせにおける対戦回数
     * @param params 追加のパラメータが格納された {@code Map}
     * @return リーグ実施条件
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
            copy.put(KEY_PLAYER_N + (i + 1), players.get(i).getName());
        }
        copy.put(KEY_MILLIS_PER_TURN, String.valueOf(givenMillisPerTurn));
        copy.put(KEY_MILLIS_IN_GAME, String.valueOf(givenMillisInGame));
        copy.put(KEY_TIMES, String.valueOf(times));
        
        return new LeagueCondition(
                new ArrayList<>(players),
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    /**
     * パラメータを一括指定してリーグ実施条件を生成します。<br>
     * {@code params} は以下の必須パラメータを含む必要があります。<br>
     * <table border="1">
     *   <caption>必須パラメータ</caption>
     *   <tr><th>パラメータ名</th><th>内容</th><th>例</th></tr>
     *   <tr><td>{@code player.?}</td><td>プレーヤーの完全修飾クラス名（{@code ?} 部分は一意な数字）</td><td>{@code xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer}</td></tr>
     *   <tr><td>{@code givenMillisPerTurn}</td><td>一手あたりの制限時間（ミリ秒）</td><td>{@code 1000}</td></tr>
     *   <tr><td>{@code givenMillisInGame}</td><td>ゲーム全体での持ち時間（ミリ秒）</td><td>{@code 15000}</td></tr>
     *   <tr><td>{@code times}</td><td>それぞれの組み合わせにおける対戦回数</td><td>{@code 10}</td></tr>
     * </table>
     * 
     * @param params パラメータが格納された {@code Map}
     * @return リーグ実施条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 必須パラメータが設定されていない場合やパラメータ値が不正な場合
     */
    public static LeagueCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        
        Map<String, String> copy = new HashMap<>(params);
        
        List<String> playerClassNames = new ArrayList<>();
        for (String key : copy.keySet()) {
            if (key.matches(KEY_PLAYER_N + "\\d+")) {
                playerClassNames.add(copy.get(key));
            }
        }
        if (playerClassNames.size() < 2) {
            throw new IllegalArgumentException(String.format(
                    "2つ以上のプレーヤークラスを指定する必要があります。count of player.? is %d", playerClassNames.size()));
        }
        
        List<Class<? extends Player>> players = new ArrayList<>();
        for (String playerClassName : playerClassNames) {
            Class<? extends Player> player = ConditionUtil.getPlayerClass(playerClassName);
            players.add(player);
        }
        
        long givenMillisPerTurn = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_PER_TURN);
        long givenMillisInGame = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_IN_GAME);
        int times = (int) ConditionUtil.getLongPositiveValue(copy, KEY_TIMES);
        
        return new LeagueCondition(
                players,
                givenMillisPerTurn,
                givenMillisInGame,
                times,
                copy);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** リーグに参加するプレーヤークラスが格納された {@code List} */
    public transient final List<Class<? extends Player>> playerClasses;
    
    /** 一手あたりの制限時間（ミリ秒） */
    public transient final long givenMillisPerTurn;
    
    /** ゲーム全体での持ち時間（ミリ秒） */
    public transient final long givenMillisInGame;
    
    /** それぞれの組み合わせにおける対戦回数 */
    public transient final int times;
    
    /** マッチ実施条件が格納された {@code Map} */
    public transient final Map<Pair, MatchCondition> matchConditions;
    
    private transient final Map<String, String> params;
    
    private LeagueCondition(
            List<Class<? extends Player>> playerClasses,
            long givenMillisPerTurn,
            long givenMillisInGame,
            int times,
            Map<String, String> params) {
            
        assert playerClasses != null;
        assert 2 <= playerClasses.size();
        assert 0 < givenMillisPerTurn;
        assert 0 < givenMillisInGame;
        assert 0 < times;
        assert params != null;
        
        this.playerClasses = Collections.unmodifiableList(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.times = times;
        this.params = Collections.unmodifiableMap(params);
        
        Map<String, String> matchParams = new HashMap<>(params);
        
        if (!matchParams.containsKey(KEY_PRINT_LEVEL)) {
            matchParams.put(KEY_PRINT_LEVEL, "LEAGUE");
        }
        if (!matchParams.containsKey(KEY_AUTO)) {
            matchParams.put(KEY_AUTO, "true");
        }
        
        Map<Pair, MatchCondition> matchConditions = new HashMap<>();
        for (int idxA = 0; idxA < playerClasses.size() - 1; idxA++) {
            for (int idxB = idxA + 1; idxB < playerClasses.size(); idxB++) {
                MatchCondition matchCondition = MatchCondition.of(
                        playerClasses.get(idxA),
                        playerClasses.get(idxB),
                        givenMillisPerTurn,
                        givenMillisInGame,
                        times,
                        matchParams);
                        
                matchConditions.put(Pair.of(idxA, idxB), matchCondition);
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
    
    /**
     * この {@code LeagueCondition} オブジェクトの代わりに、{@link SerializationProxy LeagueCondition.SerializationProxy} オブジェクトを直列化します。<br>
     * 
     * @return この {@code LeagueCondition} オブジェクトの代理となる {@link SerializationProxy} オブジェクト
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    /**
     * {@code LeagueCondition} オブジェクトを直接復元することはできません。<br>
     * {@code LeagueCondition} オブジェクトの復元は {@link SerializationProxy LeagueCondition.SerializationProxy} を通して行う必要があります。<br>
     * 
     * @serialData 例外をスローして復元を中止します。
     * @param stream オブジェクト入力ストリーム
     * @throws InvalidObjectException 直接 {@code LeagueCondition} オブジェクトの復元が試みられた場合
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
