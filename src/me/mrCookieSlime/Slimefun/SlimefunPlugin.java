package me.mrCookieSlime.Slimefun;

import io.github.thebusybiscuit.cscorelib2.protection.ProtectionManager;
import me.mrCookieSlime.Slimefun.GPS.GPSNetwork;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.OreWasher;
import me.mrCookieSlime.Slimefun.api.*;
import me.mrCookieSlime.Slimefun.api.inventory.UniversalBlockMenu;
import me.mrCookieSlime.Slimefun.autosave.BlockAutoSaver;
import me.mrCookieSlime.Slimefun.autosave.PlayerAutoSaver;
import me.mrCookieSlime.Slimefun.hooks.SlimefunHooks;
import me.mrCookieSlime.Slimefun.utils.Settings;
import me.mrCookieSlime.Slimefun.utils.Utilities;
import org.bstats.bukkit.Metrics;
import io.github.thebusybiscuit.cscorelib2.updater.BukkitUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.Updater;

import java.io.File;
import java.util.logging.Level;

import me.mrCookieSlime.Slimefun.ancient_altar.AncientAltarListener;
import me.mrCookieSlime.Slimefun.listeners.AndroidKillingListener;
import me.mrCookieSlime.Slimefun.listeners.ArmorListener;
import me.mrCookieSlime.Slimefun.listeners.AutonomousToolsListener;
import me.mrCookieSlime.Slimefun.listeners.BackpackListener;
import me.mrCookieSlime.Slimefun.listeners.BlockListener;
import me.mrCookieSlime.Slimefun.listeners.BowListener;
import me.mrCookieSlime.Slimefun.listeners.CoolerListener;
import me.mrCookieSlime.Slimefun.listeners.DamageListener;
import me.mrCookieSlime.Slimefun.listeners.FurnaceListener;
import me.mrCookieSlime.Slimefun.listeners.GearListener;
import me.mrCookieSlime.Slimefun.listeners.GuideOnJoinListener;
import me.mrCookieSlime.Slimefun.listeners.ItemListener;
import me.mrCookieSlime.Slimefun.listeners.ItemPickupListener;
import me.mrCookieSlime.Slimefun.listeners.NetworkListener;
import me.mrCookieSlime.Slimefun.listeners.PlayerQuitListener;
import me.mrCookieSlime.Slimefun.listeners.TalismanListener;
import me.mrCookieSlime.Slimefun.listeners.TeleporterListener;
import me.mrCookieSlime.Slimefun.listeners.ToolListener;
import me.mrCookieSlime.Slimefun.listeners.WorldListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Reflection.ReflectionUtils;
import me.mrCookieSlime.Slimefun.Setup.CSCoreLibLoader;
import me.mrCookieSlime.Slimefun.commands.SlimefunCommand;
import me.mrCookieSlime.Slimefun.commands.SlimefunTabCompleter;
import me.mrCookieSlime.Slimefun.GEO.OreGenSystem;
import me.mrCookieSlime.Slimefun.GEO.resources.NetherIceResource;
import me.mrCookieSlime.Slimefun.GEO.resources.OilResource;
import me.mrCookieSlime.Slimefun.hooks.github.GitHubConnector;
import me.mrCookieSlime.Slimefun.hooks.github.GitHubSetup;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunArmorPiece;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Setup.Files;
import me.mrCookieSlime.Slimefun.Setup.Messages;
import me.mrCookieSlime.Slimefun.Setup.MiscSetup;
import me.mrCookieSlime.Slimefun.Setup.ResearchSetup;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.Setup.SlimefunSetup;
import me.mrCookieSlime.Slimefun.api.energy.ItemEnergy;

public final class SlimefunPlugin extends JavaPlugin {

	public static SlimefunPlugin instance;
	private static Config researches;
	private static Config items;
	private static Config whitelist;
	private static Config config;

	public static TickerTask ticker;

    private final GPSNetwork gps = new GPSNetwork();
    private ProtectionManager protections;
    private Utilities utilities = new Utilities();
    private Settings settings;
    private SlimefunHooks hooks;

