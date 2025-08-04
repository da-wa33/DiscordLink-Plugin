package me.example;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DiscordListener extends ListenerAdapter {

    private final DiscordLinkPlugin plugin;

    // コンストラクタでメインクラスのインスタンスを受け取る
    public DiscordListener(DiscordLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // メッセージがDMから送られてきたものであり、かつ、送信者がBotでないことを確認
        if (event.isFromType(ChannelType.PRIVATE) && !event.getAuthor().isBot()) {
            String code = event.getMessage().getContentRaw(); // 送信されたメッセージ（認証コード）を取得
            User discordUser = event.getAuthor(); // メッセージの送信者情報を取得

            // 認証コードが有効かどうかチェック
            if (plugin.getCodeMap().containsKey(code)) {
                // 認証成功の処理
                UUID playerUuid = plugin.getCodeMap().get(code); // コードに対応するプレイヤーのUUIDを取得

                // 連携情報を保存
                plugin.getLinkedPlayers().put(playerUuid, discordUser.getId());

                // 使用済みの認証コードを削除
                plugin.getCodeMap().remove(code);

                // DiscordのDMに成功メッセージを送信
                discordUser.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("✅ Minecraftアカウントとの連携が完了しました！").queue();
                });

                // Minecraftのプレイヤーがオンラインなら、ゲーム内にもメッセージを送信
                Player player = plugin.getServer().getPlayer(playerUuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§a[DiscordLink] §e" + discordUser.getName() + "§a とのアカウント連携が完了しました！");
                }

                plugin.getLogger().info(discordUser.getName() + " と " + playerUuid + " のアカウント連携が完了しました。");

            } else {
                // 認証失敗の処理
                // 連携済みかどうかの確認などはここに追加できるが、今回はシンプルに失敗メッセージのみ
                discordUser.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("❌ 認証コードが正しくありません。Minecraftサーバー内で `/discord-link` コマンドを再度実行して、新しいコードを取得してください。").queue();
                });
            }
        }
    }
}