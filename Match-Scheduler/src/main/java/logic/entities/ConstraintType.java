package logic.entities;

public enum ConstraintType {
    HARD("HARD"), SOFT("SOFT");
    private String tpye;

    private  ConstraintType(String type) {
        this.tpye = type;
    }

    @Override
    public String toString(){
        return this.tpye;
    }
}
