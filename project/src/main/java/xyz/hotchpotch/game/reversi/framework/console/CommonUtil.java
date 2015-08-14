package xyz.hotchpotch.game.reversi.framework.console;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import xyz.hotchpotch.game.reversi.aiplayers.CrazyAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.RandomAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SimplestAIPlayer;
import xyz.hotchpotch.game.reversi.aiplayers.SlowpokeAIPlayer;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.util.ConsoleScanner;

class CommonUtil {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    static List<Class<? extends Player>> playerClasses() {
        // TODO: このバカチョン実装はそのうちどうにかしたい...
        return Arrays.asList(
                ConsolePlayer.class,
                SimplestAIPlayer.class,
                RandomAIPlayer.class,
                SlowpokeAIPlayer.class,
                CrazyAIPlayer.class);
    }
    
    static <E extends Enum<E>> Class<? extends Player> arrangePlayerClass(String str) {
        List<Class<? extends Player>> playerClasses = playerClasses();
        StringBuilder prompt1 = new StringBuilder();
        prompt1.append(String.format("%sを番号で選択してください。", str)).append(BR);
        int n = 0;
        for (Class<? extends Player> playerClass : playerClasses) {
            prompt1.append(String.format("\t%d : %s", ++n, playerClass.getName())).append(BR);
        }
        prompt1.append(String.format("\t0 : その他（自作クラス）")).append(BR);
        prompt1.append("> ");
        
        ConsoleScanner<Integer> scIdx = ConsoleScanner
                .intBuilder(0, playerClasses.size())
                .prompt(prompt1.toString())
                .build();
        int idx = scIdx.get();
        if (0 < idx) {
            return playerClasses.get(idx - 1);
        } else {
            return arrangeCustomPlayerClass();
        }
    }
    
    private static <E extends Enum<E>> Class<? extends Player> arrangeCustomPlayerClass() {
        Predicate<String> judge = s -> {
            try {
                @SuppressWarnings({ "unchecked", "unused" })
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return true;
            } catch (ClassNotFoundException | ClassCastException e) {
                return false;
            }
        };
        Function<String, Class<? extends Player>> converter = s -> {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return playerClass;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        };
        String prompt = "プレーヤークラスの完全修飾クラス名を指定してください（例：jp.co.hoge.MyAIPlayer）" + BR + "> ";
        String complaint = String.format(
                "クラスが見つからないか、%s を implements していません。", Player.class.getName()) + BR;
        ConsoleScanner<Class<? extends Player>> scPlayerClass = ConsoleScanner
                .builder(judge, converter, prompt, complaint).build();
        return scPlayerClass.get();
    }
    
    static long arrangeGivenMillisPerTurn() {
        return ConsoleScanner
                .longBuilder(1, 60000)
                .prompt("一手あたりの制限時間（ミリ秒）を 1～60000（1分） の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    static long arrangeGivenMillisInGame() {
        return ConsoleScanner
                .longBuilder(1, 1800000)
                .prompt("ゲーム内での持ち時間（ミリ秒）を 1～1800000（30分） の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    static int arrangeTimes() {
        return ConsoleScanner
                .intBuilder(1, 30)
                .prompt("対戦回数を 1～30 の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private CommonUtil() {
    }
}
