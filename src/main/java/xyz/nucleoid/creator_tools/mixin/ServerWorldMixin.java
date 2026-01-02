package xyz.nucleoid.creator_tools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.creator_tools.workspace.MapWorkspaceManager;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Final
    @Shadow
    private MinecraftServer server;
    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void addPlayer(ServerPlayerEntity player, CallbackInfo ci) {
        MapWorkspaceManager.get(server).onPlayerAddToWorld(player, (ServerWorld) (Object) this);
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void removePlayer(ServerPlayerEntity player, Entity.RemovalReason reason, CallbackInfo ci) {
        MapWorkspaceManager.get(server).onPlayerRemoveFromWorld(player, (ServerWorld) (Object) this);
    }
}
