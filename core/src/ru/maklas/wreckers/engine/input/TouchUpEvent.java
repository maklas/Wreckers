package ru.maklas.wreckers.engine.input;

import com.badlogic.gdx.math.Vector2;
import ru.maklas.wreckers.engine.other.Event;

/** Палец был поднят. **/
@Deprecated
public class TouchUpEvent implements Event {

	float x;
	float y;
	int finger;
	int button;

	public TouchUpEvent(Vector2 point, int finger, int button) {
		this.x = point.x;
		this.y = point.y;
		this.finger = finger;
		this.button = button;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getFinger() {
		return finger;
	}

	public int getButton() {
		return button;
	}
}
