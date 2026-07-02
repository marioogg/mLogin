package me.marioogg.mlogin.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import me.marioogg.mlogin.velocity.VelocityPlugin;
import me.marioogg.mlogin.core.util.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BackupCommand implements SimpleCommand {

    private final VelocityPlugin plugin;

    public BackupCommand(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source.hasPermission("mlogin.admin") || source.hasPermission("mlogin.backup"))) {
            source.sendMessage(Component.text("You don't have permission.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /mlogin backup | /mlogin restore <file.sql>", NamedTextColor.YELLOW));
            return;
        }

        CompletableFuture.runAsync(() -> {
            if ("backup".equalsIgnoreCase(args[0])) {
                performBackup(source);
            } else if ("restore".equalsIgnoreCase(args[0]) && args.length >= 2) {
                performRestore(source, args[1]);
            }
        });
    }

    // Sugerencia de autocompletado para el comando
    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length <= 1) {
            return List.of("backup", "restore");
        }
        return List.of();
    }

    private void performBackup(CommandSource source) {
        try {
            Path backupsDir = plugin.getDataDirectory().resolve("backups");
            Files.createDirectories(backupsDir);
            String fileName = "mlogin-backup-" + DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-") + ".sql";
            Path backupFile = backupsDir.resolve(fileName);

            try (Connection con = plugin.getSql().getConnection();
                 Statement stmt = con.createStatement();
                 var writer = Files.newBufferedWriter(backupFile)) {

                writer.write("-- mLogin Backup " + Instant.now() + "\n");
                try (ResultSet rs = stmt.executeQuery("SELECT uuid, username, password, reg_date FROM mlogin_users")) {
                    while (rs.next()) {
                        writer.write(String.format("INSERT INTO mlogin_users VALUES ('%s', '%s', '%s', '%s');\n",
                                rs.getString("uuid"), rs.getString("username").replace("'", "''"),
                                rs.getString("password").replace("'", "''"), rs.getString("reg_date")));
                    }
                }
                source.sendMessage(Component.text("Backup created: " + fileName, NamedTextColor.GREEN));
            }
        } catch (Exception e) {
            Log.getLogger().error("Error creating backup", e);
            source.sendMessage(Component.text("Error creating backup: " + e.getMessage(), NamedTextColor.RED));
        }
    }

    private void performRestore(CommandSource source, String fileName) {
        Path backupFile = plugin.getDataDirectory().resolve("backups").resolve(fileName);
        if (!Files.exists(backupFile)) {
            source.sendMessage(Component.text("Backup file not found.", NamedTextColor.RED));
            return;
        }

        try (Connection con = plugin.getSql().getConnection();
             Statement stmt = con.createStatement();
             var reader = Files.newBufferedReader(backupFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("INSERT") || line.startsWith("CREATE")) {
                    stmt.execute(line);
                }
            }
            source.sendMessage(Component.text("Restoration complete.", NamedTextColor.GREEN));
        } catch (Exception e) {
            Log.getLogger().error("Error restoring backup", e);
            source.sendMessage(Component.text("Error restoring backup: " + e.getMessage(), NamedTextColor.RED));
        }
    }
}