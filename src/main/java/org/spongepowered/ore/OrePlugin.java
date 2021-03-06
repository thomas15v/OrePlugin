package org.spongepowered.ore;

import static org.spongepowered.api.text.Text.Builder;
import static org.spongepowered.api.text.Text.NEW_LINE;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.ore.Messages.AVAILABLE_UPDATES;
import static org.spongepowered.ore.Messages.UPDATE;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.ore.client.OreClient;
import org.spongepowered.ore.client.SpongeOreClient;
import org.spongepowered.ore.cmd.CommandExecutors;
import org.spongepowered.ore.cmd.CommandTry;
import org.spongepowered.ore.config.OreConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;

/**
 * Main plugin class for Ore.
 */
@Plugin(id = "ore",
        name = "Ore",
        description = "Official package manager for Sponge.",
        authors = { "windy" }
)
public final class OrePlugin {

    @Inject public Logger log;
    @Inject public Game game;
    @Inject public PluginContainer self;
    @Inject @DefaultConfig(sharedRoot = true) private Path configPath;

    private OreClient client;
    private OreConfig config;

    @Listener(order = Order.POST)
    public void onStart(GameStartedServerEvent event) throws IOException {
        this.log.info("Initializing...");
        loadConfig();
        this.client = SpongeOreClient.forPlugin(this);
        new CommandExecutors(this).register();
        checkForUpdates();
        this.log.info("Done.");
    }

    @Listener(order = Order.POST)
    public void onStop(GameStoppingEvent event) throws IOException {
        if (this.client.hasUninstalledUpdates()) {
            this.log.info("Applying " + this.client.getUninstalledUpdates() + " updates...");
            this.client.installUpdates();
            this.log.info("Done.");
        }

        if (this.client.hasPendingUninstallations()) {
            this.log.info("Uninstalling " + this.client.getPendingUninstallations() + " plugins...");
            this.client.completeUninstallations();
            this.log.info("Done.");
        }
    }

    /**
     * Returns the client to use for interacting with the web API.
     *
     * @return Client to interact with API
     */
    public OreClient getClient() {
        return this.client;
    }

    /**
     * Loads (or reloads) the configuration file.
     *
     * @throws IOException
     */
    public void loadConfig() throws IOException {
        this.config = OreConfig.load(this.configPath);
    }

    /**
     * Returns the loaded {@link OreConfig} for this plugin.
     *
     * @return OreConfig instance
     */
    public OreConfig getConfig() {
        return this.config;
    }

    /**
     * Creates and executes a new async task for a command executor.
     *
     * @param name Task name
     * @param src CommandSource
     * @param callable To execute
     * @return Submitted task
     */
    public Task newAsyncTask(String name, CommandSource src, CommandTry callable) {
        return this.game.getScheduler().createTaskBuilder()
            .name(name)
            .async()
            .execute(() -> callable.callFor(src))
            .submit(this);
    }

    private void checkForUpdates() {
        this.log.info("Checking for updates...");
        ConsoleSource console = this.game.getServer().getConsole();
        newAsyncTask(CommandExecutors.TASK_NAME_SEARCH, console, () -> {
            Map<PluginContainer, String> updates = this.client.getAvailableUpdates();
            Builder message = AVAILABLE_UPDATES.apply(ImmutableMap.of("content", of(updates.size())));
            for (PluginContainer update : updates.keySet()) {
                message.append(NEW_LINE).append(UPDATE.apply(ImmutableMap.of(
                    "pluginId", of(update.getId()),
                    "content", of(updates.get(update))
                )).build());
            }
            console.sendMessage(message.build());
            return null;
        });
    }

}