	// Supported Versions of Minecraft
	private final String[] supported = {"v1_14_"};
	
	@Override
	public void onEnable() {
		CSCoreLibLoader loader = new CSCoreLibLoader(this);
		if (loader.load()) {

			String currentVersion = ReflectionUtils.getVersion();

			if (currentVersion.startsWith("v")) {
				boolean compatibleVersion = false;
				StringBuilder versions = new StringBuilder();
				
				int i = 0;
				for (String version: supported) {
					if (currentVersion.startsWith(version)) {
						compatibleVersion = true;
					}

                    String replace = version.substring(1).replaceFirst("_", ".").replace("_", ".X");
                    if (i == 0) versions.append(replace);
					else if (i == supported.length - 1) versions.append(" or ").append(replace);
					else versions.append(", ").append(replace);
					
					i++;
				}
				
				// Looks like you are using an unsupported Minecraft Version
				if (!compatibleVersion) {
                    getLogger().log(Level.SEVERE, "### Slimefun 加载失败!");
                    getLogger().log(Level.SEVERE, "###");
                    getLogger().log(Level.SEVERE, "### 你使用了错误版本的 Minecraft 服务端!!!");
                    getLogger().log(Level.SEVERE, "###");
                    getLogger().log(Level.SEVERE, "### 你正在使用 " + ReflectionUtils.getVersion());
                    getLogger().log(Level.SEVERE, "### 但 Slimefun v" + getDescription().getVersion() + " 要求你使用 Minecraft" + versions.toString());
                    getLogger().log(Level.SEVERE, "###");
                    getLogger().log(Level.SEVERE, "### 请使用旧版本的 Slimefun 并且关闭自动更新");
                    getLogger().log(Level.SEVERE, "### 或者考虑更新服务端版本.");
					getServer().getPluginManager().disablePlugin(this);
					return;
				}
			}

			instance = this;
            getLogger().log(Level.INFO, "加载文件中...");
            Files files = new Files();
            files.cleanup();

            getLogger().log(Level.INFO, "加载配置文件中...");

            // Setup config.yml
            PluginUtils utils = new PluginUtils(this);
			utils.setupConfig();
            config = utils.getConfig();
            settings = new Settings(config);

			// Loading all extra configs
			researches = new Config(files.researches);
			items = new Config(files.items);
			whitelist = new Config(files.whitelist);

            // Setup messages.yml
			utils.setupLocalization();
			Messages.local = utils.getLocalization();
			Messages.setup();

            new Metrics(this);

            // Setting up the Auto-Updater
            Updater updater;

            if (!getDescription().getVersion().startsWith("DEV - ")) {
                // We are using an official build, use the BukkitDev Updater
                updater = new BukkitUpdater(this, getFile(), 53485);
            }
            else {
                // If we are using a development build, we want to switch to our custom
                updater = new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/Slimefun4/master");
            }

            if (config.getBoolean("options.auto-update")) updater.start();

			// Creating all necessary Folders
            String[] storage = {"blocks", "stored-blocks", "stored-inventories", "stored-chunks", "universal-inventories", "waypoints", "block-backups"};
            String[] general = {"scripts", "generators", "error-reports", "cache/github"};
            for (String s : storage) createDir("data-storage/Slimefun/" + s);
            for (String s : general) createDir("plugins/Slimefun/" + s);

            getLogger().log(Level.INFO, "加载物品中...");
			MiscSetup.setupItemSettings();
			try {
				SlimefunSetup.setupItems();
			} catch (Exception x) {
                getLogger().log(Level.SEVERE, "An Error occured while initializing SlimefunItems for Slimefun " + Slimefun.getVersion(), x);
			}
			MiscSetup.loadDescriptions();

            settings.researchesEnabled = getResearchCfg().getBoolean("enable-researching");
            settings.smelteryFireBreakChance = (Integer) Slimefun.getItemValue("SMELTERY", "chance.fireBreak");

            getLogger().log(Level.INFO, "加载研究项目中...");
			ResearchSetup.setupResearches();

			MiscSetup.setupMisc();

            getLogger().log(Level.INFO, "加载世界生成器中...");

			// Generating Oil as an OreGenResource (its a cool API)
			OreGenSystem.registerResource(new OilResource());
			OreGenSystem.registerResource(new NetherIceResource());
			
			// Setting up GitHub Connectors...
			
			GitHubSetup.setup();
			
			// All Slimefun Listeners
            new ArmorListener(this);
            new ItemListener(this);
            new BlockListener(this);
            new GearListener(this);
            new AutonomousToolsListener(this);
            new DamageListener(this);
            new BowListener(this);
            new ToolListener(this);
            new FurnaceListener(this);
            new TeleporterListener(this);
            new AndroidKillingListener(this);
            new NetworkListener(this);
            new ItemPickupListener(this);

			// Toggleable Listeners for performance
			if (config.getBoolean("items.talismans")) new TalismanListener(this);
			if (config.getBoolean("items.backpacks")) new BackpackListener(this);
			if (config.getBoolean("items.coolers")) new CoolerListener(this);

			// Handle Slimefun Guide being given on Join
            if (config.getBoolean("options.give-guide-on-first-join")) new GuideOnJoinListener(this);

			// Load/Unload Worlds in Slimefun
            new WorldListener(this);

			// Clear the Slimefun Guide History upon Player Leaving
            new PlayerQuitListener(this);

            // Initiating various Stuff and all Items with a slightly delay (0ms after the Server finished loading)
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                Slimefun.emeraldenchants = getServer().getPluginManager().isPluginEnabled("EmeraldEnchants");
                protections = new ProtectionManager(getServer());
                MiscSetup.loadItems(settings);

                for (World world: Bukkit.getWorlds()) {
                    new BlockStorage(world);
                }

                if (SlimefunItem.getByID("ANCIENT_ALTAR") != null) new AncientAltarListener(instance);
            }, 0);

