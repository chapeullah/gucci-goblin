package com.chapeullah.GucciGoblin.command;

import com.chapeullah.GucciGoblin.infrastructure.AnsiColors;
import com.chapeullah.GucciGoblin.service.GucciService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class GucciCommands {

    private final GucciService gucciService;

    @Value("${LOG_DIR:./logs}")
    private String logDir;

    @ShellMethod(key = "sync", value = "Synchronize gucci data with CoC public API. Usage: sync")
    public String updateGucci() {
        gucciService.synchronize();
        return "OK";
    }

    @ShellMethod(
            key = "logs",
            value = "Show last N..100 lines from gucci.log. Usage: logs [-t | --tail] <lines> (default: 50) [-d | --" +
                    "date] <yyyy-MM>"
    )
    public String logsTail(
            @ShellOption(value = {"-t", "--tail"}, help = "Number of lines (1..100).", defaultValue = "50") int tail,
            @ShellOption(value = {"-d", "--date"}, help = "Log date (yyyy-MM).", defaultValue = ShellOption.NULL) String date
    ) throws Exception {
        StringBuilder prefix = new StringBuilder();
        int n = tail;
        if (tail > 100 || tail < 1) {
            n = Math.max(1, Math.min(100, tail));
            prefix.append(AnsiColors.yellow("Tail [-t | --tail] out of range, using " + n + " instead of " + tail))
                    .append(System.lineSeparator());
        }

        String fileName = (date == null) ? "gucci.log" : "gucci." + date + ".log";

        Path file = Path.of(logDir, fileName);

        if (!Files.exists(file))
            return AnsiColors.red(
                    "Log file not found: " + file.toAbsolutePath() + ". Use: <yyyy-MM> format."
            );

        List<String> all = Files.readAllLines(file);
        int from = Math.max(0, all.size() - n);

        return prefix + String.join(System.lineSeparator(), all.subList(from, all.size()));
    }

}