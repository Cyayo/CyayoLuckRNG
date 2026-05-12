package id.seria.cyayoluckrng.config;

import java.util.List;

public class RngTable {
    public enum TableType { MMOITEMS, VANILLA, COMMAND }

    public static class RngEntry {
        public String mmoType, mmoId, material, command, display, broadcast;
        public int    base, amount;

        public static RngEntry mmoitem(String type, String id, int base, String broadcast) {
            RngEntry e = new RngEntry(); e.mmoType=type; e.mmoId=id; e.base=base; e.broadcast=broadcast; return e;
        }
        public static RngEntry vanilla(String material, int amount, int base, String broadcast) {
            RngEntry e = new RngEntry(); e.material=material; e.amount=amount; e.base=base; e.broadcast=broadcast; return e;
        }
        public static RngEntry command(String command, String display, int base, String broadcast) {
            RngEntry e = new RngEntry(); e.command=command; e.display=display; e.base=base; e.broadcast=broadcast; return e;
        }
    }

    private final String key, name, receiveMessage;
    private final TableType type;
    private final boolean useLuck, useDoubleDrop;
    private final int rollsMin, rollsMax;
    private final List<RngEntry> items;

    public RngTable(String key, String name, TableType type, boolean useLuck, boolean useDoubleDrop,
                    int rollsMin, int rollsMax, String receiveMessage, List<RngEntry> items) {
        this.key=key; this.name=name; this.type=type; this.useLuck=useLuck; this.useDoubleDrop=useDoubleDrop;
        this.rollsMin=rollsMin; this.rollsMax=rollsMax; this.receiveMessage=receiveMessage; this.items=items;
    }

    public String    getKey()            { return key; }
    public String    getName()           { return name; }
    public TableType getType()           { return type; }
    public boolean   usesLuck()          { return useLuck; }
    public boolean   usesDoubleDrop()    { return useDoubleDrop; }
    public int       getRollsMin()       { return rollsMin; }
    public int       getRollsMax()       { return rollsMax; }
    public String    getReceiveMessage() { return receiveMessage; }
    public List<RngEntry> getItems()     { return items; }
}
