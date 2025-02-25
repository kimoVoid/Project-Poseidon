package net.minecraft.server;

import com.legacyminecraft.poseidon.Poseidon;
import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.PoseidonPlugin;
import com.legacyminecraft.poseidon.util.ServerLogRotator;
import com.legacyminecraft.poseidon.utility.PerformanceStatistic;
import com.legacyminecraft.poseidon.utility.PoseidonVersionChecker;
import com.projectposeidon.johnymuffin.UUIDManager;
import com.legacyminecraft.poseidon.watchdog.WatchDogThread;
import jline.ConsoleReader;
import joptsimple.OptionSet;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;
import net.ornithemc.osl.networking.impl.HandshakePayload;
import net.ornithemc.osl.networking.impl.NetServerHandlerImpl;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.LoggerOutputStream;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.util.ServerShutdownThread;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPlayReadyEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginLoadOrder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// CraftBukkit start
//import com.projectposeidon.johnymuffin.UUIDCacheFile;
// CraftBukkit end

public class MinecraftServer implements Runnable, ICommandListener {

    public static Logger log = Logger.getLogger("Minecraft");
    public static HashMap trackerList = new HashMap();
    public NetworkListenThread networkListenThread;
    public PropertyManager propertyManager;
    // public WorldServer[] worldServer; // CraftBukkit - removed!
    public ServerConfigurationManager serverConfigurationManager;
    public ConsoleCommandHandler consoleCommandHandler; // CraftBukkit - made public
    private boolean isRunning = true;
    public boolean isStopped = false;
    int ticks = 0;
    public String i;
    public int j;
    private List r = new ArrayList();
    private List s = Collections.synchronizedList(new ArrayList());
    // public EntityTracker[] tracker = new EntityTracker[2]; // CraftBukkit - removed!
    public boolean onlineMode;
    public boolean spawnAnimals;
    public boolean pvpMode;
    public boolean allowFlight;
    public String messageOfTheDay;

    // CraftBukkit start
    public List<WorldServer> worlds = new ArrayList<WorldServer>();
    public CraftServer server;
    public OptionSet options;
    public ColouredConsoleSender console;
    public ConsoleReader reader;
    public static int currentTick;
    // CraftBukkit end

    //Poseidon Start
//    private WatchDogThread watchDogThread;
    private boolean modLoaderSupport = false;
//    private PoseidonVersionChecker poseidonVersionChecker;
    //Poseidon End

    public MinecraftServer(OptionSet options) { // CraftBukkit - adds argument OptionSet
        new ThreadSleepForever(this);

        // CraftBukkit start
        this.options = options;
        try {
            this.reader = new ConsoleReader();
        } catch (IOException ex) {
            Logger.getLogger(MinecraftServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));
        // CraftBukkit end
    }

