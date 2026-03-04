package com.chapeullah.GucciGoblin.command;

import com.chapeullah.GucciGoblin.model.enums.TownHallRel;
import com.chapeullah.GucciGoblin.service.WarAttackService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class WarAttackCommands {

    private final WarAttackService warAttackService;

    @ShellMethod(key = "war add", value =
            "Add player's war attacks. " +
                    "Usage: war add --war <key> --tag <#TAG> " +
                    "[--a1stars <0..3> --a1rel <UP|SAME|DOWN>] " +
                    "[--a2stars <0..3> --a2rel <UP|SAME|DOWN>]"
    )
    public String addPlayerAttacks(
            @ShellOption(value = "--war") String warKey,
            @ShellOption(value = "--tag") String tag,

            @ShellOption(value = "--a1stars", defaultValue = ShellOption.NULL) Integer a1Stars,
            @ShellOption(value = "--a1rel", defaultValue = ShellOption.NULL) TownHallRel a1Rel,

            @ShellOption(value = "--a2stars", defaultValue = ShellOption.NULL) Integer a2Stars,
            @ShellOption(value = "--a2rel", defaultValue = ShellOption.NULL) TownHallRel a2Rel
    ) {
        warAttackService.addPlayerAttacks(warKey, tag, a1Stars, a1Rel, a2Stars, a2Rel);
        return "OK";
    }

}
