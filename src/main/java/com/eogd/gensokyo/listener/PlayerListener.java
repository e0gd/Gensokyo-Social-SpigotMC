package com.eogd.gensokyo.listener;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final GensokyoSocialPlugin plugin;

    public PlayerListener(GensokyoSocialPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 载入数据
        PlayerData data = plugin.getDataManager().loadPlayerData(player.getUniqueId());

        // 检查未读
        if (!data.getMails().isEmpty()) {
            // 延迟提醒
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("§e=============================");
                    player.sendMessage("§a[系统] 您有 §c" + data.getMails().size() + " §a封未读的系统邮件或离线留言！");
                    player.sendMessage("§a[系统] 请输入 §e/mail read §a查看详细内容。");
                    player.sendMessage("§e=============================");
                }
            }, 40L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 卸载数据
        plugin.getDataManager().unloadPlayerData(event.getPlayer().getUniqueId());
        
        // 暂不退队
    }
}