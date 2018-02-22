package ru.maklas.wreckers.client.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import ru.maklas.mengine.Engine;
import ru.maklas.mengine.Subscription;
import ru.maklas.mengine.utils.Signal;
import ru.maklas.wreckers.assets.EntityType;
import ru.maklas.wreckers.assets.GameAssets;
import ru.maklas.wreckers.assets.Subscriptions;
import ru.maklas.wreckers.client.ClientGameModel;
import ru.maklas.wreckers.engine.Mappers;
import ru.maklas.wreckers.engine.components.*;
import ru.maklas.wreckers.engine.events.DamageEvent;
import ru.maklas.wreckers.engine.events.DeathEvent;
import ru.maklas.wreckers.game.FixtureData;
import ru.maklas.wreckers.game.FixtureType;

public class EntityPlayer extends GameEntity {

    private Body body;

    private static final int playerMass = 100;

    public EntityPlayer(int id, float x, float y, float health, ClientGameModel model, EntityType eType) {
        super(id, eType, x, y, GameAssets.playerZ);

        float bodyRadius = 40;
        float pickUpRadius = 75;
        float r = bodyRadius / GameAssets.box2dScale;
        float r2 = r * r;

        FixtureDef bodyF = model.getFixturer()
                .newFixture()
                .shape(model.getShaper()
                        .buildCircle(0, 0, bodyRadius))
                .friction(0.4f)
                .mask(eType)
                .bounciness(0.2f)
                .density(playerMass / (MathUtils.PI * r2))
                .build();

        body = model.getBuilder().newBody()
                .pos(x, y)
                .type(BodyDef.BodyType.DynamicBody)
                .linearDamp(1f)
                .addFixture(bodyF, new FixtureData(FixtureType.WRECKER_BODY))
                .build();

        System.out.println(id + ": Player mass " + body.getMassData().mass);

        add(new PhysicsComponent(body));
        add(new HealthComponent(health));
        add(new MotorComponent(60));
        add(new AntiGravComponent(body.getMassData().mass,5, 5, 1.2f));
        add(new SocketComponent(1, EntityType.weaponTypeFor(eType)));
        add(new GrabZoneComponent(model.getShaper().buildCircle(0, 0, pickUpRadius)));
        add(new WreckerComponent(100,
                80,
                50,
                50,
                50));
    }

    @Override
    protected void addedToEngine(final Engine engine) {
        subscribe(new Subscription<DeathEvent>(DeathEvent.class) {
            @Override
            public void receive(Signal<DeathEvent> signal, DeathEvent deathEvent) {
                if (deathEvent.getTarget() == EntityPlayer.this){
                    engine.remove(EntityPlayer.this);
                    System.out.println("Died");
                }
            }
        });

        subscribe(new Subscription<DamageEvent>(DamageEvent.class) {
            @Override
            public void receive(Signal<DamageEvent> signal, DamageEvent damageEvent) {
                if (damageEvent.getTarget() == EntityPlayer.this){
                    System.out.println(EntityPlayer.this.toString() + " Damage: " + damageEvent.getDamage() + " Health left: " + get(Mappers.healthM).health);
                }
            }
        });
    }
}
