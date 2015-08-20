# reversi
コンソールバージョンのリバーシゲームです。  
人間とコンピュータ、およびコンピュータ同士で対戦を行えます。  
自動でコンピュータ同士の総当たり戦を行い、対戦成績を一覧で表示することもできます。  
  
次の標準コンピュータプレーヤーがパッケージに含まれています。
* SimplestAIPlayer ： 盤上を左上から順に走査するAIプレーヤーです。  
* RandomAIPlayer ： ランダムに手を選択するAIプレーヤーです。  
* DepthFirstAIPlayer ： 深さ優先探索で必勝手を探索するAIプレーヤーです。  
* MonteCarloAIPlayer ： モンテカルロ・シミュレーションにより最善手を選択するAIプレーヤーです。  

コンピュータプレーヤーを自作することも簡単です。
Player インタフェースを実装し、Player#decide メソッドをオーバーライドするだけです。  
自作したコンピュータプレーヤーと標準コンピュータプレーヤーを対戦させることもできます。  
  
詳細は [Qiita内の紹介ページ](http://qiita.com/nmby/items/bd44e28d937108fa3eb0)
およびソース内の javadoc を参照してください。  
  
## ライセンス
Licensed under the MIT License, see LICENSE.txt.  
Copyright (c) 2015 nmby  

