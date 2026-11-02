package de.verdox.mccreativelab.recipe;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record CustomItemData(@NotNull Material material, int customModelData) {
    @NotNull
    public ItemStack createStack() {
        var stack = ItemStack.of(material); // Oder new ItemStack(material);
        if (customModelData != 0) {
            stack.setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA,
                    CustomModelData.customModelData().addFloat(customModelData).build()
            );
        }
        return stack;
    }

    public boolean isSame(@NotNull ItemStack stackToCheck) {
        if (!stackToCheck.getType().equals(material))
            return false;
        boolean hasCustomModelData = stackToCheck.hasData(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (customModelData == 0) {
            if (!hasCustomModelData) return true;
            CustomModelData cmd = stackToCheck.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            return cmd == null || cmd.floats().isEmpty() || cmd.floats().getFirst() == 0;
        } else {
            if (!hasCustomModelData) return false;
            CustomModelData cmd = stackToCheck.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (cmd == null || cmd.floats().isEmpty()) return false;
            return cmd.floats().getFirst() == (float) customModelData;
        }
    }

    @NotNull
    public static CustomItemData fromItemStack(@NotNull ItemStack stack) {
        int cmdValue = 0;
        if (stack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            CustomModelData cmd = stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (cmd != null && !cmd.floats().isEmpty()) {
                cmdValue = (int) cmd.floats().getFirst().floatValue();
            }
        }
        return new CustomItemData(stack.getType(), cmdValue);
    }
}
