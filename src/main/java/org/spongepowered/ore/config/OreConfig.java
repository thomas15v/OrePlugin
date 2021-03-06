package org.spongepowered.ore.config;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.write;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Ore configuration options.
 */
@SuppressWarnings("FieldCanBeLocal")
public final class OreConfig {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
        .registerTypeAdapter(URL.class, new UrlTypeAdapter())
        .create();

    private URL repositoryUrl = new URL("https://ore-staging.spongepowered.org");
    private Path installationDirectory = Paths.get("mods").toAbsolutePath();
    private Path updatesDirectory = Paths.get("updates").toAbsolutePath();
    private Path downloadsDirectory = Paths.get("downloads").toAbsolutePath();
    private boolean autoResolveDependencies = true;
    private List<String> ignoredPlugins = Arrays.asList("Minecraft", "mcp", "FML", "Forge", "sponge", "ore");

    private OreConfig() throws MalformedURLException {}

    /**
     * Returns the location of the Ore server instance to use.
     *
     * @return Ore instance location
     */
    public URL getRepositoryUrl() {
        return this.repositoryUrl;
    }

    /**
     * Returns the directory in which mods/plugins are kept.
     *
     * @return Mods directory
     */
    public Path getInstallationDirectory() {
        return this.installationDirectory;
    }

    /**
     * Returns the directory in which Ore should store downloaded updates
     * temporarily.
     *
     * @return Updates directory
     */
    public Path getUpdatesDirectory() {
        return this.updatesDirectory;
    }

    /**
     * Returns the directory in which Ore should store plugin downloads that are not to
     * be installed.
     *
     * @return Downloads directory
     */
    public Path getDownloadsDirectory() {
        return this.downloadsDirectory;
    }

    /**
     * Returns true if plugin dependencies should be automatically resolved
     * when updating or installing.
     *
     * @return True if should automatically resolve dependencies
     */
    public boolean getAutoResolveDependencies() {
        return this.autoResolveDependencies;
    }

    /**
     * Returns a list of plugin IDs which should be ignored by Ore.
     *
     * @return Plugins to ignore
     */
    public Set<String> getIgnoredPlugins() {
        return ImmutableSet.copyOf(this.ignoredPlugins);
    }

    /**
     * Loads a new {@link OreConfig} from disk at the specified {@link Path} or
     * creates one if none exists.
     *
     * @param path Path to load from
     * @return Newly loaded OreConfig
     * @throws IOException
     */
    public static OreConfig load(Path path) throws IOException {
        if (notExists(path)) {
            createDirectories(path.getParent());
            createFile(path);
            OreConfig defaultConfig = new OreConfig();
            write(path, (GSON.toJson(defaultConfig) + '\n').getBytes());
            return defaultConfig;
        }
        return GSON.fromJson(new InputStreamReader(newInputStream(path)), OreConfig.class);
    }

}
