package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.telegram.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements BotCommand {

    private final CommandService commandService;

    @Override
    public String name() {
        return "/help";
    }

    @Override
    public String execute(String args) {
        return commandService.help();
    }

}
