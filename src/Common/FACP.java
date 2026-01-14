package Common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;

//Four Aces Common Protocol
public class FACP {

    public enum ActionType {
        //Type1
        CONNECT, START, END, END_WIN, END_LOST,
        //Type2
        MOVE, INVALID, WAIT, CHANGE_TURN, UPDATE,
        //Type for UDP
        RESYNC,
        //For Custom String Commands
        TEXTUAL
    }

    public enum Role {
        SERVER, CLIENT
    }

    public enum TransportType {
        TCP, UDP
    }

    public static class Security {
        private static final int ITERATIONS = 65536;
        private static final int KEY_LENGTH = 256;
        public static final String ALGORITHM = "PBKDF2WithHmacSHA256";

        public static String hash(char[] password) throws Exception {
            byte[] salt = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);

            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        }

        public static boolean verifyHash(char[] password, String stored) throws Exception {
            String[] parts = stored.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            return java.security.MessageDigest.isEqual(hash, testHash);
        }
    }

    public static class CommonMessage implements Serializable {
        private ActionType type;
        public final Role from;
        private final HashMap<String, Object> paramas = new HashMap<>();
        private boolean lockMessage = false;
        private String hashCode = null;

        public CommonMessage(ActionType type, Role role) {
            this.type = type;
            this.from = role;
        }
        public CommonMessage(String typeTextual, Role role) {
            this.type = ActionType.TEXTUAL;
            this.from = role;
            setParam("TEXTUAL", typeTextual);
        }
        public CommonMessage(ActionType type, Role role, String key, boolean doLock) {
            this.type = type;
            this.from = role;
            if(doLock) lock(key);
        }
        public CommonMessage(String typeTextual, Role role, String key, boolean doLock) {
            this.type = ActionType.TEXTUAL;
            this.from = role;
            setParam("TEXTUAL", typeTextual);
            if(doLock) lock(key);
        }

        public void setParam(String key, Object value) {
            if(!lockMessage)
                paramas.put(key, value);
        }

        public void setAction(ActionType type) {
            if(!lockMessage)
                this.type = type;
        }
        public void setAction(String typeTextual) {
            if(!lockMessage) {
                this.type = ActionType.TEXTUAL;
                setParam("TEXTUAL", typeTextual);
            }
        }

        public ActionType getAction() { return lockMessage ? null : type; }
        public String getTextualAction() { return lockMessage && type != ActionType.TEXTUAL ? null : (String) getParam("TEXTUAL");}

        public Object getParam(String key) {
            if(!lockMessage)
                return paramas.get(key);
            return null;
        }

        public boolean unLock(String password) {
            if(!lockMessage) return false;
            try {
                if(Security.verifyHash(password.toCharArray(), hashCode)) {
                    lockMessage = false;
                    hashCode = null;
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean isLocked() { return lockMessage; }

        public boolean lock(String password) {
            if(lockMessage) return false;
            try {
                lockMessage = true;
                hashCode = Security.hash(password.toCharArray());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
