package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.Location;
import org.bukkit.World;

class SingleAccountLocation extends AccountLocation {

    SingleAccountLocation(World world, BlockVector3D v1) {
        super(world, v1);
    }

    @Override
    public Location getAverageLocation() {
        return getMinimumLocation().add(0.5, 0.0, 0.5);
    }

}
