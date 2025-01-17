package mcjty.lib.multipart;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.model.IModelConfiguration;
import io.github.fabricators_of_create.porting_lib.model.IModelGeometry;
import io.github.fabricators_of_create.porting_lib.model.IModelLoader;
import io.github.fabricators_of_create.porting_lib.model.ModelLoaderRegistry;
import mcjty.lib.McJtyLib;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;

public class MultipartModelLoader implements IModelLoader<MultipartModelLoader.MultipartModelGeometry> {

    public static void register() {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(McJtyLib.MODID, "multipartloader"), new MultipartModelLoader());
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {

    }

    @Nonnull
    @Override
    public MultipartModelGeometry read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents) {
        return new MultipartModelGeometry();
    }

    public static class MultipartModelGeometry implements IModelGeometry<MultipartModelGeometry> {

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new MultipartBakedModel();
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return Collections.emptyList();
        }
    }
}
