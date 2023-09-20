package com.jiang.simbot.utils;

import love.forte.simbot.ID;
import love.forte.simbot.application.Application;
import love.forte.simbot.application.BotManagers;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.component.mirai.bot.MiraiBot;
import love.forte.simbot.component.mirai.bot.MiraiBotManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotUtil {
    @Autowired
    private Application application;
    @Value("${qq}")
    private String qq;
    public MiraiBot getBot(){
        BotManagers botManagers = application.getBotManagers();
        BotManager<?> botManager = botManagers.get(0);
        MiraiBot miraiBot=null;
        if(botManager instanceof MiraiBotManager miraiBotManager){
            miraiBot = miraiBotManager.get(ID.$(qq));
        }
        return miraiBot;
    }
}
