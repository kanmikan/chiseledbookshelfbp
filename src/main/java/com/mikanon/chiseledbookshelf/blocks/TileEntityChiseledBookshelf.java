package com.mikanon.chiseledbookshelf.blocks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityChiseledBookshelf extends TileEntity implements ISidedInventory {

    public int lastInteractedSlot = -1;
    private final ItemStack[] bookSlots = new ItemStack[6];

    public ItemStack getBookInSlot(int index) {
        if (index < 0 || index >= bookSlots.length) return null;
        return bookSlots[index];
    }

    public void setBookInSlot(int index, ItemStack stack) {
        if (index < 0 || index >= bookSlots.length) return;
        bookSlots[index] = stack;
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
        int filled = 0;
        for (int i = 0; i < 6; i++) {
            if (hasBookInSlot(i)) filled++;
        }
        return filled;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        for (int i = 0; i < bookSlots.length; i++) { bookSlots[i] = null; }

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

    @Override
    public int getSizeInventory() {
        return bookSlots.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return getBookInSlot(i);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            //this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack != null) {
            ItemStack removedStack;
            if (stack.stackSize <= count) {
                removedStack = stack;
                setInventorySlotContents(index, null);
            } else {
                removedStack = stack.splitStack(count);
                if (stack.stackSize == 0) {
                    setInventorySlotContents(index, null);
                }
            }
            markDirty();
            return removedStack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack stack = getStackInSlot(index);
        if (stack != null) {
            setInventorySlotContents(index, null);
        }
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= bookSlots.length) {
            return;
        }
        ItemStack prevStack = bookSlots[index];
        bookSlots[index] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }

        if (!ItemStack.areItemStacksEqual(prevStack, stack)) {
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.chiseledbookshelf"; //no gui..
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return isBookItem(stack) && getStackInSlot(index) == null;
    }

    public static boolean isBookItem(ItemStack stack) {
        //todo modded wildcard?
        return stack.getItem() == Items.book || stack.getItem() == Items.enchanted_book || stack.getItem() == Items.writable_book || stack.getItem() == Items.written_book;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return this.isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        return this.hasBookInSlot(index);
    }

}
