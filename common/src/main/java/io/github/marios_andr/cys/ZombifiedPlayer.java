package io.github.marios_andr.cys;

import com.mojang.serialization.Codec;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.SpearUseGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.world.entity.Avatar.DEFAULT_MAIN_HAND;

public class ZombifiedPlayer extends Zombie { //  Compatibility with inventory extending mods?

    private final NonNullList<ItemStack> inventory = NonNullList.create();
    private int xpToDrop = 0;

    private UUID conversionStarter;
    private int zombieConversionTime;

    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.RESOLVABLE_PROFILE);
    protected static final EntityDataAccessor<HumanoidArm> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.HUMANOID_ARM);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombifiedPlayer.class, EntityDataSerializers.BOOLEAN);

    // Taken from Mannequin
    private static final byte ALL_LAYERS = (byte) Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getMask).reduce(0, (a, b) -> a | b);
    private static final Codec<Byte> LAYERS_CODEC = PlayerModelPart.CODEC
            .listOf()
            .xmap(
                    list -> (byte) list.stream().mapToInt(PlayerModelPart::getMask).reduce(ALL_LAYERS, (a, b) -> a & ~b),
                    mask -> Arrays.stream(PlayerModelPart.values()).filter(part -> (mask & part.getMask()) == 0).toList()
            );

    protected static EntityType.EntityFactory<ZombifiedPlayer> constructor = ZombifiedPlayer::new;

    public static ZombifiedPlayer create(EntityType<ZombifiedPlayer> type, Level level) {
        return constructor.create(type, level);
    }

    public ZombifiedPlayer(EntityType<? extends ZombifiedPlayer> type, Level level) {
        super(type, level);
        this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, ALL_LAYERS);

    }

    public ZombifiedPlayer(Level level) {
        this(CysCommon.ZOMBIFIED_PLAYER.get(), level);
    }

    public ZombifiedPlayer(Level level, ServerPlayer player) {
        super(CysCommon.ZOMBIFIED_PLAYER.get(), level);
        this.setProfile(player.getProfile());
        this.setMainArm(player.getMainArm());
        this.setModelPartShown(player);
        this.setPos(player.getX(), player.getY(), player.getZ());

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            this.inventory.add(inv.getItem(i));
        }

        switch (Constants.zombieExperienceOptions) {
            case STORE_FULL ->
                    this.xpToDrop = Util.calculateTotalExperience(player.experienceLevel, player.experienceProgress);
            case STORE_PARTIAL -> this.xpToDrop = player.getExperienceReward((ServerLevel) level, null);
            case NONE -> this.xpToDrop = 0;
        }

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);

        this.equipment.set(EquipmentSlot.HEAD, head);
        this.equipment.set(EquipmentSlot.CHEST, chest);
        this.equipment.set(EquipmentSlot.LEGS, legs);
        this.equipment.set(EquipmentSlot.FEET, feet);
        this.equipment.set(EquipmentSlot.MAINHAND, mainhand);
        this.equipment.set(EquipmentSlot.OFFHAND, offhand);
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

    //===================================================Synched Data===================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_PROFILE, ClientMannequin.DEFAULT_PROFILE);
        entityData.define(DATA_PLAYER_MAIN_HAND, DEFAULT_MAIN_HAND);
        entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0);
        entityData.define(DATA_CONVERTING_ID, false);
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
            this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) (this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION) | part.getMask()));
        } else {
            this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) (this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION) & ~part.getMask()));
        }
    }

    public boolean isModelPartShown(PlayerModelPart part) {
        return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & part.getMask()) == part.getMask();
    }

    public boolean isConverting() {
        return this.getEntityData().get(DATA_CONVERTING_ID);
    }

    //================================================ Data Components =================================================

    @Override
    public <T> T get(DataComponentType<? extends T> type) {
        return type == DataComponents.PROFILE ? castComponentValue((DataComponentType<T>) type, this.getProfile()) : super.get(type);
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

    //=================================================== Overrides ====================================================

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.isAlive() && this.isConverting() && this.canConvert()) {
            this.zombieConversionTime -= 1;
            if (this.zombieConversionTime <= 0) {
                UUID id = this.getProfile().partialProfile().id();
                Player target = this.level().getPlayerByUUID(id);

                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();
                double yRot = this.getYRot();
                double xRot = this.getXRot();

                target.teleportTo((ServerLevel) this.level(), x, y, z, Set.of(), (float)yRot, (float)xRot, true);
                target.setHealth(3);
                target.giveExperiencePoints(this.xpToDrop);
                for (ItemStack itemStack : this.inventory) {
                    if (!itemStack.isEmpty() && !target.addItem(itemStack)) {
                        this.drop(itemStack, true, false);
                    }
                }
                ((ServerPlayer) target).setGameMode(GameType.SURVIVAL);


                this.remove(RemovalReason.DISCARDED);
            }
        }

        super.tick();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!Constants.zombieImmunity)
            return super.hurtServer(level, source, damage);

        boolean flag = source.is(DamageTypes.OUTSIDE_BORDER);
        flag |= source.is(DamageTypes.FELL_OUT_OF_WORLD);
        flag |= source.is(DamageTypes.GENERIC_KILL);

        if (flag) {
        } else if (!(source.getEntity() instanceof Player))
            return false;

        return super.hurtServer(level, source, damage);
    }

    private void startConverting(UUID player, int time) {
        this.conversionStarter = player;
        this.zombieConversionTime = time;
        this.getEntityData().set(DATA_CONVERTING_ID, true);
        this.removeEffect(MobEffects.WEAKNESS);
        this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, time, Math.min(this.level().getDifficulty().getId() - 1, 0)));
        this.level().broadcastEntityEvent(this, (byte) 16);
    }

    private boolean canConvert() {
        if (!Constants.isHardcoreCuringEnabled)
            return false;
        UUID id = this.getProfile().partialProfile().id();
        Player target = this.level().getPlayerByUUID(id);
        return target != null && target.isSpectator() && this.level().getLevelData().isHardcore();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.GOLDEN_APPLE)) {
            if (canConvert() && this.hasEffect(MobEffects.WEAKNESS)) {
                itemStack.consume(1, player);
                if (!this.level().isClientSide()) {
                    this.startConverting(player.getUUID(), 48);
                }

                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.CONSUME;
            }
        } else {
            return super.mobInteract(player, hand);
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
    public boolean shouldShowName() {
        return this.getProfile().name().isPresent();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(this.getProfile().name().orElse("Zombified Player"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData groupData) {
        return groupData;
    }

    //====================================================== Loot ======================================================

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty() && !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                this.drop(itemStack, true, false);
            }
        }
    }

    @Override
    protected ItemEntity createItemStackToDrop(ItemStack itemStack, boolean randomly, boolean thrownFromHand) {
        if (itemStack.isEmpty()) {
            return null;
        } else {
            double yHandPos = this.getEyeY() - 0.3F;
            ItemEntity entity = new ItemEntity(this.level(), this.getX(), yHandPos, this.getZ(), itemStack);
            entity.setPickUpDelay(40);
            if (thrownFromHand) {
                entity.setThrower(this);
            }

            if (randomly) {
                float pow = this.random.nextFloat() * 0.06F;
                float dir = this.random.nextFloat() * (float) (Math.PI * 2);
                entity.setDeltaMovement(-Mth.sin(dir) * pow, 0.2F, Mth.cos(dir) * pow);
            } else {
                float pow = 0.3F;
                float sinX = Mth.sin(this.getXRot() * (float) (Math.PI / 180.0));
                float cosX = Mth.cos(this.getXRot() * (float) (Math.PI / 180.0));
                float sinY = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
                float cosY = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
                float dir = this.random.nextFloat() * (float) (Math.PI * 2);
                float pow2 = 0.02F * this.random.nextFloat();
                entity.setDeltaMovement(
                        -sinY * cosX * 0.3F + Math.cos(dir) * pow2,
                        -sinX * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                        cosY * cosX * 0.3F + Math.sin(dir) * pow2
                );
            }

            return entity;
        }
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return true;
    }

    @Override
    protected void dropExperience(ServerLevel level, Entity killer) {
        //super.dropExperience(level, killer);
        if (this.xpToDrop > 0) {
            if (killer instanceof Player p) {
                p.giveExperiencePoints(this.xpToDrop);
            } else {
                ExperienceOrb.award(level, this.position(), this.xpToDrop);
            }
        }
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

        int conversionTime = input.getIntOr("conversion_time", -1);
        if (conversionTime != -1) {
            UUID conversionStarter = input.read("conversion_player", UUIDUtil.CODEC).orElse(null);
            this.startConverting(conversionStarter, conversionTime);
        } else {
            this.getEntityData().set(DATA_CONVERTING_ID, false);
            this.zombieConversionTime = -1;
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

        output.putInt("conversion_time", this.isConverting() ? this.zombieConversionTime : -1);
        output.storeNullable("conversion_player", UUIDUtil.CODEC, this.conversionStarter);
    }
}