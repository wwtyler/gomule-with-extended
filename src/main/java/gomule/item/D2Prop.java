/*******************************************************************************
 *
 * Copyright 2009 Silospen
 *
 * This file is part of gomule.
 *
 * gomule is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with gomlue; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 ******************************************************************************/

package gomule.item;

import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.singletonList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

import gomule.D2Files;
import gomule.util.D2Log;
import randall.d2files.D2TxtFile;
import randall.d2files.D2TxtFileItemProperties;

public class D2Prop {

    private int[] pVals;
    private int pNum;
    private int funcN;
    private int qFlag;
    private boolean opApplied;
    // Quality flag is there to seperate different types of items
    // 0 = ordinary item
    // 1 = Jewels
    // 2 = Set 2 items
    // 3 = Set 3 items
    // 4 = Set 4 items
    // 5 = Set 5 items
    // 6 = Set ? Items
    // 7 = Rune/Gem weapons
    // 8 = Rune/Gem armor
    // 9 = Rune/Gem shields
    // 12 = Set2Activated
    // 13 = Set3Activated
    // 14 = Set4Activated
    // 15 = Set5Activated
    // 16 = Set?Activated

    // 22 = Set2DeActivated
    // 23 = Set3DeActivated
    // 24 = Set4DeActivated
    // 25 = Set5DeActivated
    // 26 = FULLSetDeActivated
    // 32 = Set2Activated
    // 33 = Set3Activated
    // 34 = Set4Activated
    // 35 = Set5Activated
    // 36 = FULLSetActivated


    public D2Prop(int pNum, int[] pVals, int qFlag) {

        this.pNum = pNum;
        this.pVals = pVals;
        this.qFlag = qFlag;
    }

    public D2Prop(D2Prop newProp) {
        this.pNum = newProp.getPNum();
        this.pVals = (int[]) newProp.getPVals().clone();
        this.qFlag = 0;
    }


    public D2Prop(int pNum, int[] pVals, int qFlag, boolean opApplied, int funcN) {


        this.pNum = pNum;
        this.pVals = pVals;
        this.qFlag = qFlag;
        this.opApplied = opApplied;
        this.funcN = funcN;
    }

    public int getQFlag() {
        return qFlag;
    }

    public void setQFlag(int newQ) {
        qFlag = newQ;
    }

    public int[] getPVals() {

        return pVals;
    }

    public void setPVals(int[] pVals) {
        this.pVals = pVals;
    }

    public int getPNum() {

        return pNum;
    }

    public void setPNum(int pNum) {
        this.pNum = pNum;
    }

    public int getFuncN() {
        return funcN;
    }

    public void setFuncN(int funcN) {
        this.funcN = funcN;
    }

    public void modifyVals(int funcN, int[] pVals) {

        this.funcN = funcN;
        this.pVals = pVals;

    }

