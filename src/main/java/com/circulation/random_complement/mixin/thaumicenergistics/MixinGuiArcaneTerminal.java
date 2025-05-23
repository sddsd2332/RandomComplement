package com.circulation.random_complement.mixin.thaumicenergistics;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.circulation.random_complement.client.handler.InputHandler;
import com.circulation.random_complement.common.handler.MEHandler;
import com.circulation.random_complement.common.interfaces.SpecialLogic;
import com.circulation.random_complement.common.util.SimpleItem;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.client.gui.part.GuiAbstractTerminal;
import thaumicenergistics.client.gui.part.GuiArcaneTerminal;
import thaumicenergistics.container.ContainerBaseTerminal;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotME;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.circulation.random_complement.RCConfig.AE2;

@Mixin(GuiArcaneTerminal.class)
public abstract class MixinGuiArcaneTerminal extends GuiAbstractTerminal<IAEItemStack, IItemStorageChannel> implements SpecialLogic {

    public MixinGuiArcaneTerminal(ContainerBaseTerminal container) {
        super(container);
    }

    @Unique
    private final int randomComplement$textureIndex = AE2.craftingSlotTextureIndex;

    @Unique
    public Set<SimpleItem> randomComplement$craftableCache = new HashSet<>();

    @Unique
    public Set<SimpleItem> randomComplement$cpuCache = new HashSet<>();

    @Unique
    private Set<SimpleItem> randomComplement$mergedCache = new HashSet<>();

    @Unique
    private Set<IAEItemStack> randomComplement$getStorage() {
        IItemList<IAEItemStack> all = ((AccessorMERepo<IAEItemStack>)this.getRepo()).getList();
        return all == null ? Collections.emptySet() : StreamSupport.stream(all.spliterator(), false).collect(Collectors.toSet());
    }

    @Unique
    private Set<SimpleItem> randomComplement$getCraftables() {
        if (randomComplement$cpuCache.isEmpty()) {
            randomComplement$cpuCache = this.randomComplement$getStorage()
                    .stream()
                    .filter(IAEStack::isCraftable)
                    .map(itemStack -> SimpleItem.getInstance(itemStack.getDefinition()))
                    .collect(Collectors.toSet());
        }

        return randomComplement$cpuCache;
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    private void drawPin(float f, int x, int y, CallbackInfo ci){
        var items = this.r$getList();
        if (!items.isEmpty()) {
            List<SlotME<?>> slots = new ArrayList<>();
            for (Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotME<?> slotME) {
                    if (items.contains(SimpleItem.getInstance(slotME.getStack()))) {
                        slots.add(slotME);
                    } else {
                        break;
                    }
                }
            }

            final int cycle = (slots.size() + 8) / 9;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            MEHandler.randomComplement$bindTexture(this.mc,randomComplement$textureIndex);
            for (int i = 0; i < cycle; i++) {
                int amount = Math.min(slots.size() - i * 9,9);
                int yOffset = (randomComplement$textureIndex < 3 || randomComplement$textureIndex == 6)
                        ? InputHandler.counter * 18
                        : (randomComplement$textureIndex - 3) * 18;

                this.drawTexturedModalRect(
                        this.getGuiLeft() + 8,
                        this.getGuiTop() + 17 + 18 * i,
                        0,
                        yOffset,
                        18 * amount, 18
                );
            }
        }
    }

    @Unique
    @Override
    public void drawSlot(Slot slot){
        super.drawSlot(slot);
        if (slot instanceof SlotME<?> slotME) {
            var aeStack = slotME.getAEStack();
            if (aeStack != null && aeStack.isCraftable()) {
                MEHandler.drawPlus(slot.xPos, slot.yPos);
            }
        }
        if (slot instanceof SlotGhost slotG) {
            var aeStack = slotG.getStack();
            if (!aeStack.isEmpty()) {
                if (randomComplement$getCraftables().contains(SimpleItem.getInstance(aeStack))) {
                    MEHandler.drawPlus(slotG);
                }
            }
        }
    }

    @Unique
    @Override
    public Set<SimpleItem> r$getList() {
        if (randomComplement$mergedCache.isEmpty()){
            randomComplement$mergedCache.addAll(MEHandler.craftableCacheS);
            randomComplement$mergedCache.addAll(randomComplement$craftableCache);
            MEHandler.craftableCacheS.clear();
        }
        return randomComplement$mergedCache;
    }

    @Unique
    @Override
    public void r$setList(Set<SimpleItem> list) {
        randomComplement$craftableCache.clear();
        randomComplement$craftableCache.addAll(list);
    }

    @Unique
    @Override
    public void r$addList(SimpleItem item) {
        randomComplement$craftableCache.add(item);
    }

    @Unique
    @Override
    public void r$addAllList(Set<SimpleItem> list) {
        randomComplement$craftableCache.addAll(list);
    }

    @Mixin(value = MERepo.class,remap = false)
    public interface AccessorMERepo<T extends IAEStack<T>> {

        @Accessor
        IItemList<T> getList();

    }

}
