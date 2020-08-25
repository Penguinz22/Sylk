package dev.penguinz.Sylk.assets;

import dev.penguinz.Sylk.Application;
import dev.penguinz.Sylk.assets.loaders.AssetLoader;
import dev.penguinz.Sylk.assets.loaders.TextureLoader;
import dev.penguinz.Sylk.assets.options.AssetOptions;
import dev.penguinz.Sylk.util.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetManager implements Disposable {

    private final ExecutorService executorService;

    private final Stack<LoadingTask<?>> tasks = new Stack<>();
    private final HashMap<String, Object> assets = new HashMap<>();

    private static final HashMap<Class<?>, AssetLoader<?, ?>> loaders = new HashMap<>();
    private static final HashMap<String, Class<?>> fileTypes = new HashMap<>();

    static {
        loaders.put(Texture.class, new TextureLoader());
        fileTypes.put("png", Texture.class);
    }

    public AssetManager() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public boolean update() {
        if(isFinished())
            return true;
        return updateTask() && isFinished();
    }

    private boolean updateTask() {
        LoadingTask<?> task = tasks.peek();

        if(task.update()) {
            tasks.pop();
            assets.put(task.getDescriptor().path, task.getAsset());
            return true;
        }

        return false;
    }

    public <T> void loadAsset(String file) {
        loadAsset(file, getClassType(file.substring(file.length()-3)), null);
    }

    public <T> void loadAsset(String file, AssetOptions<T> options) {
        loadAsset(file, getClassType(file.substring(file.length()-3)), options);
    }

    public <T> void loadAsset(String file, Class<T> classType) {
        loadAsset(file, classType, null);
    }

    public <T> void loadAsset(String file, Class<T> classType, AssetOptions<T> options) {
        tasks.push(new LoadingTask<>(new AssetDescriptor<>(file, options, classType), getLoader(classType), this.executorService));
    }

    private <T> AssetLoader<T, AssetOptions<T>> getLoader(Class<T> classType) {
        @SuppressWarnings("unchecked")
        AssetLoader<T, AssetOptions<T>> loader = (AssetLoader<T, AssetOptions<T>>) loaders.get(classType);
        return loader;
    }

    public <T> T getAsset(String file) {
        if(isLoaded(file)) {
            @SuppressWarnings("unchecked")
            T asset = (T) assets.get(file);
            if(asset == null)
                Application.getInstance().getLogger().logError("Asset was loaded improperly: "+file);
            return asset;
        }
        Application.getInstance().getLogger().logError("Tried to retrieve unloaded asset: "+file);
        return null;
    }

    private <T> Class<T> getClassType(String fileType) {
        if(!fileTypes.containsKey(fileType))
            throw new RuntimeException("Could not determine class type for file extension: "+fileType);
        @SuppressWarnings("unchecked")
        Class<T> classType = (Class<T>) fileTypes.get(fileType);
        return classType;
    }

    public boolean isLoaded(String file) {
        return assets.containsKey(file);
    }

    public boolean isFinished() {
        return tasks.size() == 0;
    }

    public void finishLoading() {
        while(!isFinished())
            update();
    }

    @Override
    public void dispose() {
        executorService.shutdown();
    }
}