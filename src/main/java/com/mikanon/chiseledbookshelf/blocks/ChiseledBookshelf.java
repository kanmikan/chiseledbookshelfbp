package com.mikanon.chiseledbookshelf.blocks;

import com.mikanon.chiseledbookshelf.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ChiseledBookshelf extends BlockContainer {

    private String[] textures = new String[]{
            "chiseled_bookshelf_empty",
            "chiseled_bookshelf_top",
            "chiseled_bookshelf_side"
    };

    private IIcon textureEmpty, textureTop, textureSide;

    public ChiseledBookshelf() {
        super(Material.wood);
        this.setHardness(1.5F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityChiseledBookshelf();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityChiseledBookshelf)) return false;
        TileEntityChiseledBookshelf bookshelf = (TileEntityChiseledBookshelf) tile;

        int meta = world.getBlockMetadata(x, y, z) % 4;
        switch (meta) {
            case 0: //North
                break;
            case 1: //East
                hitX = hitZ;
                break;
            case 2: //South
                hitX = 1 - hitX;
                break;
            case 3: //West
                hitX = 1 - hitZ;
                break;
        }

        boolean clickedFront = false;
        switch (meta) {
            case 0: clickedFront = (side == 2); break;
            case 1: clickedFront = (side == 5); break;
            case 2: clickedFront = (side == 3); break;
            case 3: clickedFront = (side == 4); break;
        }
        if (!clickedFront) return false;

        //slots
        int col = 2 - (int) (hitX * 3);
        int row = 1 - (int) (hitY * 2);

        if (col < 0) col = 0;
        if (col > 2) col = 2;
        if (row < 0) row = 0;
        if (row > 1) row = 1;
        int slot = row * 3 + col;

        //held item
        ItemStack held = player.getCurrentEquippedItem();
        if (bookshelf.hasBookInSlot(slot)) {
            ItemStack book = bookshelf.getBookInSlot(slot);
            bookshelf.setBookInSlot(slot, null);
            bookshelf.lastInteractedSlot = slot;
            ItemStack copy = book.copy();

            if (!world.isRemote) {
                ItemStack result = copy.copy();
                boolean inserted = player.inventory.addItemStackToInventory(result);

                if (!inserted || result.stackSize > 0) {
                    EntityItem entityItem = new EntityItem(world, x + 0.5, y + 1, z + 0.5, result);
                    world.spawnEntityInWorld(entityItem);
                }

                world.markBlockForUpdate(x, y, z);
                world.notifyBlocksOfNeighborChange(x, y, z, this);

                //forzar sync
                player.inventoryContainer.detectAndSendChanges();
            }
            return true;

        } else if (held != null && TileEntityChiseledBookshelf.isBookItem(held)) {
            if (!world.isRemote) {
                bookshelf.setBookInSlot(slot, held.splitStack(1).copy());
                bookshelf.lastInteractedSlot = slot;

                world.markBlockForUpdate(x, y, z);
                world.notifyBlocksOfNeighborChange(x, y, z, this);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int direction = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChiseledBookshelf) {
            ((TileEntityChiseledBookshelf) te).dropInventory(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 1 || side == 0) { return this.textureTop; }

        int front = 2;
        switch (meta) {
            case 0: front = 2; break;
            case 1: front = 5; break;
            case 2: front = 3; break;
            case 3: front = 4; break;
        }
        return (side == front) ? this.textureEmpty : this.textureSide;
    }

    @Override
    public void registerBlockIcons(IIconRegister registry) {
        this.textureEmpty = registry.registerIcon(Main.MODID + ":" + textures[0]);
        this.textureTop = registry.registerIcon(Main.MODID + ":" + textures[1]);
        this.textureSide = registry.registerIcon(Main.MODID + ":" + textures[2]);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    //no se si es este o isProvidingStrongPower
    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        //el común devuelve cuantos libros hay
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChiseledBookshelf) {
            TileEntityChiseledBookshelf shelf = (TileEntityChiseledBookshelf) te;
            return shelf.getSlotCount(); // de 0 a 6
        }
        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        //el comparador devuelve el último slot usado
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityChiseledBookshelf) {
            int count = ((TileEntityChiseledBookshelf) tile).getSlotCount();
            int slot = ((TileEntityChiseledBookshelf) tile).lastInteractedSlot;
            return (count > 0) ? slot+1 : 0;
        }
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

}
