package io.github.anjoismysign.blobstones;

import io.github.anjoismysign.bloblib.entities.PluginUpdater;
import io.github.anjoismysign.bloblib.entities.proxy.BlobProxifier;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.blobstones.director.StonesManagerDirector;
import org.jetbrains.annotations.NotNull;

public final class BlobStones extends BlobPlugin {
    private StonesManagerDirector director;
    private IManagerDirector proxy;
    private PluginUpdater updater;

    @Override
    public void onEnable() {
        this.director = new StonesManagerDirector(this);
        this.proxy = BlobProxifier.PROXY(director);
        this.updater = generateGitHubUpdater("anjoismysign", "BlobStones");
    }

    @Override
    public void onDisable() {
        this.director.unload();
        unregisterFromBlobLib();
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    @Override
    @NotNull
    public PluginUpdater getPluginUpdater() {
        return updater;
    }
}
