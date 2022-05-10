package mcjty.lib.varia;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class BlockTools {

    // Write a blockstate to a string
    public static String writeBlockState(BlockState tag) {
        StringBuilder builder = new StringBuilder(Registry.BLOCK.getKey(tag.getBlock()).toString());
        ImmutableMap<Property<?>, Comparable<?>> properties = tag.getValues();
        if (!properties.isEmpty()) {
            char c = '@';

            for(Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
                Property<?> property = entry.getKey();
                builder.append(c); c = ',';
                builder.append(property.getName());
                builder.append('=');
                builder.append(getName(property, entry.getValue()));
            }
        }

        return builder.toString();
    }

    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> cmp) {
        return property.getName((T)cmp);
    }

    public static BlockState readBlockState(String s) {
        String blockName;
        String properties;
        if (s.contains("@")) {
            String[] split = StringUtils.split(s, '@');
            blockName = split[0];
            properties = split[1];
        } else {
            blockName = s;
            properties = null;
        }
        Block block = Registry.BLOCK.get(new ResourceLocation(blockName));
        if (block == null) {
            throw new RuntimeException("Cannot find block '" + blockName + "'!");
        }
        BlockState state = block.defaultBlockState();
        if (properties != null) {
            StateDefinition<Block, BlockState> statecontainer = state.getBlock().getStateDefinition();
            String[] split = StringUtils.split(properties, ',');
            for (String pv : split) {
                String[] sp = StringUtils.split(pv, '=');
                Property<?> property = statecontainer.getProperty(sp[0]);
                if (property != null) {
                    state = setValueHelper(state, property, sp[1]);
                }
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setValueHelper(BlockState state, Property<T> property, String value) {
        Optional<T> optional = property.getValue(value);
        if (optional.isPresent()) {
            return state.setValue(property, optional.get());
        } else {
            return state;
        }
    }
}
