/*******************************************************************************
 *
 * Copyright 2007 Randall
 *
 * This file is part of gomule.
 *
 * gomule is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * gomlue; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 *
 ******************************************************************************/
package gomule.d2x;

import gomule.gui.D2ItemListAdapter;
import gomule.item.D2Item;
import gomule.util.D2Backup;
import gomule.util.D2BitReader;
import gomule.util.D2Project;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2Stash extends D2ItemListAdapter {
    //    private String		iFileName;
    private ArrayList<D2Item> iItems;

    private D2BitReader iBR;
    private boolean iHC;
    private boolean iSC;

    private int iCharLvl = 75; // default char lvl for properties

    private File lFile;

//    private int iItemlistStart;
//    private int iItemlistEnd;

    public D2Stash(String pFileName) throws Exception {
        super(pFileName);
        if (iFileName == null || !iFileName.toLowerCase().endsWith(".d2x")) {
            throw new Exception("Incorrect Stash file name");
        }
        iItems = new ArrayList<D2Item>();

        lFile = new File(iFileName);

        iSC = lFile.getName().toLowerCase().startsWith("sc_");
        iHC = lFile.getName().toLowerCase().startsWith("hc_");

        if (!iSC && !iHC) {
            iSC = true;
            iHC = true;
        }

        iBR = new D2BitReader(iFileName);

        if (!iBR.isNewFile()) {
            iBR.set_byte_pos(0);
            byte lBytes[] = iBR.get_bytes(D2XOffsets.MAGIC_LENGTH);
            String lStart = new String(lBytes);
            if (D2XOffsets.MAGIC_STRING.equals(lStart)) {
                readAtmaItems();
            }
            // clear status
            setModified(false);
        } else {
            // set status (for first save)
            setModified(true);
        }
    }

    public String getFilename() {
        return iFileName;
    }

    public boolean isHC() {
        return iHC;
    }

    public boolean isSC() {
        return iSC;
    }

    public ArrayList<D2Item> getItemList() {
        return iItems;
    }

    public void addItem(D2Item pItem) {
        if (pItem != null) {
            iItems.add(pItem);
            pItem.setCharLvl(iCharLvl);
            setModified(true);
        }
    }

    public boolean containsItem(D2Item pItem) {
        return iItems.contains(pItem);
    }

    public void removeItem(D2Item pItem) {
        iItems.remove(pItem);
        setModified(true);
    }

    public ArrayList<D2Item> removeAllItems() {
        ArrayList<D2Item> lReturn = new ArrayList<D2Item>();
        lReturn.addAll(iItems);

        iItems.clear();
        setModified(true);

        return lReturn;
    }


    public int getNrItems() {
        return iItems.size();
    }

    private void readAtmaItems() throws Exception {

        iBR.set_byte_pos(D2XOffsets.CHECKSUM);
        long lOriginal = iBR.read(D2XOffsets.CHECKSUM_BITS);

        long lCalculated = calculateAtmaCheckSum();

        if (lOriginal == lCalculated) {
            iBR.set_byte_pos(D2XOffsets.NUM_ITEMS);

            long lNumItems = iBR.read(D2XOffsets.NUM_ITEMS_BITS);

            long lVersionNr = iBR.read(D2XOffsets.VERSION_BITS);

            if (D2XOffsets.isValidVersion(lVersionNr)) {
                // D2R 1.5+ 使用与 .d2s / .d2i 相同的 tail-bit 扩展格式
                // 必须在构造 D2Item 之前设置版本号，否则按 LoD 旧格式解析导致位流错位
                D2Item.sSaveVersion = 0x69;
                readItems(lNumItems);
            } else {
                throw new Exception("Stash Version Incorrect!");
            }
        }
    }

    private long calculateAtmaCheckSum() {
        long lCheckSum;
        lCheckSum = 0;

        iBR.set_byte_pos(0);
        // calculate a new checksum (bytes CHECKSUM..CHECKSUM_END are treated as 0)
        for (int i = 0; i < iBR.get_length(); i++) {
            long lByte = iBR.read(8);
            if (i >= D2XOffsets.CHECKSUM && i <= D2XOffsets.CHECKSUM_END) {
                lByte = 0;
            }

            long upshift = lCheckSum << 33 >>> 32;
            long add = lByte + ((lCheckSum >>> 31) == 1 ? 1 : 0);
            lCheckSum = upshift + add;
        }

//		System.err.println("Test " + lOriginal + " - " + lCheckSum + " = " + (lOriginal == lCheckSum) );
        return lCheckSum;
    }

    private void readItems(long pNumItems) throws Exception {
        iBR.set_byte_pos(D2XOffsets.ITEMS_START);
        for (int i = 0; i < pNumItems; i++) {
            D2Item lItem = new D2Item(iFileName, iBR, iCharLvl);
            iItems.add(lItem);
        }
    }

    public void saveInternal(D2Project pProject) {
        // backup file
        D2Backup.backup(pProject, iFileName, iBR);

        int size = 0;
        for (int i = 0; i < iItems.size(); i++)
            size += ((D2Item) iItems.get(i)).get_bytes().length;
        byte[] newbytes = new byte[size + D2XOffsets.HEADER_SIZE];
        newbytes[D2XOffsets.MAGIC]   = 'D';
        newbytes[D2XOffsets.MAGIC+1] = '2';
        newbytes[D2XOffsets.MAGIC+2] = 'X';
        int pos = D2XOffsets.ITEMS_START;
        for (int i = 0; i < iItems.size(); i++) {
            byte[] item_bytes = ((D2Item) iItems.get(i)).get_bytes();
            for (int j = 0; j < item_bytes.length; j++)
                newbytes[pos++] = item_bytes[j];
        }

        iBR.setBytes(newbytes);

        iBR.set_byte_pos(D2XOffsets.NUM_ITEMS);
        iBR.write(iItems.size(), D2XOffsets.NUM_ITEMS_BITS);
        iBR.write(D2XOffsets.VERSION_D2R, D2XOffsets.VERSION_BITS); // version
//        iBR.replace_bytes(11, iBR.get_length(), newbytes);

        long lCheckSum1 = calculateAtmaCheckSum();

        iBR.set_byte_pos(D2XOffsets.CHECKSUM);
        iBR.write(lCheckSum1, D2XOffsets.CHECKSUM_BITS);

        iBR.set_byte_pos(D2XOffsets.CHECKSUM);
        long lCheckSum2 = iBR.read(D2XOffsets.CHECKSUM_BITS);

//        long lCheckSum3 = calculateGoMuleCheckSum();
//        System.err.println("CheckSum after insert: " + lCheckSum3 );

        if (lCheckSum1 == lCheckSum2) {
            iBR.save();
            setModified(false);
        } else {
            System.err.println("Incorrect CheckSum");
        }
    }

    public void fullDump(PrintWriter pWriter) {
        pWriter.println(iFileName);
        pWriter.println();
        if (iItems != null) {
            for (int i = 0; i < iItems.size(); i++) {
                D2Item lItem = (D2Item) iItems.get(i);
                lItem.toWriter(pWriter);
            }
        }
        pWriter.println("Finished: " + iFileName);
        pWriter.println();
    }

    public String getFileNameEnd() {
        return lFile.getName();
    }

}