    public String generateDisplay(int qFlag, int cLvl) {

        if (this.qFlag != qFlag) {
            return null;
        }

        D2TxtFileItemProperties itemStatCostRow = D2TxtFile.ITEM_STAT_COST.getRow(pNum);
        String descstrpos = itemStatCostRow.get("descstrpos");
        String oString = isNullOrEmpty(descstrpos) ? null
                : D2Files.getInstance().getTranslations().getTranslation(descstrpos);
        // FUNCTION 0 means that you should use the txt files to find the print function to use.
        // Otherwise, it should be a case of looking for custom funcs
        if (funcN == 0) {

            if (itemStatCostRow.get("descfunc") != null
                    && !itemStatCostRow.get("descfunc").equals("")) {
                funcN = Integer.parseInt(itemStatCostRow.get("descfunc"));
            }
        }

        int dispLoc = 1;
        try {
            dispLoc = Integer.parseInt(itemStatCostRow.get("descval"));
        } catch (NumberFormatException e) {
            // leave dispLoc as = 1
        }

        // item_elemskill family: pVals[0]=param (element-type selector), pVals[1]=actual bonus.
        // Checked by Stat name so the fix is immune to ID changes caused by mod install order.
        // Covers: item_elemskill (126) and item_elemskill{fire/cold/light/poison/magic} (IDs vary
        // by mod).
        String statName = itemStatCostRow.get("Stat");
        if (statName != null && statName.startsWith("item_elemskill") && pVals.length > 1) {
            pVals[0] = pVals[1];
        }

        if (null != statName) // Max Durability
            switch (statName) {
                case "maxdurability" -> {
                    oString = "Maximum Durability";
                    funcN = 1;
                }
                case "item_levelreq" -> {
                    oString = "Required Level";
                    funcN = 1;
                    dispLoc = 2;
                }
                case "questitemdifficulty" -> funcN = 40;
                case "mana", "manarecovery" -> {
                    oString = "Replenishes Mana";
                    funcN = 2;
                    dispLoc = 2;
                }
                case "hitpoints" -> {
                    oString = "Replenishes Health";
                    funcN = 2;
                    dispLoc = 2;
                }
                case "item_howl" -> dispLoc = 2;
                default -> {
                }
            }

        // Self-contained D2R colour-coded template (e.g. MDK "ÿcV%+d 力量ÿc0"):
        // substitute the value directly into the template instead of prepending externally,
        // which would leave the value outside the colour span and %+d/%i as literal text.
        if (isSelfContainedTemplate(oString)) {
            switch (funcN) {
                case 1, 2, 3, 4, 6, 7, 8, 9, 12, 19, 29 -> {
                    return applyD2Template(oString, pVals[0]);
                }
                case 5, 10 -> {
                    return applyD2Template(oString, (pVals[0] * 100) / 128);
                }
                case 20, 21 -> {
                    return applyD2Template(oString, pVals[0] * -1);
                }
            }
        }

        switch (funcN) {

            case 1 -> {
                return switch (dispLoc) {
                    case 1 -> {
                        if (oString != null && oString.contains("%d")) {
                            yield oString.replaceAll("%d", Integer.toString(pVals[0]));
                        } else if (pVals[0] > -1) {
                            yield "+" + pVals[0] + " " + oString;
                        } else {
                            yield pVals[0] + " " + oString;
                        }
                    }
                    case 2 -> pVals[0] > -1 ? oString + " +" + pVals[0] : oString + " " + pVals[0];
                    default -> oString;
                };
            }
            case 2 -> {
                return switch (dispLoc) {
                    case 1 -> pVals[0] + "% " + oString;
                    case 2 -> oString + " " + pVals[0] + "%";
                    default -> oString;
                };
            }
            case 3 -> {
                return switch (dispLoc) {
                    case 1 -> pVals[0] + " " + oString;
                    case 2 -> oString + " " + pVals[0];
                    default -> oString;
                };
            }
            case 4 -> {
                return switch (dispLoc) {
                    case 1 -> pVals[0] > -1 ? "+" + pVals[0] + "% " + oString
                            : pVals[0] + "% " + oString;
                    case 2 -> pVals[0] > -1 ? oString + " +" + pVals[0] + "%"
                            : oString + " " + pVals[0] + "%";
                    default -> oString;
                };
            }
            case 5 -> {
                if (oString == null)
                    return null;
                return oString.formatted(((pVals[0] * 100) / 128));
            }
            case 6 -> {
                return switch (dispLoc) {
                    case 1 -> "+" + pVals[0] + " " + oString + " " + D2Files.getInstance()
                            .getTranslations().getTranslation(itemStatCostRow.get("descstr2"));
                    case 2 -> oString + " " + D2Files.getInstance().getTranslations()
                            .getTranslation(itemStatCostRow.get("descstr2")) + " +" + pVals[0];
                    default -> oString;
                };
            }
            case 7 -> {
                return switch (dispLoc) {
                    case 1 -> pVals[0] + "% " + oString + " " + D2Files.getInstance()
                            .getTranslations().getTranslation(itemStatCostRow.get("descstr2"));
                    case 2 -> oString + " " + D2Files.getInstance().getTranslations()
                            .getTranslation(itemStatCostRow.get("descstr2")) + pVals[0] + "%";
                    default -> oString;
                };
            }
            case 8 -> {
                return switch (dispLoc) {
                    case 1 -> "+" + pVals[0] + "% " + oString + " " + D2Files.getInstance()
                            .getTranslations().getTranslation(itemStatCostRow.get("descstr2"));
                    case 2 -> oString + " " + D2Files.getInstance().getTranslations()
                            .getTranslation(itemStatCostRow.get("descstr2")) + " +" + pVals[0]
                            + "%";
                    default -> oString;
                };
            }
            case 9 -> {
                return switch (dispLoc) {
                    case 1 -> pVals[0] + " " + oString + " " + D2Files.getInstance()
                            .getTranslations().getTranslation(itemStatCostRow.get("descstr2"));
                    case 2 -> oString + " " + D2Files.getInstance().getTranslations()
                            .getTranslation(itemStatCostRow.get("descstr2")) + " " + pVals[0];
                    default -> oString;
                };
            }
            case 10 -> {
                return switch (dispLoc) {
                    case 1 -> (pVals[0] * 100) / 128 + "% " + oString + " " + D2Files.getInstance()
                            .getTranslations().getTranslation(itemStatCostRow.get("descstr2"));
                    case 2 -> oString + " "
                            + D2Files.getInstance().getTranslations()
                                    .getTranslation(itemStatCostRow.get("descstr2"))
                            + (pVals[0] * 100) / 128 + "%";
                    default -> oString;
                };
            }
            case 11 -> {
                return "Repairs 1 Durability in " + (100 / pVals[0]) + " Seconds";
            }
            case 12 -> {
                return switch (dispLoc) {
                    case 1 -> "+" + pVals[0] + " " + oString;
                    case 2 -> oString + " +" + pVals[0];
                    default -> oString;
                };
            }
            case 13 -> {
                String charName13 = D2TxtFile.getCharacterCode(pVals[0]);
                randall.d2files.D2TxtFileItemProperties charRow13 =
                        D2TxtFile.CHARSTATS.searchColumns("class", charName13);
                if (charRow13 != null) {
                    String allSkillsKey = charRow13.get("StrAllSkills");
                    if (allSkillsKey != null && !allSkillsKey.isEmpty()) {
                        String allSkillsStr = D2Files.getInstance().getTranslations()
                                .getTranslation(allSkillsKey);
                        if (isSelfContainedTemplate(allSkillsStr))
                            return applyD2Template(allSkillsStr, pVals[1]);
                    }
                }
                return "+" + pVals[1] + " to " + charName13 + " Skill Levels";
            }
            case 14 -> {
                String tabKey14 = getSkillTreeTranslationKey(pVals[0]);
                if (tabKey14 != null) {
                    String tabStr14 =
                            D2Files.getInstance().getTranslations().getTranslation(tabKey14);
                    if (isSelfContainedTemplate(tabStr14))
                        return applyD2Template(tabStr14, pVals[1]);
                }
                return "+" + pVals[1] + " to " + getSkillTree(pVals[0]);
            }
            case 15 -> {
                if (oString == null)
                    return null;
                oString = oString.replaceFirst("%d%", Integer.toString(pVals[2]));
                oString = oString.replaceAll("%d", Integer.toString(pVals[0]));
                D2TxtFileItemProperties lRow = D2TxtFile.SKILLS.getRow(pVals[1]);
                String lText = lRow.get("skilldesc");
                String lString = null;
                if (!"".equals(lText)) {
                    lString = D2Files.getInstance().getTranslations().getTranslation(
                            D2TxtFile.SKILL_DESC.searchColumns("skilldesc", lText).get("str name"));
                }
                if (lString == null) {
                    lString = "Unknown";
                }
                return oString.replaceAll("%s", lString);
            }
            case 16 -> {
                if (oString == null)
                    return null;
                oString = oString.replaceAll("%d", Integer.toString(pVals[1]));
                return oString
                        .replaceAll("%s",
                                D2Files.getInstance().getTranslations()
                                        .getTranslation(D2TxtFile.SKILL_DESC
                                                .searchColumns("skilldesc",
                                                        D2TxtFile.SKILLS.getRow(pVals[0])
                                                                .get("skilldesc"))
                                                .get("str name")));
            }
            case 17 -> {
                return "By time!? Oh shi....";
            }
            case 18 -> {
                return "By time!? Oh shi....";
            }
            case 19, 29 -> {
                List<D2TxtFileItemProperties> matchingPropsRecords =
                        ((ArrayList<D2TxtFileItemProperties>) D2TxtFile.PROPS
                                .searchColumnsMultipleHits("stat1", itemStatCostRow.get("Stat")))
                                        .stream().filter(it -> it.get("stat2").isEmpty())
                                        .collect(Collectors.toList());
                if (matchingPropsRecords.isEmpty()) {
                    // Locale-independent fallback: map by ItemStatCost.Stat (not by translated
                    // oString).
                    // The English oString.equals(...) chain previously here failed on zhCN /
                    // colour-coded
                    // strings such as 'ÿcV%+d 最大伤害ÿc0', causing "Unknown property" warnings.
                    String statKey = itemStatCostRow.get("Stat");
                    String propCode = switch (statKey) {
                        case "indestructible" -> "indestruct";
                        case "item_maxdamage_percent" -> "dmg%";
                        case "maxdamage" -> "dmg-max";
                        case "mindamage" -> "dmg-min";
                        default -> null;
                    };
                    if (propCode != null) {
                        matchingPropsRecords =
                                singletonList(D2TxtFile.PROPS.searchColumns("code", propCode));
                    } else {
                        D2Log.warn("D2Prop",
                                "Unknown property: no PROPS row matched stat='%s' (pNum=%d funcN=%d qFlag=%d dispLoc=%d oString='%s' pVals=%s descfunc='%s' descval='%s' descstrpos='%s')",
                                itemStatCostRow.get("Stat"), pNum, funcN, qFlag, dispLoc, oString,
                                Arrays.toString(pVals), itemStatCostRow.get("descfunc"),
                                itemStatCostRow.get("descval"), itemStatCostRow.get("descstrpos"));
                        return "Unknown property [stat=" + itemStatCostRow.get("Stat") + " pNum="
                                + pNum + "]";
                    }
                }
                D2TxtFileItemProperties o = matchingPropsRecords.getFirst();
                String tooltip = o.get("*Tooltip");
                String value = tooltip.replace("#", String.valueOf(pVals[0]));
                if (pVals[0] < 0) {
                    return value.replace("+", "");
                } else {
                    return value;
                }
            }
            case 20 -> {
                return switch (dispLoc) {
                    case 1 -> (pVals[0] * -1) + "% " + oString;
                    case 2 -> oString + " " + (pVals[0] * -1) + "%";
                    default -> oString;
                };
            }
            case 21 -> {
                return switch (dispLoc) {
                    case 1 -> (pVals[0] * -1) + " " + oString;
                    case 2 -> oString + " " + (pVals[0] * -1);
                    default -> oString;
                };
            }

            case 23 -> {
                String monsterName23 = D2Files.getInstance().getTranslations()
                        .getTranslation(D2TxtFile.MONSTATS.getRow(pVals[0]).get("NameStr"));
                // TylerPack 等自包含 D2R 模板使用位置占位符 %0 (=chance) / %1 (=monster name)
                // 与 %% 转义。例如 'Moditemreanimas' = "ÿcQ%0%% 复活：%1ÿc0"。
                // 旧的 "pVals[1] + % + template + monsterName" 拼法对自包含模板会留下 %0%% 与 %1 字面文本。
                if (oString != null && (oString.contains("%0") || oString.contains("%1"))) {
                    String r = oString.replace("%0", String.valueOf(pVals[1]))
                            .replace("%1", stripD2rColorCodes(monsterName23)).replace("%%", "%");
                    return r;
                }
                return pVals[1] + "% " + oString + " " + monsterName23;
            }
            case 24 -> {
                if (oString == null)
                    return null;
                oString = oString.replaceFirst("%d", Integer.toString(pVals[2]));
                oString = oString.replaceAll("%d", Integer.toString(pVals[3]));
                return "Level " + pVals[0] + " "
                        + D2Files.getInstance().getTranslations()
                                .getTranslation(D2TxtFile.SKILL_DESC
                                        .searchColumns("skilldesc",
                                                D2TxtFile.SKILLS.getRow(pVals[1]).get("skilldesc"))
                                        .get("str name"))
                        + " " + oString;
            }
            case 27 -> {
                String skillName27 = D2Files.getInstance().getTranslations()
                        .getTranslation(D2TxtFile.SKILL_DESC
                                .searchColumns("skilldesc",
                                        D2TxtFile.SKILLS.getRow(pVals[0]).get("skilldesc"))
                                .get("str name"));
                String charclass27 = D2TxtFile.SKILLS.getRow(pVals[0]).get("charclass");
                String classKey27 = (charclass27.charAt(0) + "").toUpperCase()
                        + charclass27.substring(1) + "Only";
                String classOnly27 =
                        D2Files.getInstance().getTranslations().getTranslation(classKey27);
                if (isSelfContainedTemplate(oString)) {
                    return applyD2TemplateWithStrings(oString, pVals[1],
                            stripD2rColorCodes(skillName27), stripD2rColorCodes(classOnly27));
                }
                return "+" + pVals[1] + " to " + skillName27 + " " + classOnly27;
            }
            case 28 -> {
                String skillName28 = D2Files.getInstance().getTranslations()
                        .getTranslation(D2TxtFile.SKILL_DESC
                                .searchColumns("skilldesc",
                                        D2TxtFile.SKILLS.getRow(pVals[0]).get("skilldesc"))
                                .get("str name"));
                if (isSelfContainedTemplate(oString)) {
                    return applyD2TemplateWithStrings(oString, pVals[1],
                            stripD2rColorCodes(skillName28));
                }
                return "+" + pVals[1] + " to " + skillName28;
            }
            // UNOFFICIAL PROPERTIES

            // Enhanced Damage
            case 30 -> {
                String s30 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModEnhancedDamage");
                if (isSelfContainedTemplate(s30))
                    return applyD2Template(s30, pVals[0]);
                return pVals[0] + "% Enhanced Damage";
            }
            case 31 -> {
                String s31 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModMinDamageRange");
                if (isSelfContainedTemplate(s31))
                    return applyD2TemplateMulti(s31, pVals[0], pVals[1]);
                return "Adds " + pVals[0] + " - " + pVals[1] + " Damage";
            }
            case 32 -> {
                String s32 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModFireDamageRange");
                if (isSelfContainedTemplate(s32))
                    return applyD2TemplateMulti(s32, pVals[0], pVals[1]);
                return "Adds " + pVals[0] + " - " + pVals[1] + " Fire Damage";
            }
            case 33 -> {
                String s33 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModLightningDamageRange");
                if (isSelfContainedTemplate(s33))
                    return applyD2TemplateMulti(s33, pVals[0], pVals[1]);
                return "Adds " + pVals[0] + " - " + pVals[1] + " Lightning Damage";
            }
            case 34 -> {
                String s34 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModMagicDamageRange");
                if (isSelfContainedTemplate(s34))
                    return applyD2TemplateMulti(s34, pVals[0], pVals[1]);
                return "Adds " + pVals[0] + " - " + pVals[1] + " Magic Damage";
            }
            case 35 -> {
                // MDK string strModColdDamageRange does not include duration; display without
                // frames
                String s35 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModColdDamageRange");
                if (isSelfContainedTemplate(s35)) {
                    return pVals[0] == pVals[1] ? applyD2TemplateMulti(s35, pVals[0], pVals[0])
                            : applyD2TemplateMulti(s35, pVals[0], pVals[1]);
                }
                if (pVals[0] == pVals[1]) {
                    return "Adds " + pVals[0] + " Cold Damage Over "
                            + Math.round((double) pVals[2] / 25.0) + " Secs (" + pVals[2]
                            + " Frames)";
                }
                return "Adds " + pVals[0] + " - " + pVals[1] + " Cold Damage Over "
                        + Math.round((double) pVals[2] / 25.0) + " Secs (" + pVals[2] + " Frames)";
            }

            case 36 -> {
                // Compute actual poison damage and duration, then apply to MDK template
                int poisMin, poisMax, durationSecs;
                if (pVals.length == 4) {
                    poisMin = (int) Math
                            .round(pVals[0] * ((double) pVals[2] / (double) pVals[3]) / 256);
                    poisMax = (int) Math
                            .round(pVals[1] * ((double) pVals[2] / (double) pVals[3]) / 256);
                    durationSecs = (int) Math.floor(((double) pVals[2] / (double) pVals[3]) / 25.0);
                } else {
                    poisMin = (int) Math.round(pVals[0] * (double) pVals[2] / 256);
                    poisMax = (int) Math.round(pVals[1] * (double) pVals[2] / 256);
                    durationSecs = (int) Math.floor((double) pVals[2] / 25.0);
                }
                String s36key =
                        (poisMin == poisMax) ? "strModPoisonDamage" : "strModPoisonDamageRange";
                String s36 = D2Files.getInstance().getTranslations().getTranslation(s36key);
                if (isSelfContainedTemplate(s36)) {
                    return poisMin == poisMax ? applyD2TemplateMulti(s36, poisMin, durationSecs)
                            : applyD2TemplateMulti(s36, poisMin, poisMax, durationSecs);
                }
                if (poisMin == poisMax) {
                    return "Adds " + poisMin + " Poison Damage Over " + durationSecs + " Secs";
                }
                return "Adds " + poisMin + " - " + poisMax + " Poison Damage Over " + durationSecs
                        + " Secs";
            }
            case 37 -> {
                String s37 = D2Files.getInstance().getTranslations()
                        .getTranslation("strModAllResistances");
                if (isSelfContainedTemplate(s37))
                    return applyD2Template(s37, pVals[0]);
                return "All Resistances +" + pVals[0];
            }
            case 38 -> {
                String s38 = D2Files.getInstance().getTranslations().getTranslation("allattrib");
                if (isSelfContainedTemplate(s38))
                    return applyD2Template(s38, pVals[0]);
                return "全属性 +" + pVals[0];
            }
            case 39 -> {
                return "Level " + pVals[1] + " " + D2TxtFile.getCharacterCode(pVals[0]);
            }
            case 40 -> {
                switch (pVals[0]) {
                    case 0 -> {
                        return "Found In Normal Difficulty";
                    }
                    case 1 -> {
                        return "Found In Nightmare Difficulty";
                    }
                    case 2 -> {
                        return "Found In Hell Difficulty";
                    }
                }
            }
        }

        // funcN=0 + descfunc 真为空：mod 故意把该 stat 的 desc 留空（D2R op-stat parent 模式：
        // parent stat 隐藏，由 op stat 派生属性负责显示）。例如 TylerPack 的 stat 126
        // (item_elemskill) descfunc 已清空。这种情况是合法的 mod 配置，不应打 warning，
        // 也不应在 tooltip 里渲染 "Unrecognized property: 126" 这样的文字 —— 直接返回空串，
        // D2PropCollection.generateDisplay 会跳过空串不输出 <br>。
        String descfuncRaw = itemStatCostRow.get("descfunc");
        if (funcN == 0 && (descfuncRaw == null || descfuncRaw.isBlank())) {
            return "";
        }

        D2Log.warn("D2Prop",
                "Unrecognized property: no switch case for funcN=%d (pNum=%d qFlag=%d oString='%s' pVals=%s stat='%s' descfunc='%s' descval='%s')",
                funcN, pNum, qFlag, oString, Arrays.toString(pVals), itemStatCostRow.get("Stat"),
                itemStatCostRow.get("descfunc"), itemStatCostRow.get("descval"));
        return "Unrecognized property: " + this.pNum;
    }

