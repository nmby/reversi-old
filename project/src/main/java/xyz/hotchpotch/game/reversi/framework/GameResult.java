package xyz.hotchpotch.game.reversi.framework;

import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.BoardSnapshot;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.RuleViolationException;

public class GameResult {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    public static GameResult of(GameCondition gameCondition, Board board) {
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(board);
        return null;
    }
    
    public static GameResult of(GameCondition gameCondition, RuleViolationException violation) {
        Objects.requireNonNull(gameCondition);
        Objects.requireNonNull(violation);
        return null;
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final GameCondition gameCondition;
    public final Board board;
    public final Color gainer;
    public final RuleViolationException violation;
    
    private GameResult(GameCondition gameCondition, Board board) {
        this.gameCondition = gameCondition;
        this.board = BoardSnapshot.of(board);
        Map<Color, Integer> counts = this.board.counts();
        int black = counts.get(Color.BLACK);
        int white = counts.get(Color.WHITE);
        if (white < black) {
            gainer = Color.BLACK;
        } else if (black < white) {
            gainer = Color.WHITE;
        } else {
            gainer = null;
        }
        violation = null;
    }
    
    private GameResult(GameCondition gameCondition, RuleViolationException violation) {
        this.gameCondition = gameCondition;
        this.violation = violation;
        gainer = violation.violator.opposite();
        board = null;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        
        if (gainer == null) {
            str.append("引き分けです。").append(BR);
        } else {
            str.append(String.format("%sの勝ちです。", gainer)).append(BR);
        }
        
        if (violation == null) {
            Map<Color, Integer> counts = board.counts();
            int black = counts.get(Color.BLACK);
            int white = counts.get(Color.WHITE);
            str.append(String.format("%s : %d, %s : %d", Color.BLACK, black, Color.WHITE, white));
        } else {
            str.append(String.format("%sがルールに違反しました。", violation.violator)).append(BR);
            str.append(violation.getMessage());
        }
        
        return str.toString();
    }
}
