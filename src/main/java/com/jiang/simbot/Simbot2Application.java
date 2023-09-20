package com.jiang.simbot;

import jakarta.annotation.PostConstruct;
import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.cssxsh.mirai.tool.FixProtocolVersion;

import java.util.Map;

@SpringBootApplication
@EnableSimbot
@EnableScheduling
public class Simbot2Application {

	public static void main(String[] args) {
		FixProtocolVersion.update();
		FixProtocolVersion.fetch(BotConfiguration.MiraiProtocol.ANDROID_PAD, "8.9.63");
		FixProtocolVersion.load(BotConfiguration.MiraiProtocol.ANDROID_PAD);
		Map<BotConfiguration.MiraiProtocol, String> info = FixProtocolVersion.info();
		SpringApplication.run(Simbot2Application.class, args);
	}
}
