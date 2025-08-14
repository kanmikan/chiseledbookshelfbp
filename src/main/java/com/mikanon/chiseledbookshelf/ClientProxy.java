package com.mikanon.chiseledbookshelf;

import com.mikanon.chiseledbookshelf.blocks.TileEntityChiseledBookshelf;
import com.mikanon.chiseledbookshelf.blocks.TileEntityChiseledBookshelfRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChiseledBookshelf.class, new TileEntityChiseledBookshelfRenderer());
    }

}
