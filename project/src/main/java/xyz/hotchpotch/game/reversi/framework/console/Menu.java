package xyz.hotchpotch.game.reversi.framework.console;

import java.util.function.Supplier;

import xyz.hotchpotch.util.ConsoleScanner;

/**
 * このアプリケーションのメニューを表す列挙型です。<br>
 * このアプリケーションのエントリ・ポイントです。<br>
 * 
 * @author nmby
 */
public enum Menu {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /** ゲーム：2プレーヤーで1回対戦します。 */
    GAME("2プレーヤーで1回対戦します。", ConsoleGame::arrange),
    
    /** マッチ：2プレーヤーで黒白交代しながら複数回対戦します。 */
    MATCH("2プレーヤーで黒白交代しながら複数回対戦します。", ConsoleMatch::arrange),
    
    /** リーグ：複数プレーヤーでそれぞれ複数回、総当たり戦を行います。 */
    LEAGUE("複数プレーヤーでそれぞれ複数回、総当たり戦を行います。", ConsoleLeague::arrange),
    
    /** 終了：このアプリケーションを終了します。 */
    EXIT("終了します。", null) {
        @Override
        public void execute() {
            System.exit(0);
        }
    };
    
    /**
     * このアプリケーションのエントリ・ポイントです。<br>
     * 
     * @param args 起動パラメータ（不使用）
     */
    public static void main(String[] args) {
        ConsoleScanner<Menu> sc = ConsoleScanner.enumBuilder(Menu.class).build();
        while (true) {
            Menu menu = sc.get();
            menu.execute();
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final String description;
    private final Supplier<ConsolePlayable<?>> arranger;
    
    private Menu(String description, Supplier<ConsolePlayable<?>> arranger) {
        this.description = description;
        this.arranger = arranger;
    }
    
    /**
     * このメニューを実行します。<br>
     */
    public void execute() {
        ConsolePlayable<?> playable = arranger.get();
        playable.play();
    }
    
    /**
     * このメニューの説明を返します。<br>
     * 
     * @return このメニューの説明
     */
    @Override
    public String toString() {
        return String.format("%s - %s", name(), description);
    }
}
