package us.mytheria.blobstones.director;

import us.mytheria.bloblib.entities.GenericManager;
import us.mytheria.blobstones.BlobStones;

public class StonesManager extends GenericManager<BlobStones, StonesManagerDirector> {
    public StonesManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
    }
}
