package xyz.nucleoid.creator_tools.mixin;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.creator_tools.CreatorTools;
import xyz.nucleoid.creator_tools.workspace.MapWorkspaceManager;
import xyz.nucleoid.creator_tools.workspace.ReturnPosition;
import xyz.nucleoid.creator_tools.workspace.WorkspaceTraveler;
import xyz.nucleoid.creator_tools.workspace.editor.WorkspaceNetworking;

import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements WorkspaceTraveler {
    @Shadow
    @Final
    public MinecraftServer server;

    private ReturnPosition leaveReturn;
    private final Map<RegistryKey<World>, ReturnPosition> workspaceReturns = new Reference2ObjectOpenHashMap<>();

    private int creatorToolsProtocolVersion = WorkspaceNetworking.NO_PROTOCOL_VERSION;

    private ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "writeCustomData", at = @At("RETURN"))
    private void writeData(WriteView view, CallbackInfo ci) {
        var creatorTools = view.get(CreatorTools.ID);

        creatorTools.put("workspace_return", ReturnPosition.MAP_CODEC, this.workspaceReturns);
        creatorTools.putNullable("leave_return", ReturnPosition.CODEC, this.leaveReturn);
    }

    @Inject(method = "readCustomData", at = @At("RETURN"))
    private void readData(ReadView view, CallbackInfo ci) {
        var creatorTools = view.getReadView(CreatorTools.ID);

        this.workspaceReturns.clear();

        creatorTools.read("workspace_return", ReturnPosition.MAP_CODEC).ifPresent(this.workspaceReturns::putAll);

        this.leaveReturn = creatorTools.read("leave_return", ReturnPosition.CODEC).orElse(null);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyFrom(ServerPlayerEntity from, boolean alive, CallbackInfo ci) {
        var fromTraveler = (ServerPlayerEntityMixin) (Object) from;
        this.leaveReturn = fromTraveler.leaveReturn;
        this.workspaceReturns.clear();
        this.workspaceReturns.putAll(fromTraveler.workspaceReturns);
        this.creatorToolsProtocolVersion = fromTraveler.creatorToolsProtocolVersion;
    }

    @Inject(method = "teleportTo", at = @At("HEAD"))
    private void onTeleport(TeleportTarget target, CallbackInfoReturnable<ServerPlayerEntity> ci) {
        this.onDimensionChange(target.world());
    }

    private void onDimensionChange(ServerWorld targetWorld) {
        var sourceDimension = this.getWorld().getRegistryKey();
        var targetDimension = targetWorld.getRegistryKey();

        var workspaceManager = MapWorkspaceManager.get(this.server);
        if (workspaceManager.isWorkspace(sourceDimension)) {
            this.workspaceReturns.put(sourceDimension, ReturnPosition.capture(this));
        } else if (workspaceManager.isWorkspace(targetDimension)) {
            this.leaveReturn = ReturnPosition.capture(this);
        }
    }

    @Nullable
    @Override
    public ReturnPosition getReturnFor(RegistryKey<World> dimension) {
        return this.workspaceReturns.get(dimension);
    }

    @Nullable
    @Override
    public ReturnPosition getLeaveReturn() {
        return this.leaveReturn;
    }

    @Override
    public int getCreatorToolsProtocolVersion() {
        return this.creatorToolsProtocolVersion;
    }

    @Override
    public void setCreatorToolsProtocolVersion(int protocolVersion) {
        this.creatorToolsProtocolVersion = protocolVersion;
    }
}
