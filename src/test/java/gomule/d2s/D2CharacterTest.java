package gomule.d2s;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import randall.d2files.D2TxtFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnstableApiUsage")
public class D2CharacterTest {

    @Test
    public void complexChar() throws Exception {
        D2TxtFile.constructTxtFiles("./d2111");
        D2Character d2Character = new D2Character(new File(Resources.getResource("charFiles/complexChar.d2s").toURI()).getAbsolutePath());
        assertEquals(expectedComplexChar, d2Character.fullDumpStr().replaceAll("\r", ""));
    }

    private String expectedComplexChar = """
            Name:       ThePerfectJava
            Class:      Amazon
            Experience: 3232620645
            Level:      98
            
                        Naked/Gear
            Strength:   61/173
            Dexterity:  162/220
            Vitality:   342/367
            Energy:     15/20
            HP:         1272/1354
            Mana:       158/218
            Stamina:    496/676
            Defense:    40/1914
            AR:         780/1261
            
            Fire:       179/139/79
            Cold:       181/141/81
            Lightning:  187/147/87
            Poison:     177/137/77
            
            MF:         48       Block:      75
            GF:         0       FR/W:       70
            FHR:        24       IAS:        20
            FCR:        25
            
            Magic Arrow: 0/0
            Fire Arrow: 0/0
            Cold Arrow: 0/0
            Multiple Shot: 0/0
            Exploding Arrow: 0/0
            Ice Arrow: 0/0
            Guided Arrow: 0/0
            Strafe: 0/0
            Immolation Arrow: 0/0
            Freezing Arrow: 0/0
            
            Inner Sight: 0/0
            Critical Strike: 1/9
            Dodge: 1/9
            Slow Missiles: 0/0
            Avoid: 1/9
            Penetrate: 1/9
            Decoy: 0/0
            Evade: 1/9
            Valkyrie: 0/0
            Pierce: 1/9
            
            Jab: 1/23
            Power Strike: 20/42
            Poison Javelin: 1/23
            Impale: 0/0
            Lightning Bolt: 20/42
            Charged Strike: 20/42
            Plague Javelin: 1/23
            Fend: 0/0
            Lightning Strike: 20/45
            Lightning Fury: 20/45
            
            Viridian Small Charm
            Small Charm
            Required Level: 10
            Fingerprint: 0x61d091db
            Item Level: 1
            Version: Resurrected
            Poison Resist +7%
            
            Emerald Small Charm
            Small Charm
            Required Level: 32
            Fingerprint: 0xe7a2403f
            Item Level: 88
            Version: Resurrected
            Poison Resist +10%
            
            Crimson Small Charm of Life
            Small Charm
            Required Level: 14
            Fingerprint: 0xc1636060
            Item Level: 22
            Version: Resurrected
            +9 to Life
            Fire Resist +5%
            
            Titan's Revenge
            Ceremonial Javelin
            Throw Damage: 74 - 197
            One Hand Damage: 74 - 145
            Quantity: 143
            Required Level: 42
            Required Strength: 25
            Required Dexterity: 109
            Fingerprint: 0xdfa76564
            Item Level: 87
            Version: Resurrected
            +2 to Javelin and Spear Skills (Amazon Only)
            +2 to Amazon Skill Levels
            +30% Faster Run/Walk
            173% Enhanced Damage
            Adds 25 - 50 Damage
            9% Life stolen per hit
            +20 to Strength
            +20 to Dexterity
            Increased Stack Size
            Replenishes quantity
            
            Harpoonist's Grand Charm of Maiming
            Grand Charm
            Required Level: 63
            Fingerprint: 0x9256fa58
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +3 to Maximum Damage
            
            Russet Grand Charm of Sustenance
            Grand Charm
            Required Level: 23
            Fingerprint: 0xe4e9333d
            Item Level: 69
            Version: Resurrected
            +24 to Life
            Fire Resist +16%
            
            Viridian Small Charm
            Small Charm
            Required Level: 10
            Fingerprint: 0xe1dd3a1e
            Item Level: 88
            Version: Resurrected
            Poison Resist +6%
            
            Emerald Small Charm of the Glacier
            Small Charm
            Required Level: 32
            Fingerprint: 0xba6b6fae
            Item Level: 85
            Version: Resurrected
            Adds 3 - 6 Cold Damage Over 1 Secs (25 Frames)
            Poison Resist +11%
            
            Emerald Small Charm of Storms
            Small Charm
            Required Level: 37
            Fingerprint: 0x4bd6750d
            Item Level: 66
            Version: Resurrected
            Adds 1 - 20 Lightning Damage
            Poison Resist +11%
            
            Emerald Small Charm of Dexterity
            Small Charm
            Required Level: 32
            Fingerprint: 0x2099a50e
            Item Level: 88
            Version: Resurrected
            +1 to Dexterity
            Poison Resist +11%
            
            Crimson Small Charm
            Small Charm
            Required Level: 1
            Fingerprint: 0xfb404e24
            Item Level: 73
            Version: Resurrected
            Fire Resist +3%
            
            Viridian Small Charm
            Small Charm
            Required Level: 10
            Fingerprint: 0x2378cd91
            Item Level: 5
            Version: Resurrected
            Poison Resist +7%
            
            Viridian Small Charm
            Small Charm
            Required Level: 10
            Fingerprint: 0x9bce0b62
            Item Level: 7
            Version: Resurrected
            Poison Resist +7%
            
            Emerald Small Charm
            Small Charm
            Required Level: 32
            Fingerprint: 0xaa284445
            Item Level: 80
            Version: Resurrected
            Poison Resist +11%
            
            Crimson Small Charm
            Small Charm
            Required Level: 1
            Fingerprint: 0xbb6b812a
            Item Level: 85
            Version: Resurrected
            Fire Resist +5%
            
            Sapphire Small Charm of Dexterity
            Small Charm
            Required Level: 32
            Fingerprint: 0x85e8d8d3
            Item Level: 86
            Version: Resurrected
            +1 to Dexterity
            Cold Resist +11%
            
            Sapphire Small Charm
            Small Charm
            Required Level: 32
            Fingerprint: 0xada3471e
            Item Level: 88
            Version: Resurrected
            Cold Resist +10%
            
            Sapphire Small Charm
            Small Charm
            Required Level: 32
            Fingerprint: 0x2c3323d1
            Item Level: 88
            Version: Resurrected
            Cold Resist +10%
            
            Harpoonist's Grand Charm of Craftmanship
            Grand Charm
            Required Level: 42
            Fingerprint: 0x83b4c80d
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +1 to Maximum Damage
            
            Harpoonist's Grand Charm of Balance
            Grand Charm
            Required Level: 42
            Fingerprint: 0xc5261c1b
            Item Level: 99
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +12% Faster Hit Recovery
            
            Amber Small Charm of Dexterity
            Small Charm
            Required Level: 32
            Fingerprint: 0x4115290
            Item Level: 85
            Version: Resurrected
            +2 to Dexterity
            Lightning Resist +11%
            
            Amber Small Charm of Balance
            Small Charm
            Required Level: 32
            Fingerprint: 0xa627a9a4
            Item Level: 85
            Version: Resurrected
            +5% Faster Hit Recovery
            Lightning Resist +11%
            
            Harpoonist's Grand Charm of Dexterity
            Grand Charm
            Required Level: 42
            Fingerprint: 0xf15c64eb
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +4 to Dexterity
            
            Harpoonist's Grand Charm of Strength
            Grand Charm
            Required Level: 42
            Fingerprint: 0x6aefa918
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +5 to Strength
            
            Amber Small Charm of Strength
            Small Charm
            Required Level: 32
            Fingerprint: 0xe16d4cd6
            Item Level: 80
            Version: Resurrected
            +1 to Strength
            Lightning Resist +10%
            
            Harpoonist's Grand Charm of Strength
            Grand Charm
            Required Level: 42
            Fingerprint: 0x40eb581d
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +5 to Strength
            
            Harpoonist's Grand Charm of Strength
            Grand Charm
            Required Level: 42
            Fingerprint: 0x25db1c2a
            Item Level: 85
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +4 to Strength
            
            Amber Small Charm of Strength
            Small Charm
            Required Level: 32
            Fingerprint: 0xb790b4c1
            Item Level: 88
            Version: Resurrected
            +2 to Strength
            Lightning Resist +10%
            
            Amber Small Charm of Flame
            Small Charm
            Required Level: 32
            Fingerprint: 0xed06d3ce
            Item Level: 88
            Version: Resurrected
            Adds 1 - 2 Fire Damage
            Lightning Resist +10%
            
            Small Charm of Good Luck
            Small Charm
            Required Level: 33
            Fingerprint: 0xf835c91b
            Item Level: 67
            Version: Resurrected
            7% Better Chance of Getting Magic Items
            
            Mara's Kaleidoscope
            Amulet
            Required Level: 67
            Fingerprint: 0x68fe8447
            Item Level: 87
            Version: Resurrected
            +2 to All Skills
            All Stats +5
            All Resistances +26
            
            Raven Frost
            Ring
            Required Level: 45
            Fingerprint: 0x52eaf57b
            Item Level: 90
            Version: Resurrected
            +191 to Attack Rating
            Adds 15 - 45 Cold Damage Over 4 Secs (100 Frames)
            +20 to Dexterity
            +40 to Mana
            +20 Cold Absorb
            Cannot Be Frozen
            
            The Stone of Jordan
            Ring
            Required Level: 29
            Fingerprint: 0x10ac91b9
            Item Level: 99
            Version: Resurrected
            +1 to All Skills
            Adds 1 - 12 Lightning Damage
            +20 to Mana
            Increase Maximum Mana 25%
            
            Thundergod's Vigor
            War Belt
            Defense: 159
            Durability: 6 of 24
            Required Level: 47
            Required Strength: 110
            Fingerprint: 0x652b277b
            Item Level: 88
            Version: Resurrected
            5% Chance to cast level 7 Fist of the Heavens when struck
            Adds 1 - 50 Lightning Damage
            +3 to Lightning Fury (Amazon Only)
            +3 to Lightning Strike (Amazon Only)
            +200% Enhanced Defense
            +20 to Strength
            +20 to Vitality
            +10% to Maximum Lightning Resist
            Lightning Absorb 20%
            
            Aldur's Advance
            Battle Boots
            Defense: 42
            Durability: 11 of 18
            Required Level: 45
            Required Strength: 95
            Fingerprint: 0x7bc09616
            Item Level: 99
            Version: Resurrected
            Indestructible
            +40% Faster Run/Walk
            +50 to Life
            +180 Maximum Stamina
            Heal Stamina Plus 32%
            Fire Resist +44%
            10% Damage Taken Goes To Mana
            Set (2 items): +15 to Dexterity
            Set (3 items): +15 to Dexterity
            Set (4 items): +15 to Dexterity
            
            
            Loath Clutches
            Light Gauntlets
            Defense: 17
            Durability: 4 of 18
            Required Level: 35
            Required Strength: 45
            Fingerprint: 0xe7208e05
            Item Level: 55
            Version: Resurrected
            +2 to Javelin and Spear Skills (Amazon Only)
            +20% Increased Attack Speed
            3% Life stolen per hit
            +49% Enhanced Defense
            Fire Resist +14%
            23% Better Chance of Getting Magic Items
            
            Coral Small Charm
            Small Charm
            Required Level: 20
            Fingerprint: 0xf0273fb9
            Item Level: 86
            Version: Resurrected
            Lightning Resist +8%
            
            Crimson Small Charm of Greed
            Small Charm
            Required Level: 15
            Fingerprint: 0x46fdfa1e
            Item Level: 22
            Version: Resurrected
            Fire Resist +5%
            9% Extra Gold from Monsters
            
            Harpoonist's Grand Charm of Dexterity
            Grand Charm
            Required Level: 42
            Fingerprint: 0x5c556f34
            Item Level: 67
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +6 to Dexterity
            
            Ocher Small Charm of Frost
            Small Charm
            Required Level: 10
            Fingerprint: 0x41fd42dd
            Item Level: 88
            Version: Resurrected
            Adds 1 - 2 Cold Damage Over 1 Secs (25 Frames)
            Lightning Resist +7%
            
            Horadric Cube
            Fingerprint: 0x180f812
            Item Level: 13
            Version: Resurrected
            
            Harpoonist's Grand Charm of Balance
            Grand Charm
            Required Level: 42
            Fingerprint: 0x2232064a
            Item Level: 59
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +12% Faster Hit Recovery
            
            Harpoonist's Grand Charm of Sustenance
            Grand Charm
            Required Level: 53
            Fingerprint: 0x452acdf
            Item Level: 68
            Version: Resurrected
            +1 to Javelin and Spear Skills (Amazon Only)
            +32 to Life
            
            Toxic Small Charm of Inertia
            Small Charm
            Required Level: 55
            Fingerprint: 0xe52700c3
            Item Level: 85
            Version: Resurrected
            +3% Faster Run/Walk
            Adds 100 Poison Damage Over 5 Secs (125 Frames)
            
            Chains of Honor
            Archon Plate
            DolUmBerIst
            Defense: 882
            Durability: 15 of 60
            Required Level: 63
            Required Strength: 103
            Fingerprint: 0xed9dd144
            Item Level: 85
            Version: Resurrected
            +2 to All Skills
            +200% Damage to Demons
            +100% Damage to Undead
            8% Life stolen per hit
            +70% Enhanced Defense
            +20 to Strength
            Replenish Life +7
            All Resistances +65
            Damage Reduced by 8%
            25% Better Chance of Getting Magic Items
            4 Sockets (4 used)
            Socketed: Dol Rune
            Socketed: Um Rune
            Socketed: Ber Rune
            Socketed: Ist Rune
            
            Dol Rune
            Required Level: 31
            Version: Resurrected
            Weapons: Hit Causes Monster to Flee +25%
            Armor: Replenish Life +7
            Shields: Replenish Life +7
            
            Um Rune
            Required Level: 47
            Version: Resurrected
            Weapons: 25% Chance of Open Wounds
            Armor: Cold Resist +15%
            Lightning Resist +15%
            Fire Resist +15%
            Poison Resist +15%
            Shields: Cold Resist +22%
            Lightning Resist +22%
            Fire Resist +22%
            Poison Resist +22%
            
            Ber Rune
            Required Level: 63
            Version: Resurrected
            Weapons: 20% Chance of Crushing Blow
            Armor: Damage Reduced by 8%
            Shields: Damage Reduced by 8%
            
            Ist Rune
            Required Level: 51
            Version: Resurrected
            Weapons: 30% Better Chance of Getting Magic Items
            Armor: 25% Better Chance of Getting Magic Items
            Shields: 25% Better Chance of Getting Magic Items
            
            Widowmaker
            Ward Bow
            Two Hand Damage: 53 - 142
            Durability: 45 of 48
            Required Level: 65
            Required Strength: 72
            Required Dexterity: 146
            Fingerprint: 0x68f39e6f
            Item Level: 85
            Version: Resurrected
            Fires Magic Arrows
            169% Enhanced Damage
            Ignore Target's Defense
            33% Deadly Strike
            +5 to Guided Arrow
            
            Rejuvenation Potion
            Version: Resurrected
            Replenishes Mana 35%
            Replenishes Health 35%
            
            Super Healing Potion
            Version: Resurrected
            Replenish Life +320
            
            Super Healing Potion
            Version: Resurrected
            Replenish Life +320
            
            Super Healing Potion
            Version: Resurrected
            Replenish Life +320
            
            Super Mana Potion
            Version: Resurrected
            Replenishes Mana 250%
            
            Call to Arms
            War Scepter
            AmnRalMalIstOhm
            One Hand Damage: 37 - 63
            Durability: 68 of 70
            Required Level: 57
            Required Strength: 55
            Fingerprint: 0x1baff84d
            GUID: 0x0 0x0 0xb75ebe57 0xa491bdb
            Item Level: 61
            Version: Resurrected
            +2 to All Skills
            +40% Increased Attack Speed
            273% Enhanced Damage
            +150% Damage to Undead
            Adds 5 - 30 Fire Damage
            7% Life stolen per hit
            Prevent Monster Heal
            +9 to Battle Command
            +13 to Battle Orders
            +10 to Battle Cry
            Replenish Life +12
            30% Better Chance of Getting Magic Items
            5 Sockets (5 used)
            Socketed: Amn Rune
            Socketed: Ral Rune
            Socketed: Mal Rune
            Socketed: Ist Rune
            Socketed: Ohm Rune
            
            Amn Rune
            Required Level: 25
            GUID: 0x0 0x0 0x5203a1ac 0x67e15e4
            Version: Resurrected
            Weapons: 7% Life stolen per hit
            Armor: Attacker Takes Damage of 14
            Shields: Attacker Takes Damage of 14
            
            Ral Rune
            Required Level: 19
            GUID: 0x0 0x0 0x66f03f8f 0x70897b7
            Version: Resurrected
            Weapons: Adds 5 - 30 Fire Damage
            Armor: Fire Resist +30%
            Shields: Fire Resist +35%
            
            Mal Rune
            Required Level: 49
            GUID: 0x0 0x0 0x810819fd 0x5f39411
            Version: Resurrected
            Weapons: Prevent Monster Heal
            Armor: Magic Damage Reduced by 7
            Shields: Magic Damage Reduced by 7
            
            Ist Rune
            Required Level: 51
            GUID: 0x0 0x0 0xddb48852 0x569123e
            Version: Resurrected
            Weapons: 30% Better Chance of Getting Magic Items
            Armor: 25% Better Chance of Getting Magic Items
            Shields: 25% Better Chance of Getting Magic Items
            
            Ohm Rune
            Required Level: 57
            Version: Resurrected
            Weapons: +50% Enhanced Damage
            Armor: +5% to Maximum Cold Resist
            Shields: +5% to Maximum Cold Resist
            
            Griffon's Eye
            Diadem
            Defense: 247
            Durability: 19 of 20
            Required Level: 76
            Fingerprint: 0x5c55218a
            Item Level: 86
            Version: Resurrected
            100% Chance to cast level 41 Nova when you Level-Up
            +1 to All Skills
            +25% Faster Cast Rate
            Adds 1 - 74 Lightning Damage
            -25% to Enemy Lightning Resistance
            +20% to Lightning Skill Damage
            +191 Defense
            1 Sockets (1 used)
            Socketed: Rainbow Facet
            
            Rainbow Facet
            Jewel
            Required Level: 49
            Fingerprint: 0x79ae2700
            Item Level: 99
            Version: Resurrected
            100% Chance to cast level 41 Nova when you Level-Up
            Adds 1 - 74 Lightning Damage
            -5% to Enemy Lightning Resistance
            +5% to Lightning Skill Damage
            
            Gemmed Circlet
            Circlet
            Defense: 25
            Durability: 15 of 35
            Required Level: 41
            Fingerprint: 0x589dd484
            Item Level: 88
            Version: Resurrected
            +20 to Strength
            2 Sockets (2 used)
            Socketed: Fal Rune
            Socketed: Fal Rune
            
            Fal Rune
            Required Level: 41
            Version: Resurrected
            Weapons: +10 to Strength
            Armor: +10 to Strength
            Shields: +10 to Strength
            
            Fal Rune
            Required Level: 41
            Version: Resurrected
            Weapons: +10 to Strength
            Armor: +10 to Strength
            Shields: +10 to Strength
            
            BigBoobsBigBow's Titan's Revenge
            Matriarchal Javelin
            Throw Damage: 169 - 324
            One Hand Damage: 149 - 274
            Quantity: 143
            Required Level: 55
            Required Strength: 97
            Required Dexterity: 141
            Fingerprint: 0xac444728
            Item Level: 87
            Version: Resurrected
            +2 to Javelin and Spear Skills (Amazon Only)
            +2 to Amazon Skill Levels
            +30% Faster Run/Walk
            177% Enhanced Damage
            Adds 25 - 50 Damage
            5% Life stolen per hit
            +20 to Strength
            +20 to Dexterity
            Increased Stack Size
            Replenishes quantity
            Required Level +7
            Ethereal
            
            Stormshield
            Monarch
            Defense: 512
            Chance to Block: 47
            Indestructible
            Required Level: 73
            Required Strength: 156
            Fingerprint: 0x3b767fa8
            Item Level: 99
            Version: Resurrected
            100% Chance to cast level 47 Chain Lightning when you Die
            Indestructible
            +35% Faster Block Rate
            25% Increased Chance of Blocking
            Adds 1 - 74 Lightning Damage
            -5% to Enemy Lightning Resistance
            +5% to Lightning Skill Damage
            +367 Defense (Based on Character Level)
            +30 to Strength
            Cold Resist +60%
            Lightning Resist +25%
            Damage Reduced by 35%
            Attacker Takes Lightning Damage of 10
            1 Sockets (1 used)
            Socketed: Rainbow Facet
            
            Rainbow Facet
            Jewel
            Required Level: 49
            Fingerprint: 0xf74a9abf
            Item Level: 87
            Version: Resurrected
            100% Chance to cast level 47 Chain Lightning when you Die
            Adds 1 - 74 Lightning Damage
            -5% to Enemy Lightning Resistance
            +5% to Lightning Skill Damage
            
            Mercenary:
            
            Name:       Razan
            Race:       Desert Mercenary
            Type:       HolyFreeze-Nightmare
            Experience: 107329840
            Level:      96
            Dead?:      unknown
            
                        Naked/Gear
            Strength:   209/209
            Dexterity:  170/170
            HP:         2270/2270
            Defense:    1657/2218
            AR:         2105/2108
            
            Fire:       233/193/133
            Cold:       263/223/163
            Lightning:  233/193/133
            Poison:     233/193/133
            
            Treachery
            Wire Fleece
            ShaelThulLem
            Defense: 455
            Durability: 29 of 32
            Required Level: 53
            Required Strength: 111
            Fingerprint: 0xd979d7a7
            Item Level: 88
            Version: Resurrected
            5% Chance to cast level 15 Fade when struck
            25% Chance to cast level 15 Venom on striking
            +2 to Assassin Skill Levels
            +45% Increased Attack Speed
            +20% Faster Hit Recovery
            Cold Resist +30%
            50% Extra Gold from Monsters
            3 Sockets (3 used)
            Socketed: Shael Rune
            Socketed: Thul Rune
            Socketed: Lem Rune
            
            Shael Rune
            Required Level: 29
            Version: Resurrected
            Weapons: +20% Increased Attack Speed
            Armor: +20% Faster Hit Recovery
            Shields: +20% Faster Block Rate
            
            Thul Rune
            Required Level: 23
            Version: Resurrected
            Weapons: Adds 3 - 14 Cold Damage Over 3 Secs (75 Frames)
            Armor: Cold Resist +30%
            Shields: Cold Resist +35%
            
            Lem Rune
            Required Level: 43
            Version: Resurrected
            Weapons: 75% Extra Gold from Monsters
            Armor: 50% Extra Gold from Monsters
            Shields: 50% Extra Gold from Monsters
            
            
            Kira's Guardian
            Tiara
            Defense: 106
            Durability: 17 of 25
            Required Level: 77
            Fingerprint: 0x843d90eb
            Item Level: 99
            Version: Resurrected
            +20% Faster Hit Recovery
            +64 Defense
            All Resistances +70
            Cannot Be Frozen
            
            
            Infinity
            Superior Great Poleaxe
            BerMalBerIst
            Two Hand Damage: 296 - 817
            Durability: 28 of 28
            Required Level: 63
            Required Strength: 169
            Required Dexterity: 89
            Fingerprint: 0xc1a9ec4
            Item Level: 88
            Version: Resurrected
            50% Chance to cast level 20 Chain Lightning when you Kill an Enemy
            Level 12 Conviction Aura When Equipped
            +35% Faster Run/Walk
            330% Enhanced Damage
            +3 to Attack Rating
            -46% to Enemy Lightning Resistance
            40% Chance of Crushing Blow
            Prevent Monster Heal
            +49 to Vitality (Based on Character Level)
            30% Better Chance of Getting Magic Items
            Level 21 Cyclone Armor Level 30 %s (30/30 Charges)
            Ethereal
            4 Sockets (4 used)
            Socketed: Ber Rune
            Socketed: Mal Rune
            Socketed: Ber Rune
            Socketed: Ist Rune
            
            Ber Rune
            Required Level: 63
            Version: Resurrected
            Weapons: 20% Chance of Crushing Blow
            Armor: Damage Reduced by 8%
            Shields: Damage Reduced by 8%
            
            Mal Rune
            Required Level: 49
            Version: Resurrected
            Weapons: Prevent Monster Heal
            Armor: Magic Damage Reduced by 7
            Shields: Magic Damage Reduced by 7
            
            Ber Rune
            Required Level: 63
            Version: Resurrected
            Weapons: 20% Chance of Crushing Blow
            Armor: Damage Reduced by 8%
            Shields: Damage Reduced by 8%
            
            Ist Rune
            Required Level: 51
            Version: Resurrected
            Weapons: 30% Better Chance of Getting Magic Items
            Armor: 25% Better Chance of Getting Magic Items
            Shields: 25% Better Chance of Getting Magic Items
            
            
            """;
}