            SlimefunCommand command = new SlimefunCommand(this);
			getCommand("slimefun").setExecutor(command);
			getCommand("slimefun").setTabCompleter(new SlimefunTabCompleter(command));

            // Armor Update Task
            if (config.getBoolean("options.enable-armor-effects")) {
                getServer().getScheduler().runTaskTimer(this, () -> {
                    for (Player p: Bukkit.getOnlinePlayers()) {
                        for (ItemStack armor: p.getInventory().getArmorContents()) {
                            if (armor != null && Slimefun.hasUnlocked(p, armor, true)) {
                                if (SlimefunItem.getByItem(armor) instanceof SlimefunArmorPiece) {
                                    for (PotionEffect effect: ((SlimefunArmorPiece) SlimefunItem.getByItem(armor)).getEffects()) {
                                        p.removePotionEffect(effect.getType());
                                        p.addPotionEffect(effect);
                                    }
                                }
                                if (SlimefunManager.isItemSimiliar(armor, SlimefunItem.getItem("SOLAR_HELMET"), false) && p.getWorld().getTime() < 12300 || p.getWorld().getTime() > 23850 && p.getEyeLocation().getBlock().getLightFromSky() == 15) {
                                    ItemEnergy.chargeInventory(p, Float.valueOf(String.valueOf(Slimefun.getItemValue("SOLAR_HELMET", "charge-amount"))));
                                }
                            }
                        }

                        for (ItemStack radioactive: utilities.radioactiveItems) {
                            if (p.getInventory().containsAtLeast(radioactive, 1) || SlimefunManager.isItemSimiliar(p.getInventory().getItemInOffHand(), radioactive, true)) {
                                // Check if player is wearing the hazmat suit
                                // If so, break the loop
                                if (SlimefunManager.isItemSimiliar(SlimefunItems.SCUBA_HELMET, p.getInventory().getHelmet(), true) &&
                                        SlimefunManager.isItemSimiliar(SlimefunItems.HAZMATSUIT_CHESTPLATE, p.getInventory().getChestplate(), true) &&
                                        SlimefunManager.isItemSimiliar(SlimefunItems.HAZMATSUIT_LEGGINGS, p.getInventory().getLeggings(), true) &&
                                        SlimefunManager.isItemSimiliar(SlimefunItems.RUBBER_BOOTS, p.getInventory().getBoots(), true)) {
                                    break;
                                }

                                // If the item is enabled in the world, then make radioactivity do its job
                                if (Slimefun.isEnabled(p, radioactive, false)) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 400, 3));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 400, 3));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 3));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 3));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 1));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 1));
                                    p.setFireTicks(400);
                                    break;
                                    // Break the loop to save some calculations
                                }
                            }
                        }
                    }
                }, 0L, config.getInt("options.armor-update-interval") * 20L);
            }

			ticker = new TickerTask();

            getServer().getScheduler().runTaskTimer(this, new PlayerAutoSaver(), 2000L, settings.blocksAutoSaveDelay * 60L * 20L);

			// Starting all ASYNC Tasks
            getServer().getScheduler().runTaskTimerAsynchronously(this, new BlockAutoSaver(), 2000L, settings.blocksAutoSaveDelay * 60L * 20L);
            getServer().getScheduler().runTaskTimerAsynchronously(this, ticker, 100L, config.getInt("URID.custom-ticker-delay"));

            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> utilities.connectors.forEach(GitHubConnector::pullFile), 80L, 60 * 60 * 20L);
			
			// Hooray!
            getLogger().log(Level.INFO, "加载完成!");
			hooks = new SlimefunHooks(this);

            OreWasher.items = new ItemStack[] {SlimefunItems.IRON_DUST, SlimefunItems.GOLD_DUST, SlimefunItems.ALUMINUM_DUST, SlimefunItems.COPPER_DUST, SlimefunItems.ZINC_DUST, SlimefunItems.TIN_DUST, SlimefunItems.LEAD_DUST, SlimefunItems.SILVER_DUST, SlimefunItems.MAGNESIUM_DUST};

			// Do not show /sf elevator command in our Log, it could get quite spammy
			CSCoreLib.getLib().filterLog("([A-Za-z0-9_]{3,16}) issued server command: /sf elevator (.{0,})");
		}
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);

        if (ticker != null) {
            // Finishes all started movements/removals of block data
            ticker.halt();
            ticker.run();
        }

        PlayerProfile.iterator().forEachRemaining(profile -> {
            if (profile.isDirty()) profile.save();
        });

        for (World world: Bukkit.getWorlds()) {
            try {
                BlockStorage storage = BlockStorage.getStorage(world);
                if (storage != null) {
                    storage.save(true);
                }
                else {
                    System.err.println("无法在世界 \"" + world.getName() + "\" 中保存粘液科技方块");
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }

        for (UniversalBlockMenu menu: utilities.universalInventories.values()) {
            menu.save();
        }

        SlimefunBackup.start();

        // Prevent Memory Leaks
        Messages.local = null;
        SlimefunItem.all = null;
        SlimefunItem.items = null;
        AContainer.processing = null;
        AContainer.progress = null;
        OreWasher.items = null;

        instance = null;

        for (Player p: Bukkit.getOnlinePlayers()) {
            p.closeInventory();
        }
    }

    private void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
    }

    public static Config getCfg() {
        return instance.config;
    }

    public static Config getResearchCfg() {
        return instance.researches;
    }

    public static Config getItemCfg() {
        return instance.items;
    }

    public static Config getWhitelist() {
        return instance.whitelist;
    }

    @Deprecated
    public static int randomize(int max) {
        if (max < 1) return 0;
        return CSCoreLib.randomizer().nextInt(max);
    }

    @Deprecated
    public static boolean chance(int max, int percentage) {
        if (max < 1) return false;
        return CSCoreLib.randomizer().nextInt(max) <= percentage;
    }

    public GPSNetwork getGPS() {
        return gps;
    }

    public static SlimefunHooks getHooks() {
        return instance.hooks;
    }

    public static Utilities getUtilities() {
        return instance.utilities;
    }

    public static Settings getSettings() {
        return instance.settings;
    }

    public static TickerTask getTicker() {
        return instance.ticker;
    }

    public static boolean isActive() {
        return instance != null;
    }

    public static ProtectionManager getProtectionManager() {
        return instance.protections;
    }
}