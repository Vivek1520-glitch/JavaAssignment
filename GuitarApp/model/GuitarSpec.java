package com.aurionpro.model;

public class GuitarSpec {
    private Builder builder;
    private String model;
    private Type type;
    private int numStrings;
    private Wood backWood;
    private Wood topWood;

    public GuitarSpec(Builder builder, String model, Type type, int numStrings, Wood backWood, Wood topWood) {
        this.builder = builder;
        this.model = model;
        this.type = type;
        this.numStrings = numStrings;
        this.backWood = backWood;
        this.topWood = topWood;
    }

    public Builder getBuilder() { return builder; }
    public String getModel() { return model; }
    public Type getType() { return type; }
    public int getNumStrings() { return numStrings; }
    public Wood getBackWood() { return backWood; }
    public Wood getTopWood() { return topWood; }

    public boolean matches(GuitarSpec otherSpec) {
        if (otherSpec.getBuilder() != null && builder != otherSpec.getBuilder())
            return false;

        String model = this.model != null ? this.model.toLowerCase() : null;
        String otherModel = otherSpec.getModel() != null ? otherSpec.getModel().toLowerCase() : null;
        if (otherModel != null && !otherModel.isEmpty() && !model.equals(otherModel))
            return false;

        if (otherSpec.getType() != null && type != otherSpec.getType())
            return false;

        if (otherSpec.getNumStrings() != 0 && numStrings != otherSpec.getNumStrings())
            return false;

        if (otherSpec.getBackWood() != null && backWood != otherSpec.getBackWood())
            return false;

        if (otherSpec.getTopWood() != null && topWood != otherSpec.getTopWood())
            return false;

        return true;
    }

}
