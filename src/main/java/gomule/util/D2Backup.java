/*
 * Created on 23-mei-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.util;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import gomule.gui.D2FileManager;
import randall.util.RandallUtil;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2Backup {
    public static void backup(D2Project pProject, String pFileName, D2BitReader pContent) {
        try {
            int lBackup = pProject.getBackup();
            if (lBackup == D2Project.BACKUP_NONE) {
                return;
            }

            File lFile = new File(pFileName);

            String lFileName = lFile.getName();
            String lPathName = lFile.getParent();

            GregorianCalendar lCalendar = new GregorianCalendar();
            String lExtra1 = switch (lBackup) {
                case D2Project.BACKUP_DAY -> "D"
                        + RandallUtil.fill(lCalendar.get(Calendar.YEAR), 4)
                        + "." + RandallUtil.fill(lCalendar.get(Calendar.MONTH) + 1, 2)
                        + "." + RandallUtil.fill(lCalendar.get(Calendar.DAY_OF_MONTH), 2);
                case D2Project.BACKUP_MONTH -> "M"
                        + RandallUtil.fill(lCalendar.get(Calendar.YEAR), 4)
                        + RandallUtil.fill(lCalendar.get(Calendar.MONTH) + 1, 2);
                default -> {
                    GregorianCalendar lWeek = new GregorianCalendar();

                    while (lWeek.get(Calendar.DAY_OF_WEEK) != lWeek.getFirstDayOfWeek()) {
                        lWeek.add(Calendar.DAY_OF_MONTH, -1);
                    }
                    yield "W"
                            + RandallUtil.fill(lWeek.get(Calendar.YEAR), 4)
                            + "." + RandallUtil.fill(lWeek.get(Calendar.MONTH) + 1, 2)
                            + "." + RandallUtil.fill(lWeek.get(Calendar.DAY_OF_MONTH), 2);
                }
            };

            String lExtra2 =
                    RandallUtil.fill(lCalendar.get(Calendar.YEAR), 4)
                            + "." + RandallUtil.fill(lCalendar.get(Calendar.MONTH) + 1, 2)
                            + "." + RandallUtil.fill(lCalendar.get(Calendar.DAY_OF_MONTH), 2)
                            + "-"
                            + RandallUtil.fill(lCalendar.get(Calendar.HOUR_OF_DAY), 2)
                            + "." + RandallUtil.fill(lCalendar.get(Calendar.MINUTE), 2)
                            + "." + RandallUtil.fill(lCalendar.get(Calendar.SECOND), 2);

            String lBackupDir = lPathName + File.separator + "GoMule.backup";
            String lBackupSubDir = lBackupDir + File.separator + lExtra1;

            String lNewFileName = lFileName.substring(0, lFileName.length() - 4) + "." + lExtra2 + lFileName.substring(lFileName.length() - 4) + ".org";

            String lBackupName = lBackupSubDir + File.separator + lNewFileName;

            RandallUtil.checkDir(lBackupSubDir);

            pContent.save(lBackupName);
        } catch (Exception pEx) {
            D2Log.error("D2Backup", pEx, "backup failed for %s", pFileName);
            D2FileManager.displayErrorDialog(pEx);
        }

//        if ( pContent.isNewFile() )
//        {
//            return;
//        }
//        int backup_max = -1;
//        for (int i = 0; i <= 9; i++)
//        {
//            if (new java.io.File(pFileName + "." + i).exists())
//                backup_max = i;
//            else
//                break;
//        }
//
//        // shift the backups down
//        // (backup 9 is overwritten, 0 comes free)
//        for (int i = backup_max; i >= 0; i--)
//        {
//            if (i < 9)
//            {
//                D2BitReader src = new D2BitReader(pFileName + "." + i);
//                src.save(pFileName + "." + (i + 1));
//            }
//        }
//        
//        // save the file to backup 0
//        pContent.save(pFileName + ".0");

    }
}
