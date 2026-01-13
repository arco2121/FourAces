package Common;

import java.io.PrintStream;
import java.util.Scanner;

public class Utility {
    public final static PrintStream outer = System.out;
    public final static Scanner inner = new Scanner(System.in);
    public static boolean securityOn = false;
    public static final String globalPassword = "FourAcesExample"; //Just an example
    public final static String Version = "1.0.0";
    public final static int MAX_BYTE = 1400;
}