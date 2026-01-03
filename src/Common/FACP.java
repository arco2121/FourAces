package Common;

import java.io.Serializable;
import java.util.HashMap;

//Four Aces Common Protocol
public class FACP {

    public enum ActionType {
        //Type1
        CONNECT, START, END, END_WIN, END_LOST,
        //Type2
        MOVE, INVALID, WAIT, CHANGE_TURN, UPDATE
    }

    public enum Role {
        SERVER, CLIENT
    }

    public static class CommonMessage implements Serializable {
        private ActionType type;
        public final Role from;
        private final HashMap<String, Object> paramas = new HashMap<>();
        private boolean lockMessage = false;

        public CommonMessage(ActionType type) {
            this.type = type;
            this.from = Role.SERVER;
        }

        public void setParam(String key, Object value) {
            if(!lockMessage)
                paramas.put(key, value);
        }

        public void setAction(ActionType type) {
            if(!lockMessage)
                this.type = type;
        }

        public ActionType getAction() { return type; }

        public Object getParam(String key) {
            return paramas.get(key);
        }

        public void lock() { lockMessage = true; }
    }
}
