package FourAces.Client;

import Common.Utility;

public class InputFromServer extends Thread {
    private final Core core;

    public InputFromServer(Core core) {
        this.core = core;
    }

    @Override
    public void run() {
        Utility.outer.print(",,");
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
