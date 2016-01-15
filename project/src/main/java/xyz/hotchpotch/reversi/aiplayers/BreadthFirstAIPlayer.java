package xyz.hotchpotch.reversi.aiplayers;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

import xyz.hotchpotch.reversi.aiplayers.AIPlayerUtil.LightweightBoard;
import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;
import xyz.hotchpotch.reversi.framework.GameCondition;
import xyz.hotchpotch.reversi.framework.Player;

/**
 * 幅優先探索により最良手を探す {@link Player} の実装です。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。<br>
 * <table border="1">
 *   <caption>指定可能なオプションパラメータ</caption>
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>{@code evaluator}</td><td>{@code String}</td><td>リバーシ盤に対する評価関数のクラス名<br>
 *           （{@link ToIntBiFunction}{@code <LightweightBoard, Color>} 実装クラス名）</td><td>（なし）</td></tr>
 *   <tr><td>{@code seed}</td><td>{@code long}</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 *   <tr><td>{@code margin1}</td><td>{@code long}</td><td>探索を切り上げる余裕時間（ミリ秒）</td><td>{@code 50}</td></tr>
 *   <tr><td>{@code debug}</td><td>{@code boolean}</td><td>デバッグ出力の有無</td><td>{@code false}</td></tr>
 * </table>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class BreadthFirstAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /** 自身と相手の石の数の差に応じてリバーシ盤を評価する評価関数です。 */
    private static final ToIntBiFunction<LightweightBoard, Color> evaluator1 = (b, c) -> {
        int mine = (int) Point.parallelStream().filter(p -> b.colors[p.ordinal()] == c).count();
        int his = (int) Point.parallelStream().filter(p -> b.colors[p.ordinal()] == c.opposite()).count();
        return mine - his;
    };
    
    /** 自身と相手の石を置ける位置の数の差に応じてリバーシ盤を評価する評価関数です。 */
    private static final ToIntBiFunction<LightweightBoard, Color> evaluator2 = (b, c) -> {
        int mine = (int) Point.parallelStream().filter(p -> Rule.canPutAt(b, c, p)).count();
        int his = (int) Point.parallelStream().filter(p -> Rule.canPutAt(b, c.opposite(), p)).count();
        return mine - his;
    };
    
    /** {@link #evaluator1} と {@link #evaluator2} による評価値を単純加算する評価関数です。 */
    @SuppressWarnings("unused")
    private static final ToIntBiFunction<LightweightBoard, Color> evaluator3 = (b, c) -> {
        return evaluator1.applyAsInt(b, c) + evaluator2.applyAsInt(b, c);
    };
    
    /** ゲームの序盤～中盤は {@link #evaluator2}、終盤は {@link #evaluator1} により評価を行う評価関数です。 */
    private static final ToIntBiFunction<LightweightBoard, Color> evaluator4 = (b, c) -> {
        int blankCells = (int) Arrays.stream(b.colors).filter(color -> color == null).count();
        if (blankCells <= 10) {
            return evaluator1.applyAsInt(b, c);
        } else {
            return evaluator2.applyAsInt(b, c);
        }
    };
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Random random;
    private final Color myColor;
    private final long margin1;
    private final boolean debug;
    
    private final ToIntBiFunction<LightweightBoard, Color> evaluator;
    private final Queue<Node> queue = new ArrayDeque<>();
    private Node root;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの石の色
     * @param gameCondition ゲーム実施条件
     */
    public BreadthFirstAIPlayer(Color color, GameCondition gameCondition) {
        myColor = color;
        
        // 動作制御用パラメータの取得
        Optional<ToIntBiFunction<LightweightBoard, Color>> evaluator =
                AIPlayerUtil.getParameter(gameCondition, "evaluator", s -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends ToIntBiFunction<LightweightBoard, Color>> evalClass =
                                (Class<? extends ToIntBiFunction<LightweightBoard, Color>>) Class.forName(s);
                        return evalClass.newInstance();
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        return null;
                    }
                });
        this.evaluator = evaluator.isPresent() ? evaluator.get() : evaluator4;
        Optional<Long> seed = AIPlayerUtil.getLongParameter(gameCondition, "seed");
        random = seed.isPresent() ? new Random(seed.get()) : new Random();
        margin1 = AIPlayerUtil.getLongParameter(gameCondition, "margin1").filter(v -> 0 < v).orElse(50L);
        debug = AIPlayerUtil.getBooleanParameter(gameCondition, "debug").orElse(false);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、幅優先探索により最良手を探します。<br>
     */
    // ホントのことを言うと相手の番の間も別スレッドで計算を続けられるのだけれど、
    // なんとなくマナー違反な気がするし実装も面倒になるので、自分の番の間だけ計算を行う方式にした。
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        assert color == myColor;
        
        Instant start = Instant.now();
        LightweightBoard currBoard = new LightweightBoard(board);
        
        if (debug) {
            printStat("不要ノード削除前");
        }
        
        // 探索ツリーの中から新たなルートとなるべきノードを探して設定する。
        Node newRoot = search(color, currBoard);
        if (newRoot != null) {
            // 探索ツリーの中から目的のノードが見つかった場合
            root = newRoot;
            root.parent = null;
            
            // キューの中に残っている不要になったノードを削除する。
            Iterator<Node> itr = queue.iterator();
            while (itr.hasNext()) {
                if (!itr.next().isAlive()) {
                    itr.remove();
                }
            }
            queue.remove(root);
            
        } else {
            // 探索ツリーの中から目的のノードが見つからなかった場合
            queue.clear();
            root = new Node(null, currBoard, color);
        }
        
        if (debug) {
            printStat("不要ノード削除後");
        }
        
        // 最低限ルートノードの計算は必要なので、未計算の場合は計算する。
        if (root.next == null) {
            root.calc();
        }
        
        // 時間の許す限り、キューの中のノードについて計算する。
        int blankCells = (int) Point.parallelStream().filter(p -> board.colorAt(p) == null).count();
        int myTurns = (blankCells + 1) / 2;
        long millisForThisTurn = Long.min(givenMillisPerTurn, remainingMillisInGame / myTurns);
        Instant deadline = start.plusMillis(millisForThisTurn - margin1);
        
        while (!queue.isEmpty() && Instant.now().isBefore(deadline)) {
            queue.poll().calc();
        }
        
        if (debug) {
            printStat("探索実施後　　　");
        }
        
        // ルートノードにとっての最善手を返す。
        if (debug) {
            printScores();
        }
        return root.bestPoint();
    }
    
    /**
     * 探索ツリーの中から現在の手番とリバーシ盤に対応するノードを見つけて返す。<br>
     * 
     * @param currColor 現在の手番
     * @param currBoard 現在のリバーシ盤
     * @return 新たなルートとなるべき、現在の手番とリバーシ盤に対応するノード（見つからない場合は {@code null}）
     */
    private Node search(Color currColor, LightweightBoard currBoard) {
        assert currColor != null;
        assert currBoard != null;
        
        if (root == null) {
            return null;
        }
        Queue<Node> searchQueue = new ArrayDeque<>();
        searchQueue.add(root);
        
        while (!searchQueue.isEmpty()) {
            Node node = searchQueue.poll();
            
            if (node.color == currColor && node.board.equals(currBoard)) {
                return node;
            }
            if (node.next != null) {
                searchQueue.addAll(node.next.values());
            }
        }
        return null;
    }
    
    /**
     * デバッグ用に、探索ノードとキューの情報を標準出力に出力する。<br>
     * 
     * @param comment コメント
     */
    private void printStat(String comment) {
        System.out.println(String.format(
                "%s : 深さ %d, ノード数 %d, キュー %d",
                comment,
                root == null ? 0 : root.maxDepth(),
                root == null ? 0 : root.countNodes(),
                queue.size()));
    }
    
    /**
     * デバッグ用に、今回の手を判断する根拠となったスコアを標準出力に出力する。<br>
     */
    private void printScores() {
        System.out.println();
        System.out.println("＜スコア＞");
        
        if (root == null) {
            System.out.println("root == null");
        } else if (root.next == null) {
            System.out.println("root.next == null");
        } else {
            root.next.entrySet().stream()
                    .sorted((e1, e2) -> {
                        if (e1.getValue().score != e2.getValue().score) {
                            return Integer.compare(e2.getValue().score, e1.getValue().score);
                        }
                        assert e1.getKey() != null;
                        return e1.getKey().compareTo(e2.getKey());
                    })
                    .forEach(e -> {
                        System.out.println(String.format(
                                "%s : %d",
                                e.getKey() == null ? "PASS" : e.getKey().toStringKindly(),
                                e.getValue().score));
                    });
        }
        System.out.println();
    }
    
    /**
     * 探索ツリーを構成するノードです。<br>
     * 
     * @author nmby
     */
    private class Node {
        
        /** このノードが表すゲーム状態のリバーシ盤 */
        private final LightweightBoard board;
        
        /** このノードが表すゲーム状態の手番 */
        private final Color color;
        
        /** 親ノードへのリンク */
        private Node parent;
        
        /** 子ノードへのリンク */
        private Map<Point, Node> next;
        
        /** （このノードが表す手番ではなく）この {@code BreadthFirstAIPlayer} の色から見たスコア（スコアが高いほど有利な状態とみなされる） */
        private int score;
        
        private Node(Node parent, LightweightBoard board, Color color) {
            assert board != null;
            assert color != null;
            
            this.board = board;
            this.color = color;
            this.parent = parent;
            score = evaluator.applyAsInt(board, color);
        }
        
        /**
         * このノードがルート配下にあるかを返す。<br>
         * 
         * @return このノードがルート配下にある場合は {@code true}
         */
        private boolean isAlive() {
            if (this == root) {
                return true;
            }
            if (parent == null) {
                return false;
            }
            return parent.isAlive();
        }
        
        /**
         * 手をひとつ進めた子ノードを作成してキューに入れるとともに、自身のスコアを再計算する。<br>
         */
        private void calc() {
            Map<Point, Node> next = Point.parallelStream()
                    .filter(p -> Rule.canPutAt(board, color, p))
                    .collect(Collectors.toMap(Function.identity(), p -> {
                        LightweightBoard nextBoard = new LightweightBoard(board);
                        nextBoard.apply(Move.of(color, p));
                        return new Node(this, nextBoard, color.opposite());
                    }));
                    
            if (!next.isEmpty()) {
                this.next = next;
                reCalc();
                queue.addAll(next.values());
                
            } else if (Rule.canPut(board, color.opposite())) {
                Node node = new Node(this, board, color.opposite());
                this.next = new HashMap<>();
                this.next.put(null, node);
                queue.add(node);
            }
        }
        
        /**
         * 自ノードのスコアを子ノードのスコアに基づいて再計算する。<br>
         */
        private void reCalc() {
            assert next != null;
            int newScore;
            
            if (color == myColor) {
                newScore = next.values().stream().map(n -> n.score)
                        .max(Comparator.naturalOrder())
                        .orElseThrow(AssertionError::new);
            } else {
                newScore = next.values().stream().map(n -> n.score)
                        .min(Comparator.naturalOrder())
                        .orElseThrow(AssertionError::new);
            }
            
            // スコアが変わる場合は親ノードへも再計算を要求する。
            if (score != newScore) {
                score = newScore;
                if (parent != null) {
                    parent.reCalc();
                }
            }
        }
        
        /**
         * このノードから見た最善手を返す。<br>
         * このノードの計算が済んでいない場合はランダムに返す。<br>
         * 
         * @return このノードから見た最善手（パスの場合は {@code null}）
         */
        private Point bestPoint() {
            assert Rule.isGameOngoing(board);
            assert next != null;
            
            Point[] bestPoints = next.entrySet().stream()
                    .filter(e -> e.getValue().score == score)
                    .map(Map.Entry::getKey)
                    .toArray(Point[]::new);
                    
            assert 0 < bestPoints.length;
            
            return bestPoints[random.nextInt(bestPoints.length)];
        }
        
        /**
         * このノード配下の、自身を含むノードの数を返します。<br>
         * 
         * @return このノード配下の、自身を含むノードの数
         */
        private int countNodes() {
            if (next == null) {
                return 1;
            } else {
                return 1 + next.values().parallelStream().collect(Collectors.summingInt(Node::countNodes));
            }
        }
        
        /**
         * このノード配下の、自身を含む探索ツリーの最大の深さを返します。<br>
         * 
         * @return このノード配下の、自身を含む探索ツリーの最大の深さ
         */
        private int maxDepth() {
            if (next == null) {
                return 1;
            } else {
                return 1 + next.values().parallelStream().map(Node::maxDepth).max(Comparator.naturalOrder()).orElse(0);
            }
        }
    }
}
