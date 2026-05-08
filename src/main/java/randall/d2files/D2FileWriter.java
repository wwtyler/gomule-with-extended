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
package randall.d2files;


/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2FileWriter {
    //	private String	iFileName;
    protected byte iBuffer[];
    protected int iBufferLen = 0;

    private int iCounterPos = 0;
    private int iCounterBit = 0;

    public D2FileWriter() {
        iBuffer = new byte[100];
    }

    public byte[] getCurrentContent() {
        byte[] lReturn = new byte[iBufferLen];
        System.arraycopy(iBuffer, 0, lReturn, 0, iBufferLen);
        return lReturn;

    }

    public void increaseCounter(int pNrBits) {
        for (int i = 0; i < pNrBits; i++) {
            iCounterBit++;
            if (iCounterBit > 7) {
                iCounterBit = 0;
                iCounterPos++;
            }
        }
    }

    public int getCounterPos() {
        return iCounterPos;
    }

    public int getCounterBit() {
        return iCounterBit;
    }

    public void setCounter(int pCharPos, int pBitNr) {
        iCounterPos = pCharPos;
        iCounterBit = pBitNr;
    }

    public void setCounterBoolean(boolean pBit) {
        if (iCounterPos >= iBufferLen) {
            // increase virtual capacity
            iBufferLen++;
            if (iBufferLen >= iBuffer.length) {
                // increase real capacity
                byte lNewBuffer[] = new byte[iBuffer.length * 2];
                System.arraycopy(iBuffer, 0, lNewBuffer, 0, iBuffer.length);
                iBuffer = lNewBuffer;
            }
        }

        if (pBit) {
            int lNr = 1;
            if (iCounterBit > 0) {
                lNr = lNr << iCounterBit;
            }

            iBuffer[iCounterPos] = (byte) (iBuffer[iCounterPos] | lNr);
        } else {
            int lNr = switch (iCounterBit) {
                case 0 -> 0xFE;
                case 1 -> 0xFD;
                case 2 -> 0xFB;
                case 3 -> 0xF7;
                case 4 -> 0xEF;
                case 5 -> 0xDF;
                case 6 -> 0xBF;
                case 7 -> 0x7F;
                default -> {
                    System.err.println("Unknown bitcode: " + iCounterBit);
                    yield 0xFF;
                }
            };

            iBuffer[iCounterPos] = (byte) (iBuffer[iCounterPos] & lNr);
        }

        increaseCounter(1);
    }

//	public String getCounterString()
//	{
//	    StringBuffer lBuffer = new StringBuffer();
//	    
//	    int lInt = 0;
//	    try
//	    {
//	        lInt = getCounterInt(8);
//	    }
//	    catch ( Exception pEx )
//	    {
//	        return null;
//	    }
//	    while ( lInt != 0 )
//	    {
//		    try
//		    {
//		        lBuffer.append((char) lInt);
//		        lInt = getCounterInt(8);
//		    }
//		    catch ( Exception pEx )
//		    {
//		        lInt = 0;
//		    }
//	    }
//	    
//	    return lBuffer.toString();
//	}
//	public String getCounterString(int pCharNr)
//	{
//		char lBuffer[] = new char[pCharNr];
//		
//		for ( int lCharNr = 0 ; lCharNr < pCharNr ; lCharNr ++ )
//		{
//			lBuffer[lCharNr] = 0;
//			for ( int lBitNr=0 ; lBitNr < 8 ; lBitNr++)
//			{
//				// first read
//				boolean lBit = getCounterBoolean();
//				if ( lBit )
//				{
//					// now set
//					int lNr = 1;
//					if ( lBitNr > 0 )
//					{
//						lNr = 1 << lBitNr;
//					}
//					lBuffer[lCharNr] = (char) (lBuffer[lCharNr] | lNr);
//				}
//			}
//		}
//
//		return new String(lBuffer);
//	}

    public void setCounterInt(int pBitNr, int pValue) { // 1100111 == 103
        int lInt = pValue;

        boolean lIntCount[] = new boolean[pBitNr];

        for (int i = 0; i < pBitNr; i++) {
            lIntCount[i] = ((lInt & 1) == 1);

            lInt = lInt >> 1;
        }

        for (int i = 0; i < pBitNr; i++) {
            setCounterBoolean(lIntCount[i]);
        }

//		System.err.println( "Test" );

//		for ( int i = 0 ; i < pBitNr ; i++ )
//		{
//			lIntCount[i] = getCounterBoolean();
//		}
//		
//		for ( int i = pBitNr-1 ; i >= 0 ; i-- )
//		{
//			lInt = lInt << 1;
//			if ( lIntCount[i] )
//			{
//				lInt++;
//			}
//			
//		}


//	    for ( int i = 0 ; i < pBitNr ; i++ )
//	    {
//	        boolean lSet = ((pValue >> (pBitNr-(i+1)) ) & 1) == 1;
//	        setCounterBoolean(lSet);
//	    }
//		int lInt = 0;
//		
//		boolean lIntCount[] = new boolean[pBitNr];
//		
//		for ( int i = 0 ; i < pBitNr ; i++ )
//		{
//			lIntCount[i] = getCounterBoolean();
//		}
//		
//		for ( int i = pBitNr-1 ; i >= 0 ; i-- )
//		{
//			lInt = lInt << 1;
//			if ( lIntCount[i] )
//			{
//				lInt++;
//			}
//			
//		}
//		
//		return lInt;
    }

}
