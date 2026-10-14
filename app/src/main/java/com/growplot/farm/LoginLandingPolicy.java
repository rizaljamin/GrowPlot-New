package com.growplot.farm;

/**
 * GrowPlot post-login routing contract.
 * Call only after a successful, trusted login; never determine privileges from
 * a user-editable username field or unverified client-side profile.
 */
public final class LoginLandingPolicy {
    public enum Destination { ADMIN_HOME, EMPTY_PLOT_HOME, PLOT_HOME, LOGIN }

    private LoginLandingPolicy() {}

    public static Destination destination(boolean authenticated,
                                          boolean trustedAdministrator,
                                          int ownedPlotCount) {
        if (!authenticated) return Destination.LOGIN;
        if (trustedAdministrator) return Destination.ADMIN_HOME;
        return ownedPlotCount <= 0 ? Destination.EMPTY_PLOT_HOME : Destination.PLOT_HOME;
    }

    public static boolean mayManageUsers(boolean authenticated, boolean trustedAdministrator) {
        return authenticated && trustedAdministrator;
    }

    public static boolean shouldPromptCreateFirstPlot(boolean authenticated,
                                                      boolean trustedAdministrator,
                                                      int ownedPlotCount) {
        return destination(authenticated, trustedAdministrator, ownedPlotCount)
                == Destination.EMPTY_PLOT_HOME;
    }
}
