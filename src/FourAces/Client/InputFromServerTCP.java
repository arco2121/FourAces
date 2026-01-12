package FourAces.Client;

import Common.Utility;

public class InputFromServerTCP extends Thread {
    private final CoreTCP core;

    public InputFromServerTCP(CoreTCP core) {
        this.core = core;
    }

    @Override
    public void run() {
        Utility.outer.print("\n");
        while (true) {
           if(core.isMyTurn()) {
               Utility.outer.println("\nMove :");
               String line = Utility.inner.nextLine();
               if (line.equalsIgnoreCase("exit"))
                   System.exit(0);
               try {
                   core.sendMove(Integer.parseInt(line));
               } catch (Exception ignored) {}
           }
        }
    }
}
