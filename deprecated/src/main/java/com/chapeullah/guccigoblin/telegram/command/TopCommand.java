package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.telegram.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopCommand implements BotCommand {

    private final CommandService commandService;

    @Override
    public String name() {
        return "/top";
    }

    @Override
    public String execute(String args) {
        return commandService.top();
    }

}
