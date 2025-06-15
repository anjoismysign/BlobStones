package io.github.anjoismysign.blobstones.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.blobstones.BlobStones;

public class StonesManager extends GenericManager<BlobStones, StonesManagerDirector> {
    public StonesManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
    }
}
