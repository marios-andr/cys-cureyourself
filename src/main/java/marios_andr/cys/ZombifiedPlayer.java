package marios_andr.cys;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.SpearUseGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static net.minecraft.world.entity.Avatar.DEFAULT_MAIN_HAND;
import static net.minecraft.world.entity.decoration.Mannequin.DEFAULT_PROFILE;

public class ZombifiedPlayer extends Zombie { // consider peaceful and hardcore, experience options, nametag?, Compatibility with inventory extending mods, keep inventory?
    private final NonNullList<ItemStack> inventory = NonNullList.create();
    private int xpToDrop = 0;

    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.RESOLVABLE_PROFILE);
    protected static final EntityDataAccessor<HumanoidArm> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.HUMANOID_ARM);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.BYTE);

    // Taken from Mannequin
    private static final byte ALL_LAYERS = (byte)Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getMask).reduce(0, (a, b) -> a | b);
    private static final Codec<Byte> LAYERS_CODEC = PlayerModelPart.CODEC
            .listOf()
            .xmap(
                    list -> (byte)list.stream().mapToInt(PlayerModelPart::getMask).reduce(ALL_LAYERS, (a, b) -> a & ~b),
                    mask -> Arrays.stream(PlayerModelPart.values()).filter(part -> (mask & part.getMask()) == 0).toList()
            );

    public ZombifiedPlayer(EntityType<? extends ZombifiedPlayer> type, Level level) {
        super(type, level);
        this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, ALL_LAYERS);

    }

    public ZombifiedPlayer(Level level) {
        this(CysMod.ZOMBIFIED_PLAYER.get(), level);
    }

    public ZombifiedPlayer(Level level, ServerPlayer player) {
        super(CysMod.ZOMBIFIED_PLAYER.get(), level);
        this.setProfile(player.getProfile());
        this.setMainArm(player.getMainArm());
        this.setModelPartShown(player);
        this.setPos(player.getX(), player.getY(), player.getZ());

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            this.inventory.add(inv.getItem(i));
        }

        if (Config.zombieExperienceOptions.equals(Config.ExperienceOptions.STORE_FULL)) {
            this.xpToDrop = player.totalExperience;
        } else {
            this.xpToDrop = player.getExperienceReward((ServerLevel) level, null);
        }

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);

        this.setItemSlot(EquipmentSlot.HEAD, head, true);
        this.setItemSlot(EquipmentSlot.CHEST, chest, true);
        this.setItemSlot(EquipmentSlot.LEGS, legs, true);
        this.setItemSlot(EquipmentSlot.FEET, feet, true);
        this.setItemSlot(EquipmentSlot.MAINHAND, mainhand, true);
        this.setItemSlot(EquipmentSlot.OFFHAND, offhand, true);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new SpearUseGoal<>(this, 1.0, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_PROFILE, DEFAULT_PROFILE);
        entityData.define(DATA_PLAYER_MAIN_HAND, DEFAULT_MAIN_HAND);
        entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
    }

    public ResolvableProfile getProfile() {
        return this.entityData.get(DATA_PROFILE);
    }

    private void setProfile(ResolvableProfile profile) {
        this.entityData.set(DATA_PROFILE, profile);
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.entityData.get(DATA_PLAYER_MAIN_HAND);
    }

    private void setMainArm(HumanoidArm mainArm) {
        this.entityData.set(DATA_PLAYER_MAIN_HAND, mainArm);
    }

    private void setModelPartShown(ServerPlayer player) {
        for (PlayerModelPart part : PlayerModelPart.values()) {
            setModelPartShown(part, player.isModelPartShown(part));
        }
    }

    private void setModelPartShown(PlayerModelPart part, boolean shown) {
        if (shown) {
            this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)(this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION) | part.getMask()));
        } else {
            this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)(this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION) & ~part.getMask()));
        }
    }

    public boolean isModelPartShown(PlayerModelPart part) {
        return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & part.getMask()) == part.getMask();
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        return type == DataComponents.PROFILE ? castComponentValue((DataComponentType<T>)type, this.getProfile()) : super.get(type);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.applyImplicitComponentIfPresent(components, DataComponents.PROFILE);
        super.applyImplicitComponents(components);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        if (type == DataComponents.PROFILE) {
            this.setProfile(castComponentValue(DataComponents.PROFILE, value));
            return true;
        } else {
            return super.applyImplicitComponent(type, value);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        // Do nothing
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!Config.zombieImmunity)
            return super.hurtServer(level, source, damage);

        boolean flag = source.is(DamageTypes.OUTSIDE_BORDER);
        flag |= source.is(DamageTypes.FELL_OUT_OF_WORLD);
        flag |= source.is(DamageTypes.GENERIC_KILL);

        if (flag) {
        } else if (!(source.getEntity() instanceof Player))
            return false;

        return super.hurtServer(level, source, damage);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);
    }

    @Override
    protected void dropExperience(ServerLevel level, @Nullable Entity killer) {
        super.dropExperience(level, killer);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        return groupData;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("profile", ResolvableProfile.CODEC).ifPresent(this::setProfile);
        this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, input.read("hidden_layers", LAYERS_CODEC).orElse(ALL_LAYERS));
        this.setMainArm(input.read("main_hand", HumanoidArm.CODEC).orElse(DEFAULT_MAIN_HAND));
        this.xpToDrop = input.read("experience", Codec.INT).orElse(0);
        ValueInput.TypedInputList<ItemStack> list = input.listOrEmpty("inventory", ItemStack.CODEC);
        this.inventory.clear();
        for (ItemStack item : list) {
            this.inventory.add(item);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("profile", ResolvableProfile.CODEC, this.getProfile());
        output.store("hidden_layers", LAYERS_CODEC, this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION));
        output.store("main_hand", HumanoidArm.CODEC, this.getMainArm());
        output.store("experience", Codec.INT, this.xpToDrop);
        ValueOutput.TypedOutputList<ItemStack> list = output.list("inventory", ItemStack.CODEC);
        for (ItemStack itemStack : this.inventory) {
            list.add(itemStack);
        }
    }
}