    /**
     * Returns true if the string is a self-contained D2R colour-coded display template (contains
     * the ÿc prefix U+00FF followed by 'c').
     */
    private static boolean isSelfContainedTemplate(String s) {
        return s != null && s.indexOf('\u00FF') >= 0;
    }

    /**
     * Substitutes a single integer value into a D2R lng format string.
     * <ul>
     * <li>{@code %+d} → {@code +val} or {@code val} (for negative, sign is included)</li>
     * <li>{@code %d}, {@code %i} → {@code val}</li>
     * <li>{@code %%} → literal {@code %}</li>
     * </ul>
     */
    /**
     * Strips D2R color codes (ÿcX sequences) from a string, returning plain text.
     */
    private static String stripD2rColorCodes(String s) {
        if (s == null)
            return null;
        int i = s.indexOf('\u00FF');
        if (i < 0)
            return s;
        StringBuilder sb = new StringBuilder(s.length());
        int pos = 0;
        while (pos < s.length()) {
            char ch = s.charAt(pos);
            if (ch == '\u00FF' && pos + 2 < s.length() && s.charAt(pos + 1) == 'c') {
                pos += 3;
            } else {
                sb.append(ch);
                pos++;
            }
        }
        return sb.toString();
    }

    /**
     * Applies a D2R template substituting one integer value (for %+d/%d/%i) and then string values
     * (for %s) in order.
     */
    private static String applyD2TemplateWithStrings(String template, int numVal,
            String... strVals) {
        String result = applyD2Template(template, numVal);
        for (String sv : strVals) {
            int idx = result.indexOf("%s");
            if (idx < 0)
                break;
            result = result.substring(0, idx) + (sv != null ? sv : "") + result.substring(idx + 2);
        }
        return result;
    }

