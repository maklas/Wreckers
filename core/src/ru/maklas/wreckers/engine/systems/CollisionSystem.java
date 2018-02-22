package ru.maklas.wreckers.engine.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.jetbrains.annotations.Nullable;
import ru.maklas.mengine.Engine;
import ru.maklas.mengine.Entity;
import ru.maklas.mengine.EntitySystem;
import ru.maklas.mengine.Subscription;
import ru.maklas.mengine.utils.Signal;
import ru.maklas.wreckers.assets.EntityType;
import ru.maklas.wreckers.assets.GameAssets;
import ru.maklas.wreckers.engine.Mappers;
import ru.maklas.wreckers.engine.components.PickUpComponent;
import ru.maklas.wreckers.engine.components.WeaponComponent;
import ru.maklas.wreckers.engine.events.CollisionEvent;
import ru.maklas.wreckers.engine.events.requests.DetachRequest;
import ru.maklas.wreckers.engine.events.requests.WeaponWreckerHitEvent;
import ru.maklas.wreckers.game.FixtureData;
import ru.maklas.wreckers.game.FixtureType;

import static ru.maklas.wreckers.assets.EntityType.*;

public class CollisionSystem extends EntitySystem{

    @Override
    public void onAddedToEngine(final Engine engine) {
        subscribe(new Subscription<CollisionEvent>(CollisionEvent.class) {
            @Override
            public void receive(Signal<CollisionEvent> signal, CollisionEvent e) {
                EntityType typeA = fromType(e.getA().type);
                EntityType typeB = fromType(e.getB().type);
                if (e.getImpulse().getCount() == 0){
                    return;
                }

                if (isPlayerOrOpponent(typeA) && isPlayerOrOpponent(typeB)){
                    handlePlayerToPlayer(e.getA(), typeA, e.getB(), typeB, e.getContact(), e.getImpulse());

                } else if (isWeapon(typeA) && isWeapon(typeB)){
                    handleWeaponToWeapon(e.getA(), typeA, e.getB(), typeB, e.getContact(), e.getImpulse());

                } else if (isPlayerOrOpponent(typeA) && isWeapon(typeB)){
                    handleWeaponToPlayer(e.getB(), typeB, e.getA(), typeA, e.getContact(), e.getImpulse(), false);

                } else if (isPlayerOrOpponent(typeB) && isWeapon(typeA)){
                    handleWeaponToPlayer(e.getA(), typeA, e.getB(), typeB, e.getContact(), e.getImpulse(), true);
                }
            }
        });
    }

    // ������� ������ ���������� �����
    private void handleWeaponToWeapon(final Entity weaponA, EntityType typeA, Entity weaponB, EntityType typeB, Contact contact, ContactImpulse impulse){
        float impulseForce = impulse.getNormalImpulses()[0];
        if (impulseForce > 100){
            getEngine().dispatchLater(new DetachRequest(null, DetachRequest.Type.TARGET_WEAPON, weaponA));
            getEngine().dispatchLater(new DetachRequest(null, DetachRequest.Type.TARGET_WEAPON, weaponB));
        }
    }

    // ������� ������ ���������� �����
    private void handleWeaponToPlayer(final Entity weapon, EntityType weaponType, final Entity player, EntityType playerType, Contact contact, ContactImpulse impulse, boolean weaponIsA){
        final float minimumImpulse = 100;
        float impulseForce = impulse.getNormalImpulses()[0];
        if (impulseForce < minimumImpulse){
            return;
        }

        WeaponComponent wc = weapon.get(Mappers.weaponM);
        if (wc == null){
            return;
        }

        final WorldManifold worldManifold = contact.getWorldManifold();
        final Vector2 normal = weaponIsA ? worldManifold.getNormal() : worldManifold.getNormal().scl(-1); //Vector from weapon to player
        final Vector2 point = worldManifold.getPoints()[0];
        final Fixture weaponFixture = weaponIsA ? contact.getFixtureA() : contact.getFixtureB();
        final Body weaponBody = weaponFixture.getBody();
        final Body playerBody = (weaponIsA ? contact.getFixtureB() : contact.getFixtureA()).getBody();
        PickUpComponent pickUpC = weapon.get(Mappers.pickUpM);
        @Nullable final Entity weaponOwner = pickUpC == null ? null : pickUpC.owner;


        float dullPercent = calculateDullness(weaponBody, point, normal); // �� ������� ��������� ������ ���� �������� ������
        float sharpPercent = 1 - dullPercent;                             // �� ������� ��������� ������ ���� �������� �������

        FixtureType weaponFixtureType = ((FixtureData) weaponFixture.getUserData()).getFixtureType();

        if (weaponFixtureType == FixtureType.WEAPON_DAMAGE) { // ���� ��������� ����� �������� ���������

            WeaponWreckerHitEvent event = new WeaponWreckerHitEvent(weapon, weaponOwner, player, new Vector2(point).scl(GameAssets.box2dScale), new Vector2(normal), impulseForce, sharpPercent, dullPercent, weaponBody, playerBody);
            getEngine().dispatchLater(event);
        }

    }

    private float calculateDullness(Body weapon, Vector2 box2dPoint, Vector2 box2dNormal){
        float angle = weapon.getLinearVelocityFromWorldPoint(box2dPoint).angle(box2dNormal);
        angle = angle < 0 ? -angle : angle;
        angle -= 90;
        angle = angle < 0 ? -angle : angle;
        return angle / 90f;
    }

    // ������� ������ ���������� �����
    private void handlePlayerToPlayer(Entity playerA, EntityType typeA, Entity playerB, EntityType typeB, Contact contact, ContactImpulse impulse){

    }

}
