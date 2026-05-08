/*
 * Created on 11-mei-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.item;

import java.awt.Point;
import java.util.ArrayList;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2BodyLocations {
    public static final ArrayList<D2BodyLocations> sListAll = new ArrayList<>();
    public static final D2BodyLocations BODY_HEAD = new D2BodyLocations("head", "Head");
    public static final D2BodyLocations BODY_TORS = new D2BodyLocations("tors", "Body");
    public static final D2BodyLocations BODY_GLOV = new D2BodyLocations("glov", "Gloves");
    public static final D2BodyLocations BODY_BELT = new D2BodyLocations("belt", "Belt");
    public static final D2BodyLocations BODY_FEET = new D2BodyLocations("feet", "Boots");
    public static final D2BodyLocations BODY_RARM = new D2BodyLocations("rarm", "Shield");
    public static final D2BodyLocations BODY_ALL = new D2BodyLocations("all", "All");
    public static final D2BodyLocations BODY_NECK = new D2BodyLocations("neck", "Amulet");
    public static final D2BodyLocations BODY_LRIN = new D2BodyLocations("lrin", "Ring");
    private final String iLocation;
    private final String iDisplay;

    static {
        sListAll.add(BODY_HEAD);
        sListAll.add(BODY_TORS);
        sListAll.add(BODY_GLOV);
        sListAll.add(BODY_BELT);
        sListAll.add(BODY_FEET);
        sListAll.add(BODY_RARM);
        sListAll.add(BODY_ALL);
    }

    private D2BodyLocations(String pLocation, String pDisplay) {
        iLocation = pLocation;
        iDisplay = pDisplay;
    }

    public static ArrayList<D2BodyLocations> getArmorFilterList() {
        return sListAll;
    }

    public static Point[] generateSkillLocs(int lCharCode) {

        Point[] iSkillLocs = new Point[30];
        switch (lCharCode) {
            case 0 -> {
//			cClass = "ama";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(173, 64);
                iSkillLocs[2] = new Point(50, 125);
                iSkillLocs[3] = new Point(112, 125);
                iSkillLocs[4] = new Point(173, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(112, 248);
                iSkillLocs[7] = new Point(112, 307);
                iSkillLocs[8] = new Point(173, 307);
                iSkillLocs[9] = new Point(50, 370);

                iSkillLocs[10] = new Point(50, 64);
                iSkillLocs[11] = new Point(173, 64);
                iSkillLocs[12] = new Point(112, 125);
                iSkillLocs[13] = new Point(50, 187);
                iSkillLocs[14] = new Point(112, 187);
                iSkillLocs[15] = new Point(173, 248);
                iSkillLocs[16] = new Point(50, 307);
                iSkillLocs[17] = new Point(112, 307);
                iSkillLocs[18] = new Point(50, 370);
                iSkillLocs[19] = new Point(173, 370);

                iSkillLocs[20] = new Point(50, 64);
                iSkillLocs[21] = new Point(112, 125);
                iSkillLocs[22] = new Point(173, 125);
                iSkillLocs[23] = new Point(50, 187);
                iSkillLocs[24] = new Point(173, 187);
                iSkillLocs[25] = new Point(112, 248);
                iSkillLocs[26] = new Point(173, 248);
                iSkillLocs[27] = new Point(50, 307);
                iSkillLocs[28] = new Point(112, 370);
                iSkillLocs[29] = new Point(173, 370);
            }
            case 1 -> {
//			cClass = "sor";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(173, 64);
                iSkillLocs[2] = new Point(50, 125);
                iSkillLocs[3] = new Point(50, 187);
                iSkillLocs[4] = new Point(112, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(173, 248);
                iSkillLocs[7] = new Point(112, 307);
                iSkillLocs[8] = new Point(112, 370);
                iSkillLocs[9] = new Point(173, 370);

                iSkillLocs[10] = new Point(112, 64);
                iSkillLocs[11] = new Point(50, 125);
                iSkillLocs[12] = new Point(173, 125);
                iSkillLocs[13] = new Point(50, 187);
                iSkillLocs[14] = new Point(112, 187);
                iSkillLocs[15] = new Point(112, 248);
                iSkillLocs[16] = new Point(173, 248);
                iSkillLocs[17] = new Point(50, 307);
                iSkillLocs[18] = new Point(173, 307);
                iSkillLocs[19] = new Point(112, 370);

                iSkillLocs[20] = new Point(112, 64);
                iSkillLocs[21] = new Point(173, 64);
                iSkillLocs[22] = new Point(50, 125);
                iSkillLocs[23] = new Point(112, 125);
                iSkillLocs[24] = new Point(173, 187);
                iSkillLocs[25] = new Point(112, 248);
                iSkillLocs[26] = new Point(50, 307);
                iSkillLocs[27] = new Point(173, 307);
                iSkillLocs[28] = new Point(50, 370);
                iSkillLocs[29] = new Point(112, 370);
            }
            case 2 -> {
//			cClass = "nec";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(50, 125);
                iSkillLocs[2] = new Point(173, 125);
                iSkillLocs[3] = new Point(112, 187);
                iSkillLocs[4] = new Point(173, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(112, 248);
                iSkillLocs[7] = new Point(50, 307);
                iSkillLocs[8] = new Point(173, 307);
                iSkillLocs[9] = new Point(112, 370);

                iSkillLocs[10] = new Point(112, 64);
                iSkillLocs[11] = new Point(173, 64);
                iSkillLocs[12] = new Point(50, 125);
                iSkillLocs[13] = new Point(112, 125);
                iSkillLocs[14] = new Point(173, 187);
                iSkillLocs[15] = new Point(50, 248);
                iSkillLocs[16] = new Point(112, 248);
                iSkillLocs[17] = new Point(173, 307);
                iSkillLocs[18] = new Point(50, 370);
                iSkillLocs[19] = new Point(112, 370);

                iSkillLocs[20] = new Point(50, 64);
                iSkillLocs[21] = new Point(173, 64);
                iSkillLocs[22] = new Point(112, 125);
                iSkillLocs[23] = new Point(50, 187);
                iSkillLocs[24] = new Point(173, 187);
                iSkillLocs[25] = new Point(112, 248);
                iSkillLocs[26] = new Point(50, 307);
                iSkillLocs[27] = new Point(112, 307);
                iSkillLocs[28] = new Point(112, 370);
                iSkillLocs[29] = new Point(173, 370);
            }
            case 3 -> {
//			cClass = "pal";
                iSkillLocs[0] = new Point(50, 64);
                iSkillLocs[1] = new Point(173, 64);
                iSkillLocs[2] = new Point(112, 125);
                iSkillLocs[3] = new Point(50, 187);
                iSkillLocs[4] = new Point(173, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(112, 248);
                iSkillLocs[7] = new Point(50, 307);
                iSkillLocs[8] = new Point(173, 307);
                iSkillLocs[9] = new Point(112, 370);

                iSkillLocs[10] = new Point(50, 64);
                iSkillLocs[11] = new Point(112, 125);
                iSkillLocs[12] = new Point(173, 125);
                iSkillLocs[13] = new Point(50, 187);
                iSkillLocs[14] = new Point(50, 248);
                iSkillLocs[15] = new Point(112, 248);
                iSkillLocs[16] = new Point(112, 307);
                iSkillLocs[17] = new Point(173, 307);
                iSkillLocs[18] = new Point(50, 370);
                iSkillLocs[19] = new Point(173, 370);

                iSkillLocs[20] = new Point(50, 64);
                iSkillLocs[21] = new Point(173, 64);
                iSkillLocs[22] = new Point(112, 125);
                iSkillLocs[23] = new Point(173, 125);
                iSkillLocs[24] = new Point(50, 187);
                iSkillLocs[25] = new Point(173, 187);
                iSkillLocs[26] = new Point(112, 248);
                iSkillLocs[27] = new Point(50, 307);
                iSkillLocs[28] = new Point(112, 370);
                iSkillLocs[29] = new Point(173, 370);
            }
            case 4 -> {
//			cClass = "bar";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(50, 125);
                iSkillLocs[2] = new Point(173, 125);
                iSkillLocs[3] = new Point(112, 187);
                iSkillLocs[4] = new Point(173, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(112, 248);
                iSkillLocs[7] = new Point(173, 307);
                iSkillLocs[8] = new Point(50, 370);
                iSkillLocs[9] = new Point(112, 370);

                iSkillLocs[10] = new Point(50, 64);
                iSkillLocs[11] = new Point(112, 64);
                iSkillLocs[12] = new Point(173, 64);
                iSkillLocs[13] = new Point(50, 125);
                iSkillLocs[14] = new Point(112, 125);
                iSkillLocs[15] = new Point(173, 125);
                iSkillLocs[16] = new Point(50, 187);
                iSkillLocs[17] = new Point(173, 248);
                iSkillLocs[18] = new Point(50, 307);
                iSkillLocs[19] = new Point(173, 370);

                iSkillLocs[20] = new Point(50, 64);
                iSkillLocs[21] = new Point(173, 64);
                iSkillLocs[22] = new Point(50, 125);
                iSkillLocs[23] = new Point(112, 125);
                iSkillLocs[24] = new Point(173, 187);
                iSkillLocs[25] = new Point(50, 248);
                iSkillLocs[26] = new Point(112, 307);
                iSkillLocs[27] = new Point(173, 307);
                iSkillLocs[28] = new Point(50, 370);
                iSkillLocs[29] = new Point(112, 370);
            }
            case 5 -> {
//			cClass = "dru";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(173, 64);
                iSkillLocs[2] = new Point(50, 125);
                iSkillLocs[3] = new Point(112, 125);
                iSkillLocs[4] = new Point(173, 187);
                iSkillLocs[5] = new Point(50, 248);
                iSkillLocs[6] = new Point(112, 248);
                iSkillLocs[7] = new Point(173, 307);
                iSkillLocs[8] = new Point(50, 370);
                iSkillLocs[9] = new Point(112, 370);

                iSkillLocs[10] = new Point(50, 64);
                iSkillLocs[11] = new Point(112, 64);
                iSkillLocs[12] = new Point(173, 125);
                iSkillLocs[13] = new Point(50, 187);
                iSkillLocs[14] = new Point(173, 187);
                iSkillLocs[15] = new Point(50, 248);
                iSkillLocs[16] = new Point(112, 248);
                iSkillLocs[17] = new Point(112, 307);
                iSkillLocs[18] = new Point(173, 307);
                iSkillLocs[19] = new Point(50, 370);

                iSkillLocs[20] = new Point(50, 64);
                iSkillLocs[21] = new Point(50, 125);
                iSkillLocs[22] = new Point(173, 125);
                iSkillLocs[23] = new Point(50, 187);
                iSkillLocs[24] = new Point(173, 187);
                iSkillLocs[25] = new Point(112, 248);
                iSkillLocs[26] = new Point(50, 307);
                iSkillLocs[27] = new Point(112, 307);
                iSkillLocs[28] = new Point(50, 370);
                iSkillLocs[29] = new Point(112, 370);
            }
            case 6 -> {
//			cClass = "ass";
                iSkillLocs[0] = new Point(112, 64);
                iSkillLocs[1] = new Point(50, 125);
                iSkillLocs[2] = new Point(173, 125);
                iSkillLocs[3] = new Point(50, 187);
                iSkillLocs[4] = new Point(112, 187);
                iSkillLocs[5] = new Point(173, 248);
                iSkillLocs[6] = new Point(50, 307);
                iSkillLocs[7] = new Point(112, 307);
                iSkillLocs[8] = new Point(50, 370);
                iSkillLocs[9] = new Point(173, 370);

                iSkillLocs[10] = new Point(112, 64);
                iSkillLocs[11] = new Point(173, 64);
                iSkillLocs[12] = new Point(50, 125);
                iSkillLocs[13] = new Point(112, 187);
                iSkillLocs[14] = new Point(173, 187);
                iSkillLocs[15] = new Point(50, 248);
                iSkillLocs[16] = new Point(112, 248);
                iSkillLocs[17] = new Point(173, 307);
                iSkillLocs[18] = new Point(50, 370);
                iSkillLocs[19] = new Point(112, 370);

                iSkillLocs[20] = new Point(112, 64);
                iSkillLocs[21] = new Point(173, 64);
                iSkillLocs[22] = new Point(50, 125);
                iSkillLocs[23] = new Point(173, 125);
                iSkillLocs[24] = new Point(112, 187);
                iSkillLocs[25] = new Point(50, 248);
                iSkillLocs[26] = new Point(173, 248);
                iSkillLocs[27] = new Point(50, 307);
                iSkillLocs[28] = new Point(173, 307);
                iSkillLocs[29] = new Point(112, 370);
            }
            default -> {
                // D2RMMMDKV3 新职业 (Warlock 等) 的占位布局：3 列 × 10 行均匀网格
                for (int i = 0; i < 30; i++) {
                    int col = i % 3;
                    int row = i / 3;
                    iSkillLocs[i] = new Point(50 + col * 62, 64 + row * 34);
                }
            }
        }
        return iSkillLocs;
    }

    public String getLocation() {
        return iLocation;
    }

    @Override
    public String toString() {
        return iDisplay;
    }

}
