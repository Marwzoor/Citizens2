package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_7_R3.EntityWolf;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftWolf;
import org.bukkit.entity.Wolf;
import org.bukkit.util.Vector;

public class WolfController extends MobEntityController {
    public WolfController() {
        super(EntityWolfNPC.class);
    }

    @Override
    public Wolf getBukkitEntity() {
        return (Wolf) super.getBukkitEntity();
    }

    public static class EntityWolfNPC extends EntityWolf implements NPCHolder {
        private final CitizensNPC npc;

        public EntityWolfNPC(World world) {
            this(world, null);
        }

        public EntityWolfNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void a(boolean flag) {
            float oldw = width;
            float oldl = length;
            super.a(flag);
            if (oldw != width || oldl != length) {
                this.setPosition(locX - 0.01, locY, locZ - 0.01);
                this.setPosition(locX + 0.01, locY, locZ + 0.01);
            }
        }

        @Override
        protected void a(double d0, boolean flag) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag);
            }
        }

        @Override
        protected String aS() {
            return npc == null ? super.aS() : npc.data().get(NPC.HURT_SOUND_METADATA, super.aS());
        }

        @Override
        protected String aT() {
            return npc == null ? super.aT() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.aT());
        }

        @Override
        protected void b(float f) {
            if (npc == null || !npc.isFlyable()) {
                super.b(f);
            }
        }

        @Override
        public boolean bN() {
            if (npc == null)
                return super.bN();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.bN();
            if (super.bN()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void bm() {
            super.bm();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_7_R3.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new WolfNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean h_() {
            if (npc == null || !npc.isFlyable()) {
                return super.h_();
            } else {
                return false;
            }
        }

        @Override
        protected String t() {
            return npc == null ? super.aS() : npc.data().get(NPC.AMBIENT_SOUND_METADATA, super.t());
        }

        @Override
        protected void w() {
            if (npc == null) {
                super.w();
            }
        }
    }

    public static class WolfNPC extends CraftWolf implements NPCHolder {
        private final CitizensNPC npc;

        public WolfNPC(EntityWolfNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void setSitting(boolean sitting) {
            getHandle().setSitting(sitting);
        }

    }
}