    private boolean init() throws UnknownHostException { // CraftBukkit - added throws UnknownHostException
        this.consoleCommandHandler = new ConsoleCommandHandler(this);
        ThreadCommandReader threadcommandreader = new ThreadCommandReader(this);

        threadcommandreader.setDaemon(true);
        threadcommandreader.start();
        ConsoleLogManager.init(this); // CraftBukkit

        // CraftBukkit start
        System.setOut(new PrintStream(new LoggerOutputStream(log, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(log, Level.SEVERE), true));
        // CraftBukkit end

        //If Poseidon Config DEBUG is enabled, enable debug mode
        if (options.has("debug-config")) {
            log.info("[Poseidon] Configuration debug mode has been enabled. This will cause the poseidon.yml to be reloaded every time the server starts.");
            PoseidonConfig.getInstance().resetConfig();
        }

        modLoaderSupport = PoseidonConfig.getInstance().getBoolean("settings.support.modloader.enable", false);

        if (modLoaderSupport) {
            log.info("EXPERIMENTAL MODLOADERMP SUPPORT ENABLED.");
            if (!isModloaderPresent()) {
                log.severe("ModLoaderMP support is enabled, however, it isn't present. Please install it before enabling this setting");
                return false;
            }
            net.minecraft.server.ModLoader.Init(this);
        }

        log.info("Starting minecraft server version Beta 1.7.3");
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            log.warning("**** NOT ENOUGH RAM!");
            log.warning("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        log.info("Loading properties");
        this.propertyManager = new PropertyManager(this.options); // CraftBukkit - CLI argument support
        String s = this.propertyManager.getString("server-ip", "");

        this.onlineMode = this.propertyManager.getBoolean("online-mode", false); //Project Poseidon - False by default
        this.spawnAnimals = this.propertyManager.getBoolean("spawn-animals", true);
        this.pvpMode = this.propertyManager.getBoolean("pvp", true);
        this.allowFlight = this.propertyManager.getBoolean("allow-flight", false);
        this.messageOfTheDay = this.propertyManager.getString("motd", "A Minecraft Server").replace('§', '$');
        InetAddress inetaddress = null;

        if (s.length() > 0) {
            inetaddress = InetAddress.getByName(s);
        }

        int i = this.propertyManager.getInt("server-port", 25565);

        log.info("Starting Minecraft server on " + (s.length() == 0 ? "*" : s) + ":" + i);

        try {
            this.networkListenThread = new NetworkListenThread(this, inetaddress, i);
        } catch (Throwable ioexception) { // CraftBukkit - IOException -> Throwable
            log.warning("**** FAILED TO BIND TO PORT!");
            log.log(Level.WARNING, "The exception was: " + ioexception.toString());
            log.warning("Perhaps a server is already running on that port?");
            return false;
        }

        if (!this.onlineMode) {
            log.warning("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            log.warning("The server will make no attempt to authenticate usernames. Beware.");
            log.warning("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            log.warning("To change this, set \"online-mode\" to \"true\" in the server.settings file.");
        }

        this.serverConfigurationManager = new ServerConfigurationManager(this);
        // CraftBukkit - removed trackers
        long j = System.nanoTime();
        String s1 = this.propertyManager.getString("level-name", "world");
        String s2 = this.propertyManager.getString("level-seed", "");
        long k = (new Random()).nextLong();

        if (s2.length() > 0) {
            try {
                k = Long.parseLong(s2);
            } catch (NumberFormatException numberformatexception) {
                k = (long) s2.hashCode();
            }
        }

        log.info("Preparing level \"" + s1 + "\"");
        this.a(new WorldLoaderServer(new File(".")), s1, k);

        //Project Poseidon Start
        Poseidon.getServer().initializeServer();
        //Project Poseidon End

        // CraftBukkit start
        long elapsed = System.nanoTime() - j;
        String time = String.format("%.3fs", elapsed / 10000000000.0D);
        log.info("Done (" + time + ")! For help, type \"help\" or \"?\"");

        // log rotator process start.
        if ((boolean) PoseidonConfig.getInstance().getConfigOption("settings.per-day-log-file.enabled") && (boolean) PoseidonConfig.getInstance().getConfigOption("settings.per-day-log-file.latest-log.enabled")) {
            String latestLogFileName = "latest";
            ServerLogRotator serverLogRotator = new ServerLogRotator(latestLogFileName);
            serverLogRotator.start();
        }

        if (this.propertyManager.properties.containsKey("spawn-protection")) {
            log.info("'spawn-protection' in server.properties has been moved to 'settings.spawn-radius' in bukkit.yml. I will move your config for you.");
            this.server.setSpawnRadius(this.propertyManager.getInt("spawn-protection", 16));
            this.propertyManager.properties.remove("spawn-protection");
            this.propertyManager.savePropertiesFile();
        }
        return true;
    }

    public boolean isModloaderPresent() {
        try {
            Class.forName("net.minecraft.server.ModLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void a(Convertable convertable, String s, long i) {
        if (convertable.isConvertable(s)) {
            log.info("Converting map!");
            convertable.convert(s, new ConvertProgressUpdater(this));
        }

        // CraftBukkit start
        for (int j = 0; j < (this.propertyManager.getBoolean("allow-nether", true) ? 2 : 1); ++j) {
            WorldServer world;
            int dimension = j == 0 ? 0 : -1;
            String worldType = Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;

            ChunkGenerator gen = this.server.getGenerator(name);

            if (j == 0) {
                world = new WorldServer(this, new ServerNBTManager(new File("."), s, true), s, dimension, i, org.bukkit.World.Environment.getEnvironment(dimension), gen); // CraftBukkit
            } else {
                String dim = "DIM-1";

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);

                if ((!newWorld.isDirectory()) && (oldWorld.isDirectory())) {
                    log.info("---- Migration of old " + worldType + " folder required ----");
                    log.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    log.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    log.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        log.severe("A file or folder already exists at " + newWorld + "!");
                        log.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            log.info("Success! To restore the nether in the future, simply move " + newWorld + " to " + oldWorld);
                            log.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            log.severe("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            log.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        log.severe("Could not create path for " + newWorld + "!");
                        log.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                world = new SecondaryWorldServer(this, new ServerNBTManager(new File("."), name, true), name, dimension, i, this.worlds.get(0), org.bukkit.World.Environment.getEnvironment(dimension), gen); // CraftBukkit
            }

            if (gen != null) {
                world.getWorld().getPopulators().addAll(gen.getDefaultPopulators(world.getWorld()));
            }

            this.server.getPluginManager().callEvent(new WorldInitEvent(world.getWorld()));

            world.tracker = new EntityTracker(this, dimension);
            world.addIWorldAccess(new WorldManager(this, world));
            world.spawnMonsters = this.propertyManager.getBoolean("spawn-monsters", true) ? 1 : 0;
            world.setSpawnFlags(this.propertyManager.getBoolean("spawn-monsters", true), this.spawnAnimals);
            this.worlds.add(world);
            this.serverConfigurationManager.setPlayerFileData(this.worlds.toArray(new WorldServer[0]));
        }
        // CraftBukkit end

        short short1 = 196;
        long k = System.currentTimeMillis();

        // CraftBukkit start
        for (int l = 0; l < this.worlds.size(); ++l) {
            // if (l == 0 || this.propertyManager.getBoolean("allow-nether", true)) {
            WorldServer worldserver = this.worlds.get(l);
            log.info("Preparing start region for level " + l + " (Seed: " + worldserver.getSeed() + ")");
            if (worldserver.getWorld().getKeepSpawnInMemory()) {
                // CraftBukkit end
                ChunkCoordinates chunkcoordinates = worldserver.getSpawn();

                for (int i1 = -short1; i1 <= short1 && this.isRunning; i1 += 16) {
                    for (int j1 = -short1; j1 <= short1 && this.isRunning; j1 += 16) {
                        long k1 = System.currentTimeMillis();

                        if (k1 < k) {
                            k = k1;
                        }

                        if (k1 > k + 1000L) {
                            int l1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                            int i2 = (i1 + short1) * (short1 * 2 + 1) + j1 + 1;

                            this.a("Preparing spawn area", i2 * 100 / l1);
                            k = k1;
                        }

                        worldserver.chunkProviderServer.getChunkAt(chunkcoordinates.x + i1 >> 4, chunkcoordinates.z + j1 >> 4);

                        while (worldserver.doLighting() && this.isRunning) {
                            ;
                        }
                    }
                }
            } // CraftBukkit
        }

        // CraftBukkit start
        for (World world : this.worlds) {
            this.server.getPluginManager().callEvent(new WorldLoadEvent(world.getWorld()));
        }
        // CraftBukkit end

        this.e();
    }

    private void a(String s, int i) {
        this.i = s;
        this.j = i;
        log.info(s + ": " + i + "%");
    }

    private void e() {
        this.i = null;
        this.j = 0;

        this.server.enablePlugins(PluginLoadOrder.POSTWORLD); // CraftBukkit
    }

    void saveChunks() { // CraftBukkit - private -> default
        log.info("Saving chunks");

        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);

            worldserver.save(true, (IProgressUpdate) null);
            worldserver.saveLevel();

            WorldSaveEvent event = new WorldSaveEvent(worldserver.getWorld());
            this.server.getPluginManager().callEvent(event);
        }

        WorldServer world = this.worlds.get(0);
        if (!world.canSave) {
            this.serverConfigurationManager.savePlayers();
        }
        // CraftBukkit end
    }

    public void stop() { // CraftBukkit - private -> public
        NetServerHandlerImpl.destroy(this);
        log.info("Stopping server");

        //Project Poseidon Start

        // This is done before disablePlugins() to ensure the watchdog doesn't detect plugins disabling as a server hang
        Poseidon.getServer().shutdownServer();

        //Project Poseidon End

        // CraftBukkit start
        if (this.server != null) {
            this.server.disablePlugins();
        }
        // CraftBukkit end

        if (this.serverConfigurationManager != null) {
            this.serverConfigurationManager.savePlayers();
        }

        // CraftBukkit start - multiworld is handled in saveChunks() already.
        WorldServer worldserver = this.worlds.get(0);

        if (worldserver != null) {
            this.saveChunks();
        }
        // CraftBukkit end

        // Poseidon Start
        Map<String, PerformanceStatistic> listenerStatistics = new HashMap<>();

        // Only get the Listener Statistics if the Poseidon Server is not null. Prevents null pointer exceptions.
        if (Poseidon.getServer() != null && Poseidon.getServer().getConfig().getConfigBoolean("settings.performance-monitoring.listener-reporting.print-statistics-on-shutdown.enabled")) {
            listenerStatistics = Poseidon.getServer().getSortedListenerPerformance();
        }

        // Check if the statistics map is not empty
        if (listenerStatistics != null && !listenerStatistics.isEmpty()) {
            log.info("[Poseidon] Listener statistics from this session:");

            // Iterate over each listener and log their statistics
            for (Map.Entry<String, PerformanceStatistic> entry : listenerStatistics.entrySet()) {
                String listener = entry.getKey();
                PerformanceStatistic stats = entry.getValue();

                if (stats.getMaxExecutionTime() == 0) {
                    continue;
                }

                if (stats != null) {
                    log.info(String.format("[Poseidon] Listener: %s - Processed %d events, Total Execution Time: %d ms, Avg Time: %d ms",
                            listener,
                            stats.getEventCount(),
                            stats.getTotalExecutionTime(),
                            stats.getAverageExecutionTime()));
                } else {
                    log.warning("[Poseidon] No statistics available for listener: " + listener);
                }
            }
        }


        // Check if the statistics map is not empty

        Map<String, PerformanceStatistic> taskStatistics = new HashMap<>();

        // Only get the Task Statistics if the Poseidon Server is not null. Prevents null pointer exceptions.
        if (Poseidon.getServer() != null && Poseidon.getServer().getConfig().getConfigBoolean("settings.performance-monitoring.listener-reporting.print-statistics-on-shutdown.enabled")) {
            taskStatistics = Poseidon.getServer().getSortedTaskPerformance();
        }

        if (taskStatistics != null && !taskStatistics.isEmpty()) {
            log.info("[Poseidon] Synchronous task statistics from this session:");

            // Iterate over each task and log their statistics
            for (Map.Entry<String, PerformanceStatistic> entry : taskStatistics.entrySet()) {
                String task = entry.getKey();
                PerformanceStatistic stats = entry.getValue();

                if (stats.getMaxExecutionTime() == 0) {
                    continue;
                }

                if (stats != null) {
                    log.info(String.format("[Poseidon] Task: %s - Processed %d events, Total Execution Time: %d ms, Avg Time: %d ms",
                            task,
                            stats.getEventCount(),
                            stats.getTotalExecutionTime(),
                            stats.getAverageExecutionTime()));
                } else {
                    log.warning("[Poseidon] No statistics available for task: " + task);
                }
            }
        }
        // Poseidon End
    }

    public void a() {
        this.isRunning = false;
    }

    public void run() {
        // NSMB Poseidon Start - OSL Networking
        NetServerHandlerImpl.setUp(this);

        ServerPlayNetworking.registerListener(HandshakePayload.CHANNEL, HandshakePayload::new, (server, handler, player, payload) -> {
            handler.registerClientChannels(payload.channels);
            PlayerPlayReadyEvent event = new PlayerPlayReadyEvent((Player) player.getBukkitEntity());
            Bukkit.getServer().getPluginManager().callEvent(event);
            return true;
        });
        // NSMB Poseidon End - OSL Networking

        try {
            if (this.init()) {
                long i = System.currentTimeMillis();

                for (long j = 0L; this.isRunning; Thread.sleep(1L)) {
                    if (modLoaderSupport) {
                        net.minecraft.server.ModLoader.OnTick(this);
                    }

                    long k = System.currentTimeMillis();
                    long l = k - i;

                    if (l > 2000L) {
                        log.warning("Can\'t keep up! Did the system time change, or is the server overloaded?");
                        l = 2000L;
                    }

                    if (l < 0L) {
                        log.warning("Time ran backwards! Did the system time change?");
                        l = 0L;
                    }

                    j += l;
                    i = k;
                    if (this.worlds.get(0).everyoneDeeplySleeping()) { // CraftBukkit
                        this.h();
                        j = 0L;
                    } else {
                        while (j > 50L) {
                            MinecraftServer.currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
                            getWatchdog().tickUpdate(); // Project Poseidon
                            j -= 50L;
                            this.h();
                        }
                    }
                }
            } else {
                while (this.isRunning) {
                    this.b();

                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException interruptedexception) {
                        interruptedexception.printStackTrace();
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.log(Level.SEVERE, "Unexpected exception", throwable);

            while (this.isRunning) {
                this.b();

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedexception1) {
                    interruptedexception1.printStackTrace();
                }
            }
        } finally {
            try {
                this.stop();
                this.isStopped = true;
            } catch (Throwable throwable1) {
                throwable1.printStackTrace();
            } finally {
                System.exit(0);
            }
        }
    }

    //Project Poseidon Start - Tick Update
    private final LinkedList<Double> tpsRecords = new LinkedList<>();
    private long lastTick = System.currentTimeMillis();
    private long lastTickNano = System.nanoTime();
    private long[] averageTickTimes = new long[100];
    private int tickCount = 0;

    public LinkedList<Double> getTpsRecords() {
        return tpsRecords;
    }

    public long[] getAverageTickTimes() {
        return averageTickTimes;
    }
    //Project Poseidon End - Tick Update

    private void h() {
        ArrayList arraylist = new ArrayList();
        Iterator iterator = trackerList.keySet().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            int i = ((Integer) trackerList.get(s)).intValue();

            if (i > 0) {
                trackerList.put(s, Integer.valueOf(i - 1));
            } else {
                arraylist.add(s);
            }
        }

        int j;

        for (j = 0; j < arraylist.size(); ++j) {
            trackerList.remove(arraylist.get(j));
        }

        AxisAlignedBB.a();
        Vec3D.a();
        ++this.ticks;

        ((CraftScheduler) this.server.getScheduler()).mainThreadHeartbeat(this.ticks); // CraftBukkit

        //Project Poseidon Start - Tick Update
        long currentTime = System.currentTimeMillis();
        this.lastTickNano = System.nanoTime();
        tickCount++;

        //Check if a second has passed
        if (currentTime - lastTick >= 1000) {
            double tps = tickCount / ((currentTime - lastTick) / 1000.0);
            tpsRecords.addFirst(tps);
            if (tpsRecords.size() > 900) { //Don't keep more than 15 minutes of data
                tpsRecords.removeLast();
            }

            tickCount = 0;
            lastTick = currentTime;
        }

        //Project Poseidon End - Tick Update


        for (j = 0; j < this.worlds.size(); ++j) { // CraftBukkit
            // if (j == 0 || this.propertyManager.getBoolean("allow-nether", true)) { // CraftBukkit
            WorldServer worldserver = this.worlds.get(j); // CraftBukkit

            if (this.ticks % 20 == 0) {
                // CraftBukkit start - only send timeupdates to the people in that world
                for (int i = 0; i < worldserver.players.size(); ++i) { // Project Poseidon: serverConfigurationManager -> worldserver.players
                    EntityPlayer entityPlayer = (EntityPlayer) worldserver.players.get(i);
                    if (entityPlayer != null) {
                        entityPlayer.netServerHandler.sendPacket(new Packet4UpdateTime(entityPlayer.getPlayerTime())); // Add support for per player time

                    }
                }
                // CraftBukkit end
            }

            worldserver.doTick();

            while (worldserver.doLighting()) {
                ;
            }

            worldserver.cleanUp();
        }
        // } // CraftBukkit

        this.networkListenThread.a();
        this.serverConfigurationManager.b();

        // CraftBukkit start
        for (j = 0; j < this.worlds.size(); ++j) {
            this.worlds.get(j).tracker.updatePlayers();
        }
        // CraftBukkit end

        for (j = 0; j < this.r.size(); ++j) {
            ((IUpdatePlayerListBox) this.r.get(j)).a();
        }

        try {
            this.b();
        } catch (Exception exception) {
            log.log(Level.WARNING, "Unexpected exception while parsing console command", exception);
        }

        //Project Poseidon Start - (MS per) Tick Update
        this.averageTickTimes[this.ticks % 100] = System.nanoTime() - this.lastTickNano;
        //Project Poseidon End - (MS per) Tick Update
    }

    public void issueCommand(String s, ICommandListener icommandlistener) {
        this.s.add(new ServerCommand(s, icommandlistener));
    }

    public void b() {
        while (this.s.size() > 0) {
            ServerCommand servercommand = (ServerCommand) this.s.remove(0);

            // CraftBukkit start - ServerCommand for preprocessing
            ServerCommandEvent event = new ServerCommandEvent(this.console, servercommand.command);
            this.server.getPluginManager().callEvent(event);
            servercommand = new ServerCommand(event.getCommand(), servercommand.b);
            // CraftBukkit end

            // this.consoleCommandHandler.handle(servercommand); // CraftBukkit - Removed its now called in server.dispatchCommand
            this.server.dispatchCommand(this.console, servercommand); // CraftBukkit
        }
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.r.add(iupdateplayerlistbox);
    }

    public static void main(final OptionSet options) { // CraftBukkit - replaces main(String args[])
        StatisticList.a();

        try {
            MinecraftServer minecraftserver = new MinecraftServer(options); // CraftBukkit - pass in the options

            // CraftBukkit - remove gui

            (new ThreadServerApplication("Server thread", minecraftserver)).start();
        } catch (Exception exception) {
            log.log(Level.SEVERE, "Failed to start the minecraft server", exception);
        }
    }

    public File a(String s) {
        return new File(s);
    }

    public void sendMessage(String s) {
        log.info(s);
    }

    public void c(String s) {
        log.warning(s);
    }

    public String getName() {
        return "CONSOLE";
    }

    public WorldServer getWorldServer(int i) {
        // CraftBukkit start
        for (WorldServer world : this.worlds) {
            if (world.dimension == i) {
                return world;
            }
        }

        return this.worlds.get(0);
        // CraftBukkit end
    }

    public EntityTracker getTracker(int i) {
        return this.getWorldServer(i).tracker; // CraftBukkit
    }

    public static boolean isRunning(MinecraftServer minecraftserver) {
        return minecraftserver.isRunning;
    }

    public WatchDogThread getWatchdog() {
        return Poseidon.getServer().getWatchDogThread();
    }
}
