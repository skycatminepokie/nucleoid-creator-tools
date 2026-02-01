package xyz.nucleoid.creator_tools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RegionVisibilityFilterComponent(List<String> regions) {
    public static final Codec<RegionVisibilityFilterComponent> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.listOf().fieldOf("regions").forGetter(RegionVisibilityFilterComponent::regions)
        ).apply(instance, RegionVisibilityFilterComponent::new);
    });

    public static final StreamCodec<ByteBuf, RegionVisibilityFilterComponent> PACKET_CODEC = ByteBufCodecs.STRING_UTF8
            .apply(ByteBufCodecs.list())
            .map(RegionVisibilityFilterComponent::new, RegionVisibilityFilterComponent::regions);
}
