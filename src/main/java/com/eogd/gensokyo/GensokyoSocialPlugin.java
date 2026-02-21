package com.eogd.gensokyo;

import com.eogd.gensokyo.command.FriendCommand;
import com.eogd.gensokyo.command.MailCommand;
import com.eogd.gensokyo.command.PartyCommand;
import com.eogd.gensokyo.listener.PlayerListener;
import com.eogd.gensokyo.manager.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GensokyoSocialPlugin extends JavaPlugin {

    private static GensokyoSocialPlugin instance;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.dataManager = new DataManager(this);

        // 注册监听
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // 注册指令
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("friend").setExecutor(new FriendCommand(this));
        getCommand("mail").setExecutor(new MailCommand(this));

        getLogger().info("Gensokyo Social (幻想乡社交) 插件已成功启动！");
        getLogger().info("Author: eogd");
    }

    @Override
    public void onDisable() {
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            dataManager.unloadPlayerData(player.getUniqueId());
        }
        getLogger().info("Gensokyo Social 插件已卸载，数据已保存。");
    }

    public static GensokyoSocialPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}