    private static String applyD2Template(String template, int val) {
        return template.replace("%+d", val >= 0 ? "+" + val : String.valueOf(val))
                .replace("%d", String.valueOf(val)).replace("%i", String.valueOf(val))
                .replace("%%", "%");
    }

    /**
     * Substitutes multiple values sequentially into a D2R lng format string. Each {@code %d}
     * placeholder (not {@code %+d}) is replaced in order with the next value. {@code %%} is
     * converted to a literal {@code %} after all substitutions.
     */
    private static String applyD2TemplateMulti(String template, int... vals) {
        StringBuilder sb = new StringBuilder(template);
        int searchFrom = 0;
        for (int v : vals) {
            // Try %+d first (only if present at current position)
            int plusIdx = sb.indexOf("%+d", searchFrom);
            int plainIdx = sb.indexOf("%d", searchFrom);
            int idx;
            boolean isPlus;
            if (plusIdx >= 0 && (plainIdx < 0 || plusIdx <= plainIdx)) {
                idx = plusIdx;
                isPlus = true;
            } else if (plainIdx >= 0) {
                idx = plainIdx;
                isPlus = false;
            } else {
                break;
            }
            String replacement =
                    isPlus ? (v >= 0 ? "+" + v : String.valueOf(v)) : String.valueOf(v);
            int len = isPlus ? 3 : 2;
            sb.replace(idx, idx + len, replacement);
            searchFrom = idx + replacement.length();
        }
        // Replace %% → %
        int pct;
        while ((pct = sb.indexOf("%%")) >= 0) {
            sb.replace(pct, pct + 2, "%");
        }
        return sb.toString();
    }

