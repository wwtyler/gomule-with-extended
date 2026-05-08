package gomule.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class D2ColorCodeTest {

    @Test
    public void supportsTylerPackDefenseAndUtilityPalette() {
        String defense = D2ColorCode.toHtml("ÿcA+239 防御ÿc0");
        String utility = D2ColorCode.toHtml("ÿcC+25% 更快速打击回复ÿc0");

        assertTrue(defense.contains("#5F8A00"), defense);
        assertTrue(utility.contains("#00C8FF"), utility);
    }

    @Test
    public void supportsSlayDamageColorCode() {
        String slay = D2ColorCode.toHtml("ÿc<+65% 对恶魔的伤害ÿc0");

        assertTrue(slay.contains("#FF7043"), slay);
    }

    @Test
    public void wealthAffixesUseGoldPalette() {
        String gold = D2ColorCode.toHtml("ÿcG200% Extra Gold from Monstersÿc0");
        String magicFind = D2ColorCode.toHtml("ÿcG35% Better Chance of Getting Magic Itemsÿc0");

        assertTrue(gold.contains("#C8A800"), gold);
        assertTrue(magicFind.contains("#C8A800"), magicFind);
    }
}