package xyz.nucleoid.creator_tools.mixin;

import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(DimensionDataStorage.class)
public interface DimensionDataStorageAccess {
    @Invoker
    Path callGetDataFile(String string);

}
