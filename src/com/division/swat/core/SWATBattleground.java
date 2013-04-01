package com.division.swat.core;

import com.division.battlegrounds.core.Battleground;
import com.division.battlegrounds.core.BattlegroundPlayer;
import com.division.battlegrounds.core.Gametype;
import com.division.battlegrounds.event.BattlegroundJoinEvent;
import com.division.battlegrounds.event.BattlegroundQuitEvent;
import com.division.battlegrounds.event.RoundEndEvent;
import com.division.battlegrounds.event.RoundStartEvent;
import com.division.battlegrounds.mech.FriendlyFireBypass;
import com.division.battlegrounds.region.Region;
import com.division.battlegrounds.storage.StorageManager;
import com.division.battlegrounds.storage.SupplyCrate;
import com.division.swat.config.SWConfig;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Evan
 */
public class SWATBattleground extends Battleground {

    private Set<Player> teamRed = new HashSet<Player>();
    private Set<Player> teamBlue = new HashSet<Player>();
    private SWATGametype SWgametype;

    public SWATBattleground(String name, Gametype gametype, Region region) {
        super(name, gametype, region);
        this.setMinPlayers(SWConfig.getMinPlayers());
        this.setMaxPlayers(SWConfig.getMaxPlayers());
        this.setDynamic(true);
        this.SWgametype = (SWATGametype) gametype;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        loadOfflineStorage(evt.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent evt) {
        if (!(evt.getEntity() instanceof Player)) {
            return;
        }

        Player defender = (Player) evt.getEntity();
        if (!isPlayerInBattleground(defender)) {
            return;
        }
        if (evt.getCause() == DamageCause.FALL) {
            evt.setCancelled(true);
            return;
        }
        EntityDamageByEntityEvent edee;
        if (evt instanceof EntityDamageByEntityEvent) {
            edee = (EntityDamageByEntityEvent) evt;
        } else {
            return;
        }
        Entity eAttacker = checkSource(edee.getDamager());
        if (eAttacker instanceof Player) {
            Player attacker = (Player) eAttacker;
            if (isOnSameTeam(attacker, defender)) {
                evt.setCancelled(true);
                return;
            }
            if (evt.isCancelled()) {
                FriendlyFireBypass.damage(defender, true, evt.getDamage());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent evt) {
        Player player = evt.getEntity();
        if (!isPlayerInBattleground(player)) {
            return;
        }
        evt.getDrops().clear();
        if (teamRed.contains(player)) {
            SWgametype.incrementRedScore();
        } else {
            SWgametype.incrementBlueScore();
        }
    }

    public boolean isOnSameTeam(Player attacker, Player defender) {
        if ((teamRed.contains(attacker) && teamRed.contains(defender)) || (teamBlue.contains(attacker) && teamBlue.contains(defender))) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        //TODO Add spawnpoint code to config and region.
        Player player = evt.getPlayer();
        if (isPlayerInBattleground(player)) {
            System.out.println("[SWATBG] Attempting to respawn player: " + player.getName());
            evt.setRespawnLocation(getSpawnPoint(player));
            Bukkit.getServer().getScheduler().runTaskLater(SWAT.instance, new ResupplyRunnable(player), 5L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBattlegroundLeave(BattlegroundQuitEvent evt) {
        if (evt.getBattleground() == this) {
            loadOfflineStorage(evt.getPlayer());
            evt.getPlayer().setHealth(20);
            evt.getPlayer().setFoodLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBattlegroundJoin(BattlegroundJoinEvent evt) {
        if (evt.getBattleground() == this) {
            distributeToTeam(evt.getPlayer());
            StorageManager.saveStorageCrate(evt.getPlayer().getInventory(), evt.getPlayer().getName());
            this.getQueue().remove(evt.getPlayer());
            this.sendToSpawnPoint(evt.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRoundPrep(RoundStartEvent evt) {
        if (!evt.getBattleground().getName().equals(this.getName())) {
            System.out.println("[SWATBG] not a SWAT roundStart, ignoring event.");
            return;
        }
        Random rand = new Random();
        Player[] players = evt.getPlayersToBeAdded().toArray(new Player[0]);
        for (Player p : players) {
            boolean rndBool = rand.nextBoolean();
            if (rndBool) {
                if (!(teamBlue.size() >= (this.getMinPlayers() / 2))) {
                    System.out.println("[SWATBG] Player: " + p.getName() + " has been added to blue team.");
                    teamBlue.add(p);
                } else {
                    System.out.println("[SWATBG] Player: " + p.getName() + " has been added to red team.");
                    teamRed.add(p);
                }
            } else {
                if (!(teamRed.size() >= (this.getMinPlayers() / 2))) {
                    System.out.println("[SWATBG] Player: " + p.getName() + " has been added to red team.");
                    teamRed.add(p);
                } else {
                    System.out.println("[SWATBG] Player: " + p.getName() + " has been added to blue team.");
                    teamBlue.add(p);
                }
            }
            StorageManager.saveStorageCrate(p.getInventory(), p.getName());
            this.inBattleground.put(new BattlegroundPlayer(p.getName()), p.getLocation());
            this.getQueue().remove(p);
        }
        Set<BattlegroundPlayer> inGamePlayers = this.inBattleground.keySet();
        System.out.println("Got to spawn code.");
        for (BattlegroundPlayer bgPlayer : inGamePlayers) {
            System.out.println("[SWATBG] Player: " + bgPlayer.getName() + " was sent to their spawn point.");
            this.sendToSpawnPoint(bgPlayer.getPlayer());
        }
        System.out.println("Got past spawn code.");
    }

    public void loadOfflineStorage(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
        SupplyCrate crate = StorageManager.loadStorageCrate(player.getName());
        if (crate != null) {
            player.teleport(player.getWorld().getSpawnLocation());
            inv.setContents(crate.uncrateContents());
            inv.setArmorContents(crate.uncrateArmor());
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "Battlegrounds" + ChatColor.GRAY + "]" + ChatColor.GREEN + "Your inventory from SWAT has been loaded");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void handleRoundEnd(RoundEndEvent evt) {
        if (evt.getBattleground() == this) {
            Set<BattlegroundPlayer> bgPlayers = this.inBattleground.keySet();
            for (BattlegroundPlayer bgPlayer : bgPlayers) {
                bgPlayer.getPlayer().teleport(inBattleground.get(bgPlayer));
                this.loadOfflineStorage(bgPlayer.getPlayer());
            }
            this.inBattleground.clear();
        }
    }

    public Entity checkSource(Entity source) {
        if (source instanceof Player) {
            return source;
        }
        if ((source instanceof Projectile) && (((Projectile) source).getShooter() instanceof Player)) {
            return ((Projectile) source).getShooter();
        }
        if ((source instanceof ThrownPotion) && (((ThrownPotion) source).getShooter() instanceof Player)) {
            return ((ThrownPotion) source).getShooter();
        }
        return null;
    }

    public void sendToSpawnPoint(Player player) {
        Location red = SWConfig.getRedSpawn();
        Location blue = SWConfig.getBlueSpawn();
        if (teamRed.contains(player)) {
            player.teleport(red);
        } else {
            player.teleport(blue);
        }
        this.reSupply(player);
    }

    public Location getSpawnPoint(Player player) {
        Location red = SWConfig.getRedSpawn();
        Location blue = SWConfig.getBlueSpawn();
        if (teamRed.contains(player)) {
            return red;
        } else {
            return blue;
        }
    }

    public void reSupply(Player player) {
        player.setHealth(2);
        player.setFoodLevel(15);
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
        inv.addItem(new ItemStack(Material.BOW, 1));
        inv.addItem(new ItemStack(Material.ARROW, 64));
    }

    public void distributeToTeam(Player player) {
        int redCount = teamRed.size();
        int blueCount = teamBlue.size();
        if (redCount > blueCount) {
            teamBlue.add(player);
        } else if (blueCount > redCount) {
            teamRed.add(player);
        } else {
            Random rand = new Random();
            if (rand.nextBoolean()) {
                teamBlue.add(player);
            } else {
                teamRed.add(player);
            }
        }
    }

    public class ResupplyRunnable implements Runnable {

        private Player target;

        public ResupplyRunnable(Player player) {
            this.target = player;
        }

        @Override
        public void run() {
            reSupply(target);
        }
    }
}
