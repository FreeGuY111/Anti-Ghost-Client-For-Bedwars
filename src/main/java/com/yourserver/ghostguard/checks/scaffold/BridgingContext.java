// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/scaffold/BridgingContext.java
package com.yourserver.ghostguard.checks.scaffold;

public final class BridgingContext {
    public boolean bridging;
    public String why;

    public BridgingContext(boolean bridging, String why) {
        this.bridging = bridging;
        this.why = why;
    }
}