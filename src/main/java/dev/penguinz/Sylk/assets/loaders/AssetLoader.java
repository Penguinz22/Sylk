package dev.penguinz.Sylk.assets.loaders;

import dev.penguinz.Sylk.assets.options.AssetOptions;

public interface AssetLoader<T, P extends AssetOptions<T>> {

    void loadAsync(String path, P options);

    T loadSync();

    AssetLoader<T, P> copy();

}
