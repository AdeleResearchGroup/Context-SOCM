package fr.liglab.adele.cream.annotations.provider;

public enum OriginEnum {
    internal(Value.internal),
    local(Value.local),
    remote(Value.remote);

    private String value;

    OriginEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public static class Value {
        public static final String internal = "internal";
        public static final String local = "local";
        public static final String remote = "remote";
    }
}
