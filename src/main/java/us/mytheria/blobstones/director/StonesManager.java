package us.mytheria.blobstones.director;

import us.mytheria.bloblib.managers.Manager;
import us.mytheria.blobstones.BlobStones;

public class StonesManager extends Manager {
    public StonesManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
    }

    @Override
    public StonesManagerDirector getManagerDirector() {
        return (StonesManagerDirector) super.getManagerDirector();
    }

    @Override
    public BlobStones getPlugin() {
        return (BlobStones) super.getPlugin();
    }
}
