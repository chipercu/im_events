package com.infomaximum.im_event.Config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */

@Configuration
@Data
@PropertySource("application.properties")

public class TelegramBotConfig {

    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;

}
