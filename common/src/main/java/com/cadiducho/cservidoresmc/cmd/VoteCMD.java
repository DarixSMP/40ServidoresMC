package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.Cooldown;
import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.model.VoteResponse;
import com.cadiducho.cservidoresmc.model.VoteStatus;

import java.util.Arrays;
import java.util.List;

/**
 * Comando para validar el voto en 40ServidoresMC
 */
public class VoteCMD extends CSCommand {

    protected VoteCMD() {
        super("vote", "40servidores.vote", Arrays.asList("votar40", "vote40", "voto40", "mivoto40"),
                "Valida tu voto en el servidor",
                "Usa /vote para validar tu voto en el servidor");
    }

    final Cooldown cooldown = new Cooldown(60);

    @Override
    public CommandResult execute(CSPlugin plugin, CSCommandSender sender, String label, List<String> args) {
        if (sender.isConsole()) {
            return CommandResult.ONLY_PLAYER;
        }

        if (cooldown.isCoolingDown(sender.getName())) {
            return CommandResult.COOLDOWN;
        }

        cooldown.setOnCooldown(sender.getName());

        sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.getting"));
        plugin.getApiClient().validateVote(sender.getName()).thenAccept((VoteResponse voteResponse) -> {
            String web = voteResponse.getWeb();
            VoteStatus status = voteResponse.getStatus();

            switch (status) {
                case NOT_VOTED:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.notVoted").replace("{0}", web));
                    break;
                case SUCCESS:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.success"));

                    plugin.getCSConfiguration().customCommandsList().stream()
                            .map(cmds -> cmds.replace("{0}", sender.getName()))
                            .forEach(plugin::dispatchCommand);

                    plugin.broadcastMessage(plugin.getCSConfiguration().getString("command.vote.successBroadcast").replace("{0}", sender.getName()));
                    break;
                case ALREADY_VOTED:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.alreadyVoted"));
                    break;
                case INVALID_kEY:
                    sender.sendMessage("&cClave incorrecta. Entra en &bhttps://40servidoresmc.es/miservidor.php &cy cambia esta.");
                    break;
                default:
                    sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.error"));
                    break;
            }
        }).exceptionally(e -> {
            sender.sendMessage(plugin.getCSConfiguration().getString("command.vote.error"));
            plugin.logError("Excepción intentando votar: " + e.getMessage());
            return null;
        });

        return CommandResult.SUCCESS;
    }
}
