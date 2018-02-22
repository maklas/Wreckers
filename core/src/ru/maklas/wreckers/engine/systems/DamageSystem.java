package ru.maklas.wreckers.engine.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import ru.maklas.mengine.Engine;
import ru.maklas.mengine.Entity;
import ru.maklas.mengine.EntitySystem;
import ru.maklas.mengine.Subscription;
import ru.maklas.mengine.utils.Signal;
import ru.maklas.wreckers.assets.GameAssets;
import ru.maklas.wreckers.client.entities.EntityArrow;
import ru.maklas.wreckers.client.entities.EntityNumber;
import ru.maklas.wreckers.client.entities.EntityString;
import ru.maklas.wreckers.engine.Mappers;
import ru.maklas.wreckers.engine.components.HealthComponent;
import ru.maklas.wreckers.engine.components.WeaponComponent;
import ru.maklas.wreckers.engine.components.WreckerComponent;
import ru.maklas.wreckers.engine.events.DamageEvent;
import ru.maklas.wreckers.engine.events.DeathEvent;
import ru.maklas.wreckers.engine.events.Event;
import ru.maklas.wreckers.engine.events.requests.WeaponWreckerHitEvent;
import ru.maklas.wreckers.libs.Utils;

import java.util.Random;

public class DamageSystem extends EntitySystem {

    private final Vector2 vec1 = new Vector2();
    private final Vector2 vec2 = new Vector2();
    private final Random random = new Random();


    @Override
    public void onAddedToEngine(final Engine engine) {
        subscribe(new Subscription<WeaponWreckerHitEvent>(WeaponWreckerHitEvent.class) {
            @Override
            public void receive(Signal<WeaponWreckerHitEvent> signal, WeaponWreckerHitEvent e) {
                Entity weapon = e.getWeapon();
                WreckerComponent wreckC = e.getTargetWrecker().get(Mappers.wreckerM);
                WeaponComponent weapC = weapon.get(Mappers.weaponM);
                HealthComponent hc = e.getTargetWrecker().get(Mappers.healthM);
                if (weapC == null || wreckC == null || hc == null){
                    System.err.println("Wrecker or Weapon doesn't have stats to do damage");
                    return;
                }

                //���ר� �����

                final float damageAdjustment = e.getWeaponOwner() == null ? 1/1000f : 1/300f; // ������ ��� ��������� ������� � ���� ������ �����

                float trueDullDamage = e.getImpulse()  * e.getDullness()  * weapC.dullDamage * damageAdjustment;
                float trueSliceDamage = e.getImpulse() * e.getSharpness() * weapC.sliceDamage * damageAdjustment;

                float dullDamage = trueDullDamage * leagueFormula(wreckC.dullArmor);
                float sliceDamage = trueSliceDamage * leagueFormula(wreckC.sliceArmor);

                float totalDamage = dullDamage + sliceDamage; // �������� �����


                //���ר� ��� �������

                float additionalImpulse = e.getWreckerBody().getMass() * (e.getImpulse() * ((weapC.hitImpulse * leagueFormula(wreckC.stability)) / 100) ); // �������������� ������������. ����� ���� ������ ����


                //����ר� �����

                float dullHitForce = ((e.getImpulse() * e.getDullness()) / 500);
                dullHitForce = dullHitForce > 1 ? 1 : dullHitForce;   // �������� ������ �����. 0..1
                float stunChance = dullHitForce * (weapC.stunAbility / 100f) //��������� ������� �� ����� ������. 1, ������ ���� stunAbility == 100
                        * leagueFormula(wreckC.stunResist); // ��������� �������.
                System.out.println("Stun chance: " + stunChance);
                boolean doStun = random.nextFloat() < stunChance;
                float stunDuration = 0;
                if (doStun){
                    stunDuration = weapC.stunAbility / 40f; //�� 2.5 ������ �����.
                }




                Vector2 box2dPos = vec1.set(e.getPoint()).scl(1 / GameAssets.box2dScale);
                Vector2 box2dImpulse = vec2.set(e.getNormal()).scl(additionalImpulse);
                e.getWreckerBody().applyForce(box2dImpulse, box2dPos, true); //��������� ��� �������.
                applyDamageAndDispatch(e.getTargetWrecker(), hc, totalDamage, e);  //��������� ����

                engine.add(new EntityNumber((int) totalDamage, 2, e.getPoint().x, e.getPoint().y));
                if (doStun){
                    engine.add(new EntityString("STUN!", 2, e.getPoint().x, e.getPoint().y + 50, Color.RED));
                }
                if (e.getSharpness() > 0.90f){
                    engine.add(new EntityString("Sharp! " + (int)(e.getSharpness() * 100), 2, e.getPoint().x + (random.nextFloat() * 50 - 25), e.getPoint().y + 25, Color.BLUE));
                }
                if (e.getDullness() > 0.90f){
                    engine.add(new EntityString("Dull! " + (int)(e.getDullness() * 100), 2, e.getPoint().x + (random.nextFloat() * 50 - 25), e.getPoint().y - 25, Color.RED));
                }
                engine.add(new EntityArrow(e.getPoint(), e.getNormal().scl(75).add(e.getPoint()), 1, Color.ORANGE));
            }
        });
    }


    private void applyDamageAndDispatch(Entity e, HealthComponent hc, float damage, Event hitEvent){
        hc.health -= damage;
        getEngine().dispatch(new DamageEvent(e, damage, hitEvent));
        if (hc.health < 0){
            hc.health = 0;
            hc.dead = true;
            getEngine().dispatchLater(new DeathEvent(e, hitEvent));
        }
    }

    /**
     * ���������� ������� ����������� ����� ����� ����� ��������. ����������� ���:
     * <p>������������������� = ���������� * leagueFormula(������);</p>
     */
    private float leagueFormula(float resist){
        return ((100) / (100 + resist));
    }

}
