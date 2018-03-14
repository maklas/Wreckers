package ru.maklas.wreckers.network.events;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import org.jetbrains.annotations.NotNull;
import ru.maklas.wreckers.libs.Copyable;

/**
 * Created by MaklasEventMaker on 14.03.2018
 * Синхронизует тела. Используется Box2d скейлинг. Угол в радианах
 */
public class BodySyncEvent implements Copyable {
    
    int entityId;
    float x;
    float y;
    float velX;
    float velY;
    float angle;
    
    public BodySyncEvent (int entityId, float x, float y, float velX, float velY, float angle) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.angle = angle;
    }
    
    public BodySyncEvent () {
        
    }
    
    public BodySyncEvent setAndRet(int entityId, float x, float y, float velX, float velY, float angle) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.angle = angle;
        return this;
    }
    
    public int getId() {
        return this.entityId;
    }
    
    public float getX() {
        return this.x;
    }
    
    public float getY() {
        return this.y;
    }
    
    public float getVelX() {
        return this.velX;
    }
    
    public float getVelY() {
        return this.velY;
    }

    public static BodySyncEvent fromBody(int id, @NotNull Body body){
        Vector2 position = body.getPosition();
        Vector2 linearVelocity = body.getLinearVelocity();
        return new BodySyncEvent(id, position.x, position.y, linearVelocity.x, linearVelocity.y, body.getAngle());
    }

    public void hardApply(Body body) {
        body.setTransform(x, y, angle);
        body.setLinearVelocity(velX, velY);
    }


    @Override
    public String toString() {
        return "BodySyncEvent{" +
        "entityId=" + entityId +
        ", x=" + x +
        ", y=" + y +
        ", velX=" + velX +
        ", velY=" + velY +
        ", angle=" + angle +
        '}';
    }

    @Override
    public Object copy() {
        return new BodySyncEvent(entityId, x, y, velX, velY, angle);
    }
}
