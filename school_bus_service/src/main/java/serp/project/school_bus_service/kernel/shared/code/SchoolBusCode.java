package serp.project.school_bus_service.kernel.shared.code;

public enum SchoolBusCode {
    SCHOOL("SCHOOL", "SBU"),
    STUDENT("STUDENT", "STU"),
    ROUTE("ROUTE", "RTE");

    private final String sequenceKey;
    private final String prefix;

    SchoolBusCode(String sequenceKey, String prefix) {
        this.sequenceKey = sequenceKey;
        this.prefix = prefix;
    }

    public String sequenceKey() {
        return sequenceKey;
    }

    public String prefix() {
        return prefix;
    }
}
