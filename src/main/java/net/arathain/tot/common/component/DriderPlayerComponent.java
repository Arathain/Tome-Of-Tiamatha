package net.arathain.tot.common.component;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
import net.arathain.tot.common.entity.ToTUtil;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;
import java.util.UUID;

public class DriderPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    private static final EntityAttributeModifier DRIDER_ATTACK_SPEED_MODIFIER = new EntityAttributeModifier(UUID.fromString("c2b783da-45c2-4fc2-8ba8-9d5b0d81434d"), "Drider modifier", 3, EntityAttributeModifier.Operation.ADDITION);
    private static final EntityAttributeModifier DRIDER_MOVEMENT_SPEED_MODIFIER = new EntityAttributeModifier(UUID.fromString("53f0d4fe-52f6-4e23-b4d8-023a577fea2b"), "Drider modifier", 1.08, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier DRIDER_ATTACK_DAMAGE_MODIFIER = new EntityAttributeModifier(UUID.fromString("35279bb7-44e5-4b33-8134-c4e1d3156ba3"), "Drider modifier", -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier DRIDER_ARMOR_MODIFIER = new EntityAttributeModifier(UUID.fromString("5366c148-18ec-481d-948a-106ee533b437"), "Drider modifier", 12, EntityAttributeModifier.Operation.ADDITION);
    private static final EntityAttributeModifier DRIDER_ATTACK_RANGE_MODIFIER = new EntityAttributeModifier(UUID.fromString("e0db1b07-47c8-4c98-8751-92a0b6558f08"), "Drider modifier", -1, EntityAttributeModifier.Operation.ADDITION);
    private static final EntityAttributeModifier DRIDER_REACH_MODIFIER = new EntityAttributeModifier(UUID.fromString("c412efb9-3737-44ed-9014-490882e561ed"), "Drider modifier", -1, EntityAttributeModifier.Operation.ADDITION);

    private final PlayerEntity obj;
    private DriderEntity.Type variant = DriderEntity.Type.DARK;
    private boolean drider = false;
    private int stage = 0;

    public DriderPlayerComponent(PlayerEntity obj) {
        this.obj = obj;
    }

    public boolean isDrider() {
        return drider;
    }

    public DriderEntity.Type getVariant() {
        return variant;
    }
    public int getStage() {
        return stage;
    }
    public void setStage(int newStage) {
        stage = newStage;
        ToTComponents.DRIDER_COMPONENT.sync(obj);
    }

    public void setVariant(DriderEntity.Type variant) {
        this.variant = variant;
        ToTComponents.DRIDER_COMPONENT.sync(obj);
    }

    public void setDrider(boolean drider) {
        this.drider = drider;
        System.out.println(obj.getUuidAsString());
        setVariant(obj.getRandom().nextInt(10) == 1 ? DriderEntity.Type.ALBINO : DriderEntity.Type.DARK);
        if(Objects.equals(obj.getUuidAsString(), "1ece513b-8d36-4f04-9be2-f341aa8c9ee2")) setVariant(DriderEntity.Type.ARATHAIN);
        ToTComponents.DRIDER_COMPONENT.sync(obj);
        if(!obj.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) {
            obj.damage(DamageSource.CRAMMING, 10);
        }
        obj.sendEquipmentBreakStatus(EquipmentSlot.LEGS);
        obj.sendEquipmentBreakStatus(EquipmentSlot.FEET);
        updateAttributes();
    }

    @Override
    public void serverTick() {
        boolean dreeder = ToTUtil.isDrider(obj);
        if (dreeder) {
            obj.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void updateAttributes() {
        boolean dreeder = ToTUtil.isDrider(obj);
        EntityAttributeInstance attackSpeedAttribute = obj.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        EntityAttributeInstance armorAttribute = obj.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        EntityAttributeInstance movementSpeedAttribute = obj.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        EntityAttributeInstance attackDamageAttribute = obj.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        EntityAttributeInstance attackRange = obj.getAttributeInstance(ReachEntityAttributes.ATTACK_RANGE);
        EntityAttributeInstance reach = obj.getAttributeInstance(ReachEntityAttributes.REACH);
        if (dreeder && !attackSpeedAttribute.hasModifier(DRIDER_ATTACK_SPEED_MODIFIER)) {
            attackSpeedAttribute.addPersistentModifier(DRIDER_ATTACK_SPEED_MODIFIER);
            attackDamageAttribute.addPersistentModifier(DRIDER_ATTACK_DAMAGE_MODIFIER);
            movementSpeedAttribute.addPersistentModifier(DRIDER_MOVEMENT_SPEED_MODIFIER);
            armorAttribute.addPersistentModifier(DRIDER_ARMOR_MODIFIER);
            attackRange.addPersistentModifier(DRIDER_ATTACK_RANGE_MODIFIER);
            reach.addPersistentModifier(DRIDER_REACH_MODIFIER);
            if(!obj.world.isClient) {
                TomeOfTiamatha.DRIDER_LOCK.lock(obj, DefaultInventoryNodes.LEGS);
                TomeOfTiamatha.DRIDER_LOCK.lock(obj, DefaultInventoryNodes.FEET);
            }
        }
        else if (!dreeder && attackSpeedAttribute.hasModifier(DRIDER_ATTACK_SPEED_MODIFIER)) {
            attackSpeedAttribute.removeModifier(DRIDER_ATTACK_SPEED_MODIFIER);
            movementSpeedAttribute.removeModifier(DRIDER_MOVEMENT_SPEED_MODIFIER);
            attackDamageAttribute.removeModifier(DRIDER_ATTACK_DAMAGE_MODIFIER);
            armorAttribute.removeModifier(DRIDER_ARMOR_MODIFIER);
            attackRange.removeModifier(DRIDER_ATTACK_RANGE_MODIFIER);
            reach.removeModifier(DRIDER_REACH_MODIFIER);
        }
    }


    @Override
    public void readFromNbt(NbtCompound tag) {
        setDrider(tag.getBoolean("drider"));
        if (tag.contains("Type")) {
            this.setVariant(DriderEntity.Type.valueOf(tag.getString("Type")));
        }
        setStage(tag.getInt("stage"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("drider", isDrider());
        if(this.getVariant() != null) {
            tag.putString("Type", this.getVariant().toString());
        }
        tag.putInt("stage", this.getStage());
    }

}
