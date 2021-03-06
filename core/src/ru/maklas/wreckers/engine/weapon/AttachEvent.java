package ru.maklas.wreckers.engine.weapon;

import ru.maklas.mengine.Entity;
import ru.maklas.wreckers.engine.other.Event;

/** Внутридвижковый ивент об удачной смене состояния оружия (Прикрепилось / Открепилось) **/
public class AttachEvent implements Event {

	private Entity owner;
	private Entity attachable;
	private boolean attached;

	public AttachEvent(Entity owner, Entity attachable, boolean attached) {
		this.owner = owner;
		this.attachable = attachable;
		this.attached = attached;
	}

	public Entity getOwner() {
		return owner;
	}

	public Entity getAttachable() {
		return attachable;
	}

	public boolean isAttached() {
		return attached;
	}
}
