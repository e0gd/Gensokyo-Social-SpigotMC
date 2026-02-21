# 幻想乡社交 SpigotMC

幻想乡社交 是一款为 SpigotMC 1.20 编写的轻量级社交系统插件。本插件提供了基础的好友系统、队伍系统以及离线邮件（留言）功能，并为第三方插件开发者提供了高度解耦的静态 API 接口。

## 玩家指令 (Commands)

本插件提供三套核心指令组。玩家无需管理员权限即可使用以下所有功能。

### 队伍系统 (/party 或 /team, /p)
队伍系统支持多玩家组队，并在队伍频道内共享系统广播与队长移交机制。

* `/party create` - 创建一个队伍（创建者自动成为队长）。
* `/party invite <玩家名>` - 邀请在线玩家加入当前队伍（仅队长可用）。
* `/party accept` - 接受收到的队伍邀请。
* `/party leave` - 离开当前队伍。若队长离开，系统将自动移交队长权限。
* `/party kick <玩家名>` - 将特定玩家踢出队伍（仅队长可用）。
* `/party list` - 查看当前队伍内所有成员的状态与身份。

### 好友系统 (/friend 或 /f)
好友系统支持双向确认，并允许对离线玩家进行数据操作。

* `/friend add <玩家名>` - 向目标玩家发送好友请求。
* `/friend accept` - 接受最近收到的一条好友请求。
* `/friend remove <玩家名>` - 将目标玩家从好友列表中移除（支持移除离线玩家）。
* `/friend list` - 查看当前所有好友及其在线状态。

### 邮件与留言系统 (/mail)
系统邮件与离线留言服务，玩家进服时会自动收到未读提醒。

* `/mail read` - 读取当前邮箱中的所有未读系统邮件或留言。
* `/mail clear` - 清空邮箱中的所有邮件。

## 配置文件 (Configuration)

首次加载插件后，系统将在 plugins/GensokyoSocial/config.yml 生成如下配置。可通过修改此文件调整系统限制。

```yaml
settings: 
  # 队伍最大人数上限
  party-max-size: 3 
  # 好友数量上限 
  friend-max-limit: 50 
  # 系统邮件发件人默认名称 
  system-mail-sender: "Kirisame Marisa"
```


  开发者 API接入 (Developer API)
其他插件可以通过 GensokyoAPI 类调用本插件的社交数据。

1. Maven 依赖配置
如果你的项目使用 Maven 构建，推荐使用 JitPack 仓库引入 GitHub 上的代码。请在你的 pom.xml 中添加以下配置：

```XML
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.e0gd</groupId>
        <artifactId>Gensokyo-Social-SpigotMC</artifactId>
        <version>master-SNAPSHOT</version> 
        <scope>provided</scope>
    </dependency>
</dependencies>
```

并在你插件的 plugin.yml 中添加依赖：

```YAML
depend: [GensokyoSocial]
```
2. API 使用示例
以下是一个完整的 Java 示例类，展示了如何通过静态方法调用 GensokyoAPI 获取玩家队伍信息、读取好友列表以及发送系统邮件。
```java
package com.yourdomain.customplugin;

import com.eogd.gensokyo.api.GensokyoAPI;
import com.eogd.gensokyo.data.Party;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class SocialIntegrationExample {

    private final JavaPlugin plugin;

    public SocialIntegrationExample(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // 检查玩家队伍状态
    public void checkPlayerParty(Player player) {
        Party party = GensokyoAPI.getPlayerParty(player);
        if (party != null) {
            UUID leaderUuid = party.getLeader();
            int size = party.getMembers().size();
            player.sendMessage("你所在的队伍共有 " + size + " 人。");
        } else {
            player.sendMessage("你目前不在任何队伍中。");
        }
    }

    // 向玩家的好友群发自定义邮件
    public void broadcastToFriends(Player player, String message) {
        List<UUID> friends = GensokyoAPI.getFriends(player);
        if (friends.isEmpty()) {
            return;
        }
        
        for (UUID friendUuid : friends) {
            // 支持向离线玩家发送
            GensokyoAPI.sendMail(friendUuid, player.getName(), message);
        }
        player.sendMessage("已向 " + friends.size() + " 名好友发送了邮件。");
    }

    // 发送系统奖励邮件
    public void sendRewardMail(UUID targetPlayer) {
        GensokyoAPI.sendMail(targetPlayer, "System", "恭喜完成任务，请前往主城领取奖励！");
    }
}