    public void applyOp(int cLvl) {

        if (D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("op").equals(""))
            return;
        if (opApplied)
            return;

        int op = Integer.parseInt(D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("op"));

        switch (op) {

            case 2, 4, 5 -> {
                if (D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("op base").equals("level")) {

                    pVals[0] = (int) Math.floor(
                            ((double) (pVals[0] * cLvl)) / ((double) (Math.pow(2, Integer.parseInt(
                                    D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("op param"))))));
                }
            }
        }
        opApplied = true;
    }

    private static String getSkillTreeTranslationKey(int n) {
        return switch (n) {
            case 0 -> "StrSklTabItem3"; // Amazon: Bow and Crossbow
            case 1 -> "StrSklTabItem2"; // Amazon: Passive and Magic
            case 2 -> "StrSklTabItem1"; // Amazon: Javelin and Spear
            case 8 -> "StrSklTabItem15"; // Sorceress: Fire
            case 9 -> "StrSklTabItem14"; // Sorceress: Lightning
            case 10 -> "StrSklTabItem13"; // Sorceress: Cold
            case 16 -> "StrSklTabItem8"; // Necromancer: Curses
            case 17 -> "StrSklTabItem7"; // Necromancer: Poison and Bone
            case 18 -> "StrSklTabItem9"; // Necromancer: Summoning
            case 24 -> "StrSklTabItem6"; // Paladin: Combat Skills
            case 25 -> "StrSklTabItem5"; // Paladin: Offensive Auras
            case 26 -> "StrSklTabItem4"; // Paladin: Defensive Auras
            case 32 -> "StrSklTabItem11"; // Barbarian: Combat Skills
            case 33 -> "StrSklTabItem12"; // Barbarian: Masteries
            case 34 -> "StrSklTabItem10"; // Barbarian: Warcries
            case 40 -> "StrSklTabItem16"; // Druid: Summoning
            case 41 -> "StrSklTabItem17"; // Druid: Shape-shifting
            case 42 -> "StrSklTabItem18"; // Druid: Elemental
            case 48 -> "StrSklTabItem19"; // Assassin: Traps
            case 49 -> "StrSklTabItem20"; // Assassin: Shadow Discipline
            case 50 -> "StrSklTabItem21"; // Assassin: Martial Arts
            case 56 -> "StrSklTabItem22"; // Warlock : Eldritch
            case 57 -> "StrSklTabItem23"; // Warlock : Chaos
            case 58 -> "StrSklTabItem24"; // Warlock : Demon
            default -> null;
        };
    }

