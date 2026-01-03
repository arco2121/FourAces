package Common;

import java.io.PrintStream;
import java.util.Scanner;

public class Utility {
    public final static PrintStream outer = System.out;
    public final static Scanner inner = new Scanner(System.in);
    public static boolean securityOn = false;
    public static final String globalPassword = DotEnv.get("GLOBAL_PASSWORD");
    public final static String Version = "1.0.0";
}