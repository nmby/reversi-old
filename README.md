# reversi
コンソールバージョンのリバーシゲームです。  
人間とコンピュータ、およびコンピュータ同士で対戦を行えます。  
自動でコンピュータ同士の総当たり戦を行い、対戦成績を一覧で表示することもできます。  
  
次の標準AIプレーヤーがパッケージに含まれています。
* [SimplestAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/game/reversi/aiplayers/SimplestAIPlayer.java) ： 盤上を左上から順に走査するAIプレーヤーです。  
* [RandomAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/game/reversi/aiplayers/RandomAIPlayer.java) ： ランダムに手を選択するAIプレーヤーです。  
* [DepthFirstAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/game/reversi/aiplayers/DepthFirstAIPlayer.java) ： 深さ優先探索で必勝手を探索するAIプレーヤーです。  
* [MonteCarloAIPlayer](https://github.com/nmby/reversi/blob/master/project/src/main/java/xyz/hotchpotch/game/reversi/aiplayers/MonteCarloAIPlayer.java) ： モンテカルロ・シミュレーションにより最善手を選択するAIプレーヤーです。  

AIプレーヤーを自作することも簡単です。
[Player インタフェース](http://nmby.github.io/reversi/api-docs/xyz/hotchpotch/game/reversi/framework/Player.html) を実装し、Player#decide メソッドをオーバーライドするだけです。  
自作したAIプレーヤーと標準AIプレーヤーを対戦させることもできます。  
  
詳細は [Qiita内の紹介ページ](http://qiita.com/nmby/items/bd44e28d937108fa3eb0)
および [javadoc](http://nmby.github.io/reversi/api-docs/index.html) を参照してください。  
  
## ライセンス
Licensed under the MIT License, see LICENSE.txt.  
Copyright (c) 2015 nmby  

