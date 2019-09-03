package com.jayfella.jme.vehicle.view;

public enum VehicleCamView {

    FirstPerson,
    ThirdPerson;

    public VehicleCamView next() {

        int currentOrdinal = ordinal();

        if (currentOrdinal == VehicleCamView.values().length - 1) {
            return VehicleCamView.values()[0];
        }
        else {
            return VehicleCamView.values()[currentOrdinal + 1];
        }
    }

    public VehicleCamView previous() {

        int currentOrdinal = ordinal();

        if (currentOrdinal == 0) {
            return VehicleCamView.values()[VehicleCamView.values().length - 1];
        }
        else {
            return VehicleCamView.values()[currentOrdinal - 1];
        }
    }

}