    public String getSkillTree(int lSkillNr) {

        return switch (lSkillNr) {
            case 0 -> "Bow and Crossbow Skills (Amazon Only)";
            case 1 -> "Passive and Magic Skills (Amazon Only)";
            case 2 -> "Javelin and Spear Skills (Amazon Only)";
            case 8 -> "Fire Skills (Sorceress Only)";
            case 9 -> "Lightning Skills (Sorceress Only)";
            case 10 -> "Cold Skills (Sorceress Only)";
            case 16 -> "Curses (Necromancer only)";
            case 17 -> "Poison and Bone Skills (Necromancer Only)";
            case 18 -> "Summoning Skills (Necromancer Only)";
            case 24 -> "Combat Skills (Paladin Only)";
            case 25 -> "Offensive Aura Skills (Paladin Only)";
            case 26 -> "Defensive Aura Skills (Paladin Only)";
            case 32 -> "Combat Skills (Barbarian Only)";
            case 33 -> "Masteries Skills (Barbarian Only)";
            case 34 -> "Warcry Skills (Barbarian Only)";
            case 40 -> "Summoning Skills (Druid Only)";
            case 41 -> "Shape-Shifting Skills (Druid Only)";
            case 42 -> "Elemental Skills (Druid Only)";
            case 48 -> "Trap Skills (Assassin Only)";
            case 49 -> "Shadow Discipline Skills (Assassin Only)";
            case 50 -> "Martial Art Skills (Assassin Only)";
            case 56 -> "Eldritch Skills (Warlock Only)";
            case 57 -> "Chaos Skills (Warlock Only)";
            case 58 -> "Demon Skills (Warlock Only)";
            default -> "Unknown Tree (P 188)";
        };

    }

