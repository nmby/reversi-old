# reversi
コンソールバージョンのリバーシゲームです。  
人間とコンピュータ、およびコンピュータ同士で対戦を行えます。  
自動でコンピュータ同士の総当たり戦を行い、対戦成績を一覧で表示することもできます。  
  
次の標準AIプレーヤーがパッケージに含まれています。
* [SimplestAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/reversi/aiplayers/SimplestAIPlayer.java) ： 盤上を左上から順に走査するAIプレーヤーです。  
* [RandomAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/reversi/aiplayers/RandomAIPlayer.java) ： ランダムに手を選択するAIプレーヤーです。  
* [DepthFirstAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/reversi/aiplayers/DepthFirstAIPlayer.java) ： 深さ優先探索で必勝手を探索するAIプレーヤーです。  
* [BreadthFirstAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/reversi/aiplayers/BreadthFirstAIPlayer.java) ： 幅優先探索で最善手を探索するAIプレーヤーです。  
* [MonteCarloAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/reversi/aiplayers/MonteCarloAIPlayer.java) ： モンテカルロ・シミュレーションにより最善手を選択するAIプレーヤーです。  

AIプレーヤーを自作することも簡単です。
[Player インタフェース](http://nmby.github.io/reversi/api-docs/xyz/hotchpotch/reversi/framework/Player.html) を実装し、
[Player#decide メソッド](http://nmby.github.io/reversi/api-docs/xyz/hotchpotch/reversi/framework/Player.html#decide-xyz.hotchpotch.reversi.core.Board-xyz.hotchpotch.reversi.core.Color-long-long-) をオーバーライドするだけです。  
自作したAIプレーヤーと標準AIプレーヤーを対戦させることもできます。  
  
詳細は [Qiita内の紹介ページ](http://qiita.com/nmby/items/bd44e28d937108fa3eb0)
および [javadoc](http://nmby.github.io/reversi/api-docs/index.html) を参照してください。  
  
## 更新履歴
#### Version 2.0.0 (2016/01/xx)
* ベースパッケージを xyz.hotchpotch.game.reversi から xyz.hotchpotch.reversi に変更
  
#### Version 1.1.0 (2016/01/02)
* ユーティリティパッケージの構成を変更
  
##### Version 1.0.1 (2016/01/02)
* javadocを整備
  
#### Version 1.0.0 (2016/01/01)
* 正式版初版リリース
  
## ライセンス
Licensed under the MIT License, see LICENSE.txt.  
Copyright (c) 2015 nmby  

