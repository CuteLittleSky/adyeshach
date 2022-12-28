package ink.ptms.adyeshach.impl.entity.controller;

import ink.ptms.adyeshach.core.entity.EntityInstance;
import ink.ptms.adyeshach.core.entity.controller.Controller;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import taboolib.library.xseries.XMaterial;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer
 *
 * @author mojang
 */
public class ControllerLookAtPlayer extends Controller {

    protected final EntityInstance entity;
    protected double lookDistance;
    protected int lookTime;
    protected double probability;
    protected boolean onlyHorizontal;
    protected int baseLookTime;

    @Nullable
    protected LivingEntity lookAt;

    public ControllerLookAtPlayer(EntityInstance entity) {
        this(entity, 8f, 0.01F);
    }

    public ControllerLookAtPlayer(EntityInstance entity, double lookDistance) {
        this(entity, lookDistance, 0.01F);
    }

    public ControllerLookAtPlayer(EntityInstance entity, double lookDistance, double probability) {
        this(entity, lookDistance, probability, false, 40);
    }

    public ControllerLookAtPlayer(EntityInstance entity, double lookDistance, double probability, boolean onlyHorizontal) {
        this(entity, lookDistance, probability, onlyHorizontal, 40);
    }

    public ControllerLookAtPlayer(EntityInstance entity, double lookDistance, double probability, boolean onlyHorizontal, int baseLookTime) {
        this.entity = entity;
        this.lookDistance = lookDistance;
        this.probability = probability;
        this.onlyHorizontal = onlyHorizontal;
        this.baseLookTime = baseLookTime;
    }

    @NotNull
    @Override
    public String id() {
        return "LOOK_AT_PLAYER";
    }

    @NotNull
    @Override
    public String key() {
        return "LOOK";
    }

    @Override
    public int priority() {
        return 8;
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.random().nextFloat() >= this.probability) {
            return false;
        } else {
            this.entity.getWorld().getPlayers().stream()
                    .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= lookDistance * lookDistance)
                    .min((o1, o2) -> {
                        double d1 = o1.getLocation().distanceSquared(entity.getLocation());
                        double d2 = o2.getLocation().distanceSquared(entity.getLocation());
                        return Double.compare(d1, d2);
                    }).ifPresent(player -> {
                        this.lookAt = player;
                    });
            return this.lookAt != null;
        }
    }

    @Override
    public boolean continueExecute() {
        if (this.lookAt == null || !this.lookAt.isValid()) {
            return false;
        } else if (this.entity.getWorld() != this.lookAt.getWorld() || this.entity.getEyeLocation().distanceSquared(this.lookAt.getLocation()) > (double) (this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(this.baseLookTime + this.entity.random().nextInt(this.baseLookTime));
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (this.lookAt != null && this.lookAt.isValid()) {
            double y = this.onlyHorizontal ? this.entity.getEyeLocation().getY() : this.lookAt.getEyeLocation().getY();
            this.entity.controllerLookAt(this.lookAt.getLocation().getX(), y, this.lookAt.getLocation().getZ());
            this.lookTime--;
        }
    }

    @Override
    public String toString() {
        return "LOOK_AT_PLAYER:" + lookDistance + "," + probability + "," + onlyHorizontal + "," + baseLookTime;
    }
}