    public void addPVals(int[] newVals) {

        // Poison length needs to keep track of the number of properties contributing to it.
        // Therefore, [2] becomes the counter.
        if (getPNum() == 59) {

            if (pVals.length < 2) {
                pVals = new int[] {pVals[0], pVals[0], 0};
            }
            if (newVals.length < 2) {
                newVals = new int[] {newVals[0], newVals[0], 0};
            }

            if (pVals[2] == 0) {
                pVals = new int[] {pVals[0], pVals[1], 1};
            }
            if (newVals[2] == 0) {
                newVals = new int[] {newVals[0], newVals[1], 1};
            }
        }

        int vLen = pVals.length;

        if (pVals.length > newVals.length) {
            vLen = newVals.length;
        }

        for (int z = 0; z < vLen; z++) {
            pVals[z] = pVals[z] + newVals[z];
        }

    }

    public int getDescPriority() {

        if (D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("descpriority").equals("")) {
            if (pNum == 183) {
                return 38;
            } else if (pNum == 184) {
                return 69;
            }
            return 0;
        } else {
            return Integer.parseInt(D2TxtFile.ITEM_STAT_COST.getRow(pNum).get("descpriority"));
        }
    }

    public void addCharMods(int[] outStats, ArrayList<D2Prop> plSkill, int cLvl, int op,
            int qFlagMarker) {

        // If it's 0 we only want standard properties (non set)
        if (qFlagMarker == 0) {
            if (qFlag != 0)
                return;
        } else {
            if (qFlag != 12 && qFlag != 13 && qFlag != 14 && qFlag != 15 && qFlag != 16)
                return;
        }

        if (!opApplied)
            applyOp(cLvl);

        /**
         * 0 Str 0 1 Str/lvl 220 2 En 1 3 En/lvl 222 4 Dex 2 5 Dex/lvl 221 6 Vit 3 7 Vit/lvl 223 8
         * Life 7 9 HP/lvl 216 10 Mana 9 11 Mana/Lvl 217 12 Stam 11 13 Stam/lvl 242 14 AR 19 15 AR/%
         * 119 16 AR/lvl 224 17 AR/%/Lvl 225 18 FR 39 19 LR 41 20 CR 43 21 PR 45 22 MF 80 23 MF/Lvl
         * 240 24 FRW 96 25 FCR 105 26 IAS 93 27 FHR 99 28 GF 79 29 GF/lvl 239 30 AllSkill 127 31
         * Block 20
         */


        switch (pNum) {

            case 0 -> outStats[0] = outStats[0] + (pVals[0] * op);
            case 1 -> outStats[2] = outStats[2] + (pVals[0] * op);
            case 2 -> outStats[4] = outStats[4] + (pVals[0] * op);
            case 3 -> outStats[6] = outStats[6] + (pVals[0] * op);
            case 7 -> outStats[8] = outStats[8] + (pVals[0] * op);
            case 9 -> outStats[10] = outStats[10] + (pVals[0] * op);
            case 11 -> outStats[12] = outStats[12] + (pVals[0] * op);
            case 19 -> outStats[14] = outStats[14] + (pVals[0] * op);
            case 20 -> outStats[30] = outStats[30] + (pVals[0] * op);
            case 39 -> outStats[18] = outStats[18] + (pVals[0] * op);
            case 41 -> outStats[19] = outStats[19] + (pVals[0] * op);
            case 43 -> outStats[20] = outStats[20] + (pVals[0] * op);
            case 45 -> outStats[21] = outStats[21] + (pVals[0] * op);
            case 79 -> outStats[28] = outStats[28] + (pVals[0] * op);
            case 80 -> outStats[22] = outStats[22] + (pVals[0] * op);
            case 93 -> outStats[26] = outStats[26] + (pVals[0] * op);
            case 96 -> outStats[24] = outStats[24] + (pVals[0] * op);
            case 99 -> outStats[27] = outStats[27] + (pVals[0] * op);
            case 105 -> outStats[25] = outStats[25] + (pVals[0] * op);
            case 119 -> outStats[15] = outStats[15] + (pVals[0] * op);
            case 216 -> outStats[9] = outStats[9] + (pVals[0] * op);
            case 217 -> outStats[11] = outStats[11] + (pVals[0] * op);
            case 220 -> outStats[1] = outStats[1] + (pVals[0] * op);
            case 221 -> outStats[5] = outStats[5] + (pVals[0] * op);
            case 222 -> outStats[3] = outStats[3] + (pVals[0] * op);
            case 223 -> outStats[7] = outStats[7] + (pVals[0] * op);
            case 224 -> outStats[16] = outStats[16] + (pVals[0] * op);
            case 225 -> outStats[17] = outStats[17] + (pVals[0] * op);
            case 239 -> outStats[29] = outStats[29] + (pVals[0] * op);
            case 240 -> outStats[23] = outStats[23] + (pVals[0] * op);
            case 242 -> outStats[13] = outStats[13] + (pVals[0] * op);
            // + SKILLS
            case 188, 126, 97, 107, 83, 127 -> {
                if (plSkill != null) {
                    if (op == 1) {
                        plSkill.add(this);
                    } else {
                        plSkill.remove(this);
                    }
                }
            }
        }
    }
}
