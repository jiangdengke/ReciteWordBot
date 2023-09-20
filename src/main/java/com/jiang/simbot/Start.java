package com.jiang.simbot;

import cn.hutool.core.date.DateTime;
import com.jiang.simbot.utils.BotUtil;
import lombok.Data;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.ID;
import love.forte.simbot.component.mirai.bot.MiraiBot;
import love.forte.simbot.event.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

@Component
public class Start implements ApplicationRunner {

    @Autowired
    private BotUtil botUtil;
    public void run(ApplicationArguments args) {
        MiraiBot bot = botUtil.getBot();

        bot.getFriend(ID.$(1728439852)).sendBlocking(new DateTime()+"  机器人【"+bot.getUsername()+"】启动成功！");
    }
}
