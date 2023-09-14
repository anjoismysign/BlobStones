package us.mytheria.blobstones;

import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.PluginUpdater;
import us.mytheria.bloblib.entities.proxy.BlobProxifier;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;
import us.mytheria.blobstones.director.StonesManagerDirector;

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
