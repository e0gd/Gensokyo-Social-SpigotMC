package com.eogd.gensokyo.api;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.data.Mail;
import com.eogd.gensokyo.data.Party;
import com.eogd.gensokyo.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class GensokyoAPI {

    // 获取队伍
    public static Party getPlayerParty(Player player) {
        UUID uuid = player.getUniqueId();
        for (Party party : GensokyoSocialPlugin.getInstance().getDataManager().getActiveParties().values()) {
            if (party.getMembers().contains(uuid)) {
                return party;
            }
        }
        return null;
    }

    // 发送邮件
    public static void sendMail(UUID targetPlayer, String sender, String content) {
        PlayerData data = GensokyoSocialPlugin.getInstance().getDataManager().loadPlayerData(targetPlayer);
        data.addMail(new Mail(sender, content, System.currentTimeMillis()));
        // 离线保存
        if (org.bukkit.Bukkit.getPlayer(targetPlayer) == null) {
            GensokyoSocialPlugin.getInstance().getDataManager().savePlayerData(data);
            GensokyoSocialPlugin.getInstance().getDataManager().unloadPlayerData(targetPlayer);
        }
    }

    // 获取好友
    public static List<UUID> getFriends(Player player) {
        PlayerData data = GensokyoSocialPlugin.getInstance().getDataManager().getCachedData(player.getUniqueId());
        if (data != null) {
            return data.getFriends();
        }
        return new java.util.ArrayList<>();
    }
}