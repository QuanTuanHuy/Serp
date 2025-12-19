package serp.project.logistics.constant;

public enum EntityType {
    PRODUCT("PRODUCT"),
    SUPPLIER("SUPPLIER"),
    CUSTOMER("CUSTOMER"),
    FACILITY("FACILITY");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static EntityType fromValue(String value) {
        for (EntityType type : EntityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EntityType value: " + value);
    }

}
