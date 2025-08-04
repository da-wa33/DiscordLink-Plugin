package me.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordLinkPlugin extends JavaPlugin {
    // 認証コードとプレイヤーUUIDを一時的に保存するMap
    private final Map<String, UUID> codeMap = new HashMap<>();
    // 連携済みのプレイヤーUUIDとDiscordユーザーIDを保存するMap
    private final Map<UUID, String> linkedPlayers = new HashMap<>();

    private JDA jda; // JDAのインスタンスを保持する変数

    @Override
    public void onEnable() {
        getLogger().info("DiscordLinkPluginが有効になりました。");

        // デフォルトのconfig.ymlが存在しない場合、jar内のファイルをコピーして生成する
        saveDefaultConfig();

        // データの読み込み処理を追加
        loadData();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // config.ymlからトークンを読み込む
        String botToken = getConfig().getString("bot-token");

        // トークンが設定されているかチェック
        if (botToken == null || botToken.equals("YourTokenHere") || botToken.isEmpty()) {
            getLogger().severe("==================================================");
            getLogger().severe("Discord Botのトークンがconfig.ymlに設定されていません！");
            getLogger().severe("plugins/DiscordLinkPlugin/config.yml を編集してください。");
            getLogger().severe("プラグインを無効化します。");
            getLogger().severe("==================================================");
            getServer().getPluginManager().disablePlugin(this);
            return; // onEnableの処理をここで中断
        }

        try {
            // JDAインスタンスを構築し、Botをログインさせる
            jda = JDABuilder.createDefault(botToken)
                    // DM受信とメッセージ内容取得のためにインテントを有効化
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .build();

            // 作成したDiscordListenerをJDAにイベントリスナーとして登録
            jda.addEventListener(new DiscordListener(this));

            // JDAが完全に準備できるまで待つ
            jda.awaitReady();
            getLogger().info("Discord Botが正常にログインしました。");

        } catch (Exception e) {
            getLogger().severe("Discord Botのログイン中に何らかのエラーが発生しました。");
            getLogger().severe("トークンが正しいか、ネットワーク接続に問題がないか確認してください。");
            e.printStackTrace(); // 詳細なエラーをコンソールに出力
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // データの保存処理を追加
        saveData();

        // プラグインが無効になったときの処理
        if (jda != null) {
            jda.shutdownNow(); // Botの接続を即座に切断
            getLogger().info("Discord Botがシャットダウンしました。");
        }
        getLogger().info("DiscordLinkPluginが無効になりました。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // コマンド実行者がプレイヤーであり、かつコマンド名が "discord-link" であることを確認
        if (sender instanceof Player player && command.getName().equalsIgnoreCase("discord-link")) {

            // すでに連携済みかチェック
            if (linkedPlayers.containsKey(player.getUniqueId())) {
                player.sendMessage("§c[DiscordLink] あなたのアカウントは既にDiscordと連携済みです。");
                return true;
            }

            // ランダムな8桁の認証コードを生成
            String code = UUID.randomUUID().toString().substring(0, 8);
            codeMap.put(code, player.getUniqueId());

            // プレイヤーにメッセージを送信
            player.sendMessage("§a[DiscordLink] このコードをDiscordボットにDMで送信してください:");
            player.sendMessage("§e" + code);
            getLogger().info("認証コード発行: " + code + " -> " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
            return true;
        }
        return false;
    }

    // 他のクラスからMapにアクセスするための「getter」メソッド
    public Map<String, UUID> getCodeMap() {
        return codeMap;
    }

    public Map<UUID, String> getLinkedPlayers() {
        return linkedPlayers;
    }

    // データ保存用メソッド
    public void saveData() {
        // データ保存用のファイルを作成 (plugins/DiscordLinkPlugin/data.yml)
        File dataFile = new File(getDataFolder(), "data.yml");
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // HashMapの中身をYAML形式に変換
        // UUIDはStringに変換して保存する必要がある
        // 現在のlinkedPlayersをクリアしてから書き込む
        dataConfig.set("players", null); // 古いデータをクリア
        for (Map.Entry<UUID, String> entry : linkedPlayers.entrySet()) {
            dataConfig.set("players." + entry.getKey().toString(), entry.getValue());
        }

        try {
            // ファイルに保存
            dataConfig.save(dataFile);
            getLogger().info("連携データをdata.ymlに保存しました。");
        } catch (IOException e) {
            getLogger().severe("データの保存中にエラーが発生しました。");
            e.printStackTrace();
        }
    }

    // データ読み込み用メソッド
    public void loadData() {
        File dataFile = new File(getDataFolder(), "data.yml");
        // data.ymlが存在しない場合は、何もしないで終了
        if (!dataFile.exists()) {
            return;
        }

        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        // "players"セクションが存在するか確認
        if (!dataConfig.isConfigurationSection("players")) {
            return;
        }

        // YAMLのすべてのキー（UUIDの文字列）を取得
        for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID playerUuid = UUID.fromString(uuidString);
                String discordId = dataConfig.getString("players." + uuidString);

                // 読み込んだデータをHashMapに戻す
                linkedPlayers.put(playerUuid, discordId);
            } catch (IllegalArgumentException e) {
                getLogger().warning(uuidString + " は不正なUUID形式のため、読み込めませんでした。");
            }
        }
        getLogger().info(linkedPlayers.size() + "件の連携データをdata.ymlから読み込みました。");
    }
}