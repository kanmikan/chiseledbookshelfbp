package com.mikanon.chiseledbookshelf;

import com.mikanon.chiseledbookshelf.blocks.ChiseledBookshelf;
import com.mikanon.chiseledbookshelf.blocks.TileEntityChiseledBookshelf;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {

    public static final String MODID = "chiseledbookshelf";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.mikanon.chiseledbookshelf.ClientProxy", serverSide = "com.mikanon.chiseledbookshelf.CommonProxy")
    public static CommonProxy proxy;

    public static Block blockChiseledBookshelf;

    @EventHandler
    public void PreInit(FMLPreInitializationEvent event){
        blockChiseledBookshelf = new ChiseledBookshelf().setBlockName("chiseled_bookshelf").setCreativeTab(CreativeTabs.tabDecorations);
        GameRegistry.registerBlock(blockChiseledBookshelf, blockChiseledBookshelf.getUnlocalizedName());
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        GameRegistry.registerTileEntity(TileEntityChiseledBookshelf.class, Main.MODID + "te_chiseled_bookshelf");
        proxy.init();

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(blockChiseledBookshelf),
                "PPP",
                        "SSS",
                        "PPP",
                        'P', "plankWood",
                        'S', "slabWood"
        ));
    }
    
    @EventHandler
    public void PostInit(FMLPostInitializationEvent event){
		
    }
    
}
