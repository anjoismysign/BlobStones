package us.mytheria.blobstones;

import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.blobstones.director.StonesManagerDirector;

public final class BlobStones extends BlobPlugin {
    private StonesManagerDirector director;

    @Override
    public void onEnable() {
        this.director = new StonesManagerDirector(this);
    }

    @Override
    public void onDisable() {
        this.director.unload();
        unregisterFromBlobLib();
    }

    @Override
    public StonesManagerDirector getManagerDirector() {
        return this.director;
    }
}
