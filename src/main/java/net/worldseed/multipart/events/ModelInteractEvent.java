package net.worldseed.multipart.events;

import net.worldseed.multipart.model_bones.BoneEntity;
import net.worldseed.multipart.GenericModel;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelInteractEvent extends ModelEvent {
    private final GenericModel model;
    private final Player interactor;
    private final BoneEntity interactedBone;
    private final EquipmentSlot hand;

    /*public ModelInteractEvent(@NotNull EmoteModel model, PlayerInteractEntityEvent event) {
        this(model, event, null);
    }*/
    
    public ModelInteractEvent(@NotNull GenericModel model, PlayerInteractEntityEvent event, @Nullable BoneEntity interactedBone) {
        this.model = model;
        this.hand = event.getHand();
        this.interactor = event.getPlayer();
        this.interactedBone = interactedBone;
    }

    @Override
    public @NotNull GenericModel model() {
        return model;
    }

    public @NotNull EquipmentSlot getHand() {
        return hand;
    }

    public @NotNull Player getInteracted() { // This should probably be getInteractor() or getPlayer() but I left this untouched so code doesn't break
        return interactor;
    }
    
    public @Nullable BoneEntity getBone() {
        return interactedBone;
    }
}

