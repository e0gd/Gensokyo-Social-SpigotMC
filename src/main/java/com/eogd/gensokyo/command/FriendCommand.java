package com.eogd.gensokyo.command;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendCommand implements CommandExecutor {

    private final GensokyoSocialPlugin plugin;
    // 请求映射
    private final Map<UUID, UUID> friendRequests;

    public FriendCommand(GensokyoSocialPlugin plugin) {
        this.plugin = plugin;
        this.friendRequests = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用好友指令！");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();
        PlayerData myData = plugin.getDataManager().getCachedData(playerUuid);

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /friend add <玩家名>");
                    return true;
                }
                Player targetToAdd = Bukkit.getPlayerExact(args[1]);
                if (targetToAdd == null || !targetToAdd.isOnline()) {
                    player.sendMessage("§c[系统] 找不到该玩家或玩家不在线！");
                    return true;
                }
                if (targetToAdd.getUniqueId().equals(playerUuid)) {
                    player.sendMessage("§c[系统] 你不能添加你自己为好友！");
                    return true;
                }
                if (myData.getFriends().contains(targetToAdd.getUniqueId())) {
                    player.sendMessage("§c[系统] 你们已经是好友了！");
                    return true;
                }
                int maxFriends = plugin.getConfig().getInt("settings.friend-max-limit", 50);
                if (myData.getFriends().size() >= maxFriends) {
                    player.sendMessage("§c[系统] 你的好友列表已满 (" + maxFriends + "人)！");
                    return true;
                }

                friendRequests.put(targetToAdd.getUniqueId(), playerUuid);
                player.sendMessage("§a[系统] 已向 " + targetToAdd.getName() + " 发送了好友请求。");
                targetToAdd.sendMessage("§e[系统] 玩家 §a" + player.getName() + " §e请求添加你为好友！");
                targetToAdd.sendMessage("§e[系统] 输入 §a/friend accept §e来接受请求。");
                break;

            case "accept":
                if (!friendRequests.containsKey(playerUuid)) {
                    player.sendMessage("§c[系统] 你没有收到任何待处理的好友请求！");
                    return true;
                }
                UUID senderUuid = friendRequests.remove(playerUuid);
                Player senderPlayer = Bukkit.getPlayer(senderUuid);
                if (senderPlayer == null || !senderPlayer.isOnline()) {
                    player.sendMessage("§c[系统] 对方已下线，请求失效！");
                    return true;
                }
                
                PlayerData senderData = plugin.getDataManager().getCachedData(senderUuid);
                
                // 双向添加
                myData.addFriend(senderUuid);
                senderData.addFriend(playerUuid);
                
                player.sendMessage("§a[系统] 你已与 " + senderPlayer.getName() + " 成为好友！");
                senderPlayer.sendMessage("§a[系统] " + player.getName() + " 接受了你的好友请求！");
                break;

            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /friend remove <玩家名>");
                    return true;
                }
                // 离线可用
                @SuppressWarnings("deprecation")
                OfflinePlayer targetToRemove = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUuid = targetToRemove.getUniqueId();
                
                if (!myData.getFriends().contains(targetUuid)) {
                    player.sendMessage("§c[系统] 该玩家不在你的好友列表中！");
                    return true;
                }
                
                myData.removeFriend(targetUuid);
                player.sendMessage("§a[系统] 已将 " + targetToRemove.getName() + " 从你的好友列表中移除。");
                
                // 双向移除
                PlayerData targetData = plugin.getDataManager().loadPlayerData(targetUuid);
                targetData.removeFriend(playerUuid);
                // 离线保存
                if (!targetToRemove.isOnline()) {
                    plugin.getDataManager().savePlayerData(targetData);
                    plugin.getDataManager().unloadPlayerData(targetUuid);
                }
                break;

            case "list":
                player.sendMessage("§e=== 你的好友列表 (" + myData.getFriends().size() + ") ===");
                if (myData.getFriends().isEmpty()) {
                    player.sendMessage("§7你还没有添加任何好友。");
                } else {
                    for (UUID fUuid : myData.getFriends()) {
                        OfflinePlayer fPlayer = Bukkit.getOfflinePlayer(fUuid);
                        String status = fPlayer.isOnline() ? "§a[在线]" : "§7[离线]";
                        player.sendMessage(status + " §f" + fPlayer.getName());
                    }
                }
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e=== 好友系统帮助 ===");
        player.sendMessage("§a/friend add <玩家> §7- 发送好友请求");
        player.sendMessage("§a/friend accept §7- 接受好友请求");
        player.sendMessage("§a/friend remove <玩家> §7- 删除好友");
        player.sendMessage("§a/friend list §7- 查看好友列表");
    }
}