package io.github.marios_andr.cys.platform;

import io.github.marios_andr.cys.Constants;
import io.github.marios_andr.cys.CysCommon;
import io.github.marios_andr.cys.CysNeoforgeMod;
import io.github.marios_andr.cys.platform.services.IPlatformHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class NeoForgePlatformHelper implements IPlatformHelper {

    private static final EntityCapability<ResourceHandler<ItemResource>, Void> CURIOS_INVENTORY;

    static {
        if (ModList.get().isLoaded("curios")) {
            CURIOS_INVENTORY = EntityCapability.createVoid(Identifier.fromNamespaceAndPath("curios", "item_handler"), ResourceHandler.asClass());
        } else
            CURIOS_INVENTORY = null;
    }

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public NonNullList<ItemStack> getPlayerCurio(Player player) {
        if (isModLoaded("curios")) {
            NonNullList<ItemStack> stacks = NonNullList.create();

            try {
                //var test = player.getCapability(Capabilities.Item.ENTITY); doesn't work

                ResourceHandler<ItemResource> rh = player.getCapability(CURIOS_INVENTORY);
                try (Transaction tr = Transaction.openRoot()) {
                    ItemResource item;
                    ItemStack stack;
                    for (int i = 0; i < rh.size(); i++) {
                        item = rh.getResource(i);
                        if (item.isEmpty()) continue;
                        stack = item.toStack();
                        stacks.add(stack);
                        rh.extract(i, item, stack.count(), tr);
                    }
                    tr.commit();
                }
            }  catch (Exception e) {
                Constants.LOG.error("Something went wrong while copying curio inventory to zombie. Please report this.", e);
            }

            return stacks;
        }

        return NonNullList.create();
    }
}