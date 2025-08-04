# DiscordLink-Plugin

## ✅ 主な機能
- Discordアカウント連携: `/discord-link`コマンドで発行される一時的なコードを使って、MinecraftアカウントとDiscordアカウントを安全に連携します。
  
- 行動制限（強制連携）: 連携が完了していないプレイヤーは、サーバー内で移動することができません。


## ⚙️ 動作要件
サーバー: Paper (またはそのフォークサーバー) 1.21.1 以降


## インストールする前に

このプラグインはDiscordSRVなどのDiscordMinecraft認証プラグインと相性が悪いです

そのためDiscordSRVも使う方はDiscordSRVなどのプラグインの設定の認証を無効化するか、違うボットを設定の上ご利用ください

## 🚀 インストール手順
[Releases](https://github.com/da-wa33/DiscordLink-Plugin/releases/latest)ページから最新のDiscordLinkPlugin-vX.X.jarをダウンロードします。

ダウンロードした.jarファイルを、あなたのMinecraftサーバーのpluginsフォルダに配置します。

一度サーバーを起動します。`plugins/DiscordLinkPlugin/config.yml`という設定ファイルが自動で生成されます。

サーバーを停止します。

次に進み、Discord Botとプラグインの設定を行ってください。


## 🔧 設定方法
設定は大きく分けて2段階あります。「Discord Botの準備」と「プラグインの設定」です。

**1. Discord Botの準備**

既存のボットを使用する際はTokenの設定だけでOKです

[Discord Developer Portal](https://discord.com/developers/applications)にアクセスし、Discordアカウントでログインします。
右上の「New Application」ボタンを押し、Botの名前（例: MyServer Discord-MC Link Bot）を決めて作成します。

左側のメニューから「Bot」タブを選択します。

Privileged Gateway Intents のセクションにある、以下の2つのトグルを**必ず有効（ON）**にしてください。
```
MESSAGE CONTENT INTENT
SERVER MEMBERS INTENT
```
TOKENセクションにある「Reset Token」ボタンを押し、表示されたトークンをコピーします。このトークンはパスワードと同じくらい重要です。絶対に他人に教えたり、公開したりしないでください。

**2. プラグインの設定**

サーバーの`plugins/DiscordLinkPlugin/config.yml`ファイルをテキストエディタで開きます。
ファイルの中身を以下のように編集します。

```
# Discord Developer Portalで取得したBotのトークンを、このシングルクォーテーションの間に貼り付けてください。
bot-token: 'ここにあなたのDiscord Botトークンを貼り付け'
```

## 🎮 使い方
Minecraftサーバーに初めて参加すると、ユーザーは移動することができません。

チャット欄で /discord-link コマンドを実行します。

画面に8桁の認証コードが表示されます。（例: a1b2c3d4）

Discordを開き、サーバーが指定したBot（例: MyServer Link Bot）に、その認証コードをダイレクトメッセージ（DM）で送信します。

Botから「✅ Minecraftアカウントとの連携が完了しました！」と返信が来たら認証成功です。

Minecraftのゲーム画面に戻ると、自由に移動できるようになっています！

### 📝 コマンド

|コマンド | 説明	|　パーミッション |（権限ノード） |
|--------|------|----------------|------------|
| /discord-link |	Discord連携用の認証コードを発行します。 | discordlink.command.link  | (デフォルトで全員に許可) |


ファイルを保存し、サーバーを起動します。コンソールに「Discord Botが正常にログインしました。」と表示されれば設定完了です。
