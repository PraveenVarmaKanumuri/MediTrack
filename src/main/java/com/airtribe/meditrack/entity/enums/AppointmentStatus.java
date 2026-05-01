package com.airtribe.meditrack.entity.enums;

public enum AppointmentStatus {

    PENDING("Pending") {
        @Override
        public boolean canTransitionTo(AppointmentStatus next) {
            return next == CONFIRMED || next == CANCELLED;
        }
    },
    CONFIRMED("Confirmed") {
        @Override
        public boolean canTransitionTo(AppointmentStatus next) {
            return next == COMPLETED || next == CANCELLED;
        }
    },
    CANCELLED("Cancelled") {
        @Override
        public boolean canTransitionTo(AppointmentStatus next) {
            return false;
        }
    },
    COMPLETED("Completed") {
        @Override
        public boolean canTransitionTo(AppointmentStatus next) {
            return next == PAID;
        }
    },
    PAID("Paid") {
        @Override
        public boolean canTransitionTo(AppointmentStatus next) {
            return false;
        }
    };

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Abstract method — each value must implement its own version
    public abstract boolean canTransitionTo(AppointmentStatus next);

    @Override
    public String toString() {
        return displayName;
    }
}