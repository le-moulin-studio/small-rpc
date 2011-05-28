package com.lemoulinstudio.small;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 *
 * @author Vincent Cantin
 */
public class SmallDataInputStream extends DataInputStream {

  public SmallDataInputStream(InputStream in) {
    super(in);
  }
  
  public String readMyUTF() throws IOException {
    int utflen = readUnsignedShort();

    // This is the support for the null value.
    if (utflen == 65535)
      return null;

    byte bytearr[] = new byte[utflen * 2];
    char chararr[] = new char[utflen * 2];

    int c, char2, char3;
    int count = 0;
    int chararr_count = 0;

    readFully(bytearr, 0, utflen);

    while (count < utflen) {
      c = (int) bytearr[count] & 0xff;      
      if (c > 127) break;
      count++;
      chararr[chararr_count++] = (char) c;
    }

    while (count < utflen) {
      c = (int) bytearr[count] & 0xff;
      switch (c >> 4) {
        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
          /* 0xxxxxxx*/
          count++;
          chararr[chararr_count++]= (char) c;
          break;
        case 12: case 13:
          /* 110x xxxx   10xx xxxx*/
          count += 2;
          if (count > utflen)
            throw new UTFDataFormatException("malformed input: partial character at end");
          char2 = (int) bytearr[count - 1];
          if ((char2 & 0xC0) != 0x80)
            throw new UTFDataFormatException("malformed input around byte " + count); 
          chararr[chararr_count++]=(char)(((c & 0x1F) << 6) | 
                                          (char2 & 0x3F));  
          break;
        case 14:
          /* 1110 xxxx  10xx xxxx  10xx xxxx */
          count += 3;
          if (count > utflen)
            throw new UTFDataFormatException("malformed input: partial character at end");
          char2 = (int) bytearr[count - 2];
          char3 = (int) bytearr[count - 1];
          if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
            throw new UTFDataFormatException("malformed input around byte " + (count - 1));
          chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                          ((char2 & 0x3F) << 6)  |
                                          ((char3 & 0x3F) << 0));
          break;
        default:
          /* 10xx xxxx,  1111 xxxx */
          throw new UTFDataFormatException("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than utflen
    return new String(chararr, 0, chararr_count);
  }
  
}
