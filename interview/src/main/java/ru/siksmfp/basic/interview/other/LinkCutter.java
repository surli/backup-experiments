package ru.siksmfp.basic.interview.other;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Artem Karnov @date 16.06.2017.
 *         artem.karnov@t-systems.com
 *         <p>
 *         Class for link cutting.
 *         <p>
 *         Requirments:
 *         1 short link should be short enought for human remembering
 *         2 app shouldn't use additional store instrumens like databases
 *         3 should be used algorithm that unequivocally make link shorter
 */

// TODO: 17.07.2017 to finish it
public class LinkCutter {
    public static String getShortLink(String fullLink) {
        try {
            // Encode a String into bytes
            byte[] input = fullLink.getBytes("UTF-8");

            // Compress the bytes
            byte[] output = new byte[100];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            showBits(output);
            String inputString = new String(output, 0, compressedDataLength);
            System.out.println(inputString);
            compresser.end();

            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(output, 0, compressedDataLength);
            byte[] result = new byte[100];
            int resultLength = decompresser.inflate(result);
            decompresser.end();
            showBits(result);
            // Decode the bytes into a String
            String outputString = new String(result, 0, resultLength, "UTF-8");
            System.out.println(outputString);
        } catch (java.io.UnsupportedEncodingException ex) {
            // handle
        } catch (java.util.zip.DataFormatException ex) {
            // handle
        }
        return "";
    }

    public static void showBits(byte[] arr) {
        for (byte curByte : arr)
            System.out.print(curByte);
        System.out.println();

    }

    public static String getFullQualifiedLink(String shortLink) {
        return "";
    }

    public static void main(String[] args) {

        getShortLink(" mcbcvxv n bvxddsfv fdsrtfv   crgf");
    }
}
