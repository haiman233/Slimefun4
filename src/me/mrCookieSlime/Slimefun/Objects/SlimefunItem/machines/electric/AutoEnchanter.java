package me.mrCookieSlime.Slimefun.Objects.SlimefunItem.machines.electric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.InvUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.EmeraldEnchants.EmeraldEnchants;
import me.mrCookieSlime.EmeraldEnchants.ItemEnchantment;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineHelper;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.energy.ChargableBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class AutoEnchanter extends AContainer {

	public AutoEnchanter(Category category, ItemStack item, String name, RecipeType recipeType, ItemStack[] recipe) {
		super(category, item, name, recipeType, recipe);
	}

	@Override
	public String getInventoryTitle() {
		return "&5自动附魔机";
	}

	@Override
	public ItemStack getProgressBar() {
		return new ItemStack(Material.GOLDEN_CHESTPLATE);
	}

	@Override
	public void registerDefaultRecipes() {}

	@Override
	public int getEnergyConsumption() {
		return 9;
	}

	@Override
	protected void tick(Block b) {
		if (isProcessing(b)) {
			int timeleft = progress.get(b);
			if (timeleft > 0) {
                MachineHelper.updateProgressbar(BlockStorage.getInventory(b), 22, timeleft, processing.get(b).getTicks(), getProgressBar());

				if (ChargableBlock.isChargable(b)) {
					if (ChargableBlock.getCharge(b) < getEnergyConsumption()) return;
					ChargableBlock.addCharge(b, -getEnergyConsumption());
					progress.put(b, timeleft - 1);
				}
				else progress.put(b, timeleft - 1);
			}
			else {
				BlockStorage.getInventory(b).replaceExistingItem(22, new CustomItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
				pushItems(b, processing.get(b).getOutput());

				progress.remove(b);
				processing.remove(b);
			}
		}
		else {
			MachineRecipe r = null;
			slots:
			for (int slot: getInputSlots()) {
				ItemStack target = BlockStorage.getInventory(b).getItemInSlot(slot == getInputSlots()[0] ? getInputSlots()[1]: getInputSlots()[0]);
				// Check if enchantable
				SlimefunItem sfTarget = SlimefunItem.getByItem(target);
				if(sfTarget != null && !sfTarget.isEnchantable()) return;
				
				ItemStack item = BlockStorage.getInventory(b).getItemInSlot(slot);
				
				// Enchant
				if (item != null && item.getType() == Material.ENCHANTED_BOOK && target != null) {
					Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
					Set<ItemEnchantment> enchantments2 = new HashSet<ItemEnchantment>();
					int amount = 0;
					int specialAmount = 0;
					EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
					for (Map.Entry<Enchantment, Integer> e: meta.getStoredEnchants().entrySet()) {
						if (e.getKey().canEnchantItem(target)) {
							amount++;
							enchantments.put(e.getKey(), e.getValue());
						}
					}
					if (Slimefun.isEmeraldEnchantsInstalled()) {
						for (ItemEnchantment enchantment: EmeraldEnchants.getInstance().getRegistry().getEnchantments(item)) {
							if (EmeraldEnchants.getInstance().getRegistry().isApplicable(target, enchantment.getEnchantment()) && EmeraldEnchants.getInstance().getRegistry().getEnchantmentLevel(target, enchantment.getEnchantment().getName()) < enchantment.getLevel()) {
								amount++;
								specialAmount++;
								enchantments2.add(enchantment);
							}
						}
						specialAmount += EmeraldEnchants.getInstance().getRegistry().getEnchantments(target).size();
					}
					if (amount > 0 && specialAmount <= SlimefunPlugin.instance.getSettings().emeraldEnchantsLimit) {
						ItemStack newItem = target.clone();
						for (Map.Entry<Enchantment, Integer> e: enchantments.entrySet()) {
							newItem.addUnsafeEnchantment(e.getKey(), e.getValue());
						}
						for (ItemEnchantment e: enchantments2) {
							EmeraldEnchants.getInstance().getRegistry().applyEnchantment(newItem, e.getEnchantment(), e.getLevel());
						}
						r = new MachineRecipe(75 * amount, new ItemStack[] {target, item}, new ItemStack[] {newItem, new ItemStack(Material.BOOK)});
					}
					break slots;
				}
			}

			if (r != null) {
				if (!fits(b, r.getOutput())) return;
				for (int slot: getInputSlots()) {
					BlockStorage.getInventory(b).replaceExistingItem(slot, InvUtils.decreaseItem(BlockStorage.getInventory(b).getItemInSlot(slot), 1));
				}
				processing.put(b, r);
				progress.put(b, r.getTicks());
			}
		}
	}

	@Override
	public int getSpeed() {
		return 1;
	}

	@Override
	public String getMachineIdentifier() {
		return "AUTO_ENCHANTER";
	}

}