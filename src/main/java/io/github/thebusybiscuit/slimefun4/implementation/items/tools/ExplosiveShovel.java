package io.github.thebusybiscuit.slimefun4.implementation.items.tools;

import io.github.starwishsama.utils.ProtectionChecker;
import io.github.thebusybiscuit.cscorelib2.materials.MaterialTools;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SimpleSlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.DamageableItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.NotPlaceable;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockBreakHandler;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The {@link ExplosiveShovel} works similar to the {@link ExplosivePickaxe}.
 * However it can only break blocks that a shovel can break.
 *
 * @author Linox
 * @see ExplosivePickaxe
 */
public class ExplosiveShovel extends SimpleSlimefunItem<BlockBreakHandler> implements NotPlaceable, DamageableItem {

    private boolean damageOnUse;

    public ExplosiveShovel(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String[] keys, Object[] values) {
        super(category, item, recipeType, recipe, keys, values);
    }

    @Override
    public BlockBreakHandler getItemHandler() {
        return new BlockBreakHandler() {

            @Override
            public boolean isPrivate() {
                return false;
            }

            @Override
            public boolean onBlockBreak(BlockBreakEvent e, ItemStack item, int fortune, List<ItemStack> drops) {
                if (isItem(item)) {
                    if (Slimefun.hasUnlocked(e.getPlayer(), ExplosiveShovel.this, true)) {
                        e.getBlock().getWorld().createExplosion(e.getBlock().getLocation(), 0.0F);
                        e.getBlock().getWorld().playSound(e.getBlock().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3F, 1F);

                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && y == 0 && z == 0) {
                                        continue;
                                    }

                                    Block b = e.getBlock().getRelative(x, y, z);

                                    if (MaterialTools.getBreakableByShovel().contains(b.getType()) && SlimefunPlugin.getProtectionManager().hasPermission(e.getPlayer(), b.getLocation(), ProtectableAction.BREAK_BLOCK) && ProtectionChecker.check(e.getPlayer(), b, false)) {
                                        SlimefunPlugin.getProtectionManager().logAction(e.getPlayer(), b, ProtectableAction.BREAK_BLOCK);

                                        b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());

                                        for (ItemStack drop : b.getDrops(getItem())) {
                                            if (drop != null) {
                                                b.getWorld().dropItemNaturally(b.getLocation(), drop);
                                            }
                                        }

                                        b.setType(Material.AIR);
                                        damageItem(e.getPlayer(), item);
                                    }
                                }
                            }
                        }
                    }

                    return true;
                } else return false;
            }
        };
    }

    @Override
    public void postRegister() {
        damageOnUse = ((boolean) Slimefun.getItemValue(getID(), "damage-on-use"));
    }

    @Override
    public boolean isDamageable() {
        return damageOnUse;
    }

}
