package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import lombok.Getter;

import java.util.*;

public class CSCommandManager {

    @Getter private final CSPlugin plugin;
    private final Map<String, CSCommand> commands;
    private final List<CSCommand> commandList;

    public CSCommandManager(CSPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.commandList = new ArrayList<>();

        registerCommand(new ReloadCMD());
        registerCommand(new StatsCMD());
        registerCommand(new TestCMD());
        registerCommand(new UpdateCMD());
        registerCommand(new VoteCMD());
    }

    public List<CSCommand> getCommands() {
        return this.commandList;
    }

    /**
     * Registrar un nuevo comando, probablemente específico por cada plataforma, a este manager
     * @param command El comando a registrar
     */
    public void registerCommand(CSCommand command) {
        this.commandList.add(command);
        this.commands.put(command.getName(), command);
        command.getAliases().forEach(alias -> this.commands.put(alias, command));
    }

    /**
     * Ejecutar un comando. Buscarlo en el Mapa de comandos y alias y si es posible, invocarlo
     * @param sender Quien ejecuta el comando
     * @param label El comando escrito
     * @param args Los argumentos del comando
     */
    public void executeCommand(final CSCommandSender sender, String label, List<String> args) {
        Optional<CSCommand> command = Optional.ofNullable(commands.getOrDefault(label, null));
        if (command.isPresent()) {
            CSCommand cmd = command.get();
            if (!cmd.isAuthorized(sender)) {
                sender.sendMessage(plugin.getCSConfiguration().getString("command.global.noPermission"));
                return;
            }
            CSCommand.CommandResult result = cmd.execute(plugin, sender, label, args);
            switch (result) {
                case COOLDOWN:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.global.cooldown"));
                    break;
                case NO_PERMISSION:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.global.noPermission"));
                    break;
                case ERROR:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.global.error"));
                    break;
                case ONLY_PLAYER:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.global.onlyPlayer"));
                    break;
            }
        }
    }
}
