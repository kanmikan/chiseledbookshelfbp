package com.mikanon.chiseledbookshelf.blocks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityChiseledBookshelf extends TileEntity {

    public int lastInteractedSlot = -1;
    private final ItemStack[] bookSlots = new ItemStack[6];

    public ItemStack getBookInSlot(int index) {
        if (index < 0 || index >= bookSlots.length) return null;
        return bookSlots[index];
    }

    public void setBookInSlot(int index, ItemStack stack) {
        if (index < 0 || index >= bookSlots.length) return;
        bookSlots[index] = stack;
        markDirty();
    }

    public boolean hasBookInSlot(int index) {
        return getBookInSlot(index) != null;
    }

    public void dropInventory(World world, int x, int y, int z) {
        for (int i = 0; i < 6; i++) {
            ItemStack stack = getBookInSlot(i);
            if (stack != null) {
                float offsetX = world.rand.nextFloat() * 0.8F + 0.1F;
                float offsetY = world.rand.nextFloat() * 0.8F + 0.1F;
                float offsetZ = world.rand.nextFloat() * 0.8F + 0.1F;

                EntityItem entity = new EntityItem(world, x + offsetX, y + offsetY, z + offsetZ, stack.copy());

                if (stack.hasTagCompound()) {
                    NBTTagCompound compound = (NBTTagCompound) stack.getTagCompound().copy();
                    entity.getEntityItem().setTagCompound(compound);
                }

                float motionFactor = 0.05F;
                entity.motionX = world.rand.nextGaussian() * motionFactor;
                entity.motionY = world.rand.nextGaussian() * motionFactor + 0.2F;
                entity.motionZ = world.rand.nextGaussian() * motionFactor;

                world.spawnEntityInWorld(entity);
                setBookInSlot(i, null);
            }
        }
    }

    public int getSlotCount() {
        return bookSlots.length;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Books", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound slotTag = list.getCompoundTagAt(i);
            int slot = slotTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < bookSlots.length) {
                bookSlots[slot] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < bookSlots.length; i++) {
            if (bookSlots[i] != null) {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte) i);
                bookSlots[i].writeToNBT(slotTag);
                list.appendTag(slotTag);
            }
        }
        tag.setTag("Books", list);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeToNBT(syncData);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

}
