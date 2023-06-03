package com.nurdoidz.mites.item;

import com.nurdoidz.mites.entity.Mite;
import com.nurdoidz.mites.entity.Mite.Enthrall;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.Item;

public class InspectorTool extends Item {

    private final Set<Enthrall> offspring = new HashSet<>();
    private Enthrall father = null;
    private Enthrall mother = null;

    public InspectorTool(Properties pProperties) {

        super(pProperties);
    }

    public NextStatus next(Enthrall pEnthrall) {

        if (father == null) {
            this.father = pEnthrall;
            this.clearOffspring();
            return NextStatus.PARENT;
        } else {
            this.mother = pEnthrall;
            this.setOffspring();
            this.father = null;
            this.mother = null;
            return NextStatus.OFFSPRING;
        }
    }

    private void clearOffspring() {

        this.offspring.clear();
    }

    private void setOffspring() {

        this.offspring.addAll(Mite.getEnthrallCandidates(this.father, this.mother));
    }

    public Set<Enthrall> getOffspring() {

        return this.offspring;
    }

    public enum NextStatus {
        PARENT,
        OFFSPRING
    }
}
