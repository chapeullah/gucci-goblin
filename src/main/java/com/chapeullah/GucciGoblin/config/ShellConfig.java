package com.chapeullah.GucciGoblin.config;

import com.chapeullah.GucciGoblin.infrastructure.AnsiColors;
import org.jline.utils.AttributedString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {

    @Bean
    public PromptProvider promptProvider() { return () -> new AttributedString(AnsiColors.yellow("gucci:>")); }

}
