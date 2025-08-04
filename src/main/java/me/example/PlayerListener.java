package me.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Minecraftサーバー内でのプレイヤーのイベントを監視するリスナークラス
 */
public class PlayerListener implements Listener {

    private final DiscordLinkPlugin plugin;

    /**
     * コンストラクタ
     * @param plugin メインクラスのインスタンス
     */
    public PlayerListener(DiscordLinkPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤーが移動した時に呼び出されるイベントハンドラ
     * @param event プレイヤー移動イベントの情報
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // プレイヤーが連携済みリストに含まれて「いない」かどうかをチェック
        if (!plugin.getLinkedPlayers().containsKey(player.getUniqueId())) {

            // 視点移動だけでは反応しないように、ブロック座標が変わった場合のみ処理する
            if (hasMovedBlock(event)) {

                // イベントをキャンセルして、移動を元の場所に戻す
                event.setCancelled(true);

                // プレイヤーにメッセージを送信して、理由を知らせる
                player.sendMessage("§cアカウントを連携するまで移動できません。§e/discord-link §cコマンドを実行してください。");
            }
        }
    }

    /**
     * プレイヤーがブロックをまたいで移動したかどうかを判定するヘルパーメソッド
     * @param event プレイヤー移動イベントの情報
     * @return ブロックをまたいでいれば true, そうでなければ false
     */
    private boolean hasMovedBlock(PlayerMoveEvent event) {
        // 移動元(from)と移動先(to)のブロック座標を比較
        return event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ();
    }
}