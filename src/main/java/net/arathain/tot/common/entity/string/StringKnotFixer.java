package net.arathain.tot.common.entity.string;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;

public class StringKnotFixer extends NbtFixer {
    public static final StringKnotFixer INSTANCE = new StringKnotFixer();

    @Override
    protected int getVersion() {
        return 2_01_00;
    }

    @Override
    public void registerFixers() {
        addFix(2_01_00, "Make Chains position relative", this::fixStringPos201);
    }

    /**
     * Make chain positions relative
     */
    private NbtCompound fixStringPos201(NbtCompound nbt) {
        if (isNotChainKnot201(nbt)) return nbt;
        NbtList pos = nbt.getList("Pos", NbtType.DOUBLE);
        int sx = MathHelper.floor(pos.getDouble(0));
        int sy = MathHelper.floor(pos.getDouble(1));
        int sz = MathHelper.floor(pos.getDouble(2));
        for (NbtElement linkElem : nbt.getList("Strings", NbtType.COMPOUND)) {
            if (linkElem instanceof NbtCompound link) {
                if (link.contains("X")) {
                    int dx = link.getInt("X");
                    int dy = link.getInt("Y");
                    int dz = link.getInt("Z");
                    link.remove("X");
                    link.remove("Y");
                    link.remove("Z");
                    link.putInt("RelX", dx - sx);
                    link.putInt("RelY", dy - sy);
                    link.putInt("RelZ", dz - sz);
                }
            }
        }
        return nbt;
    }

    private boolean isNotChainKnot201(NbtCompound nbt) {
        // Not using the registry here to avoid breaking when the id changes
        return !nbt.getString("id").equals("tot:string_knot");
    }
}
