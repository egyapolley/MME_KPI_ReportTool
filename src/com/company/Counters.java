package com.company;

import java.util.List;

public class Counters {

    private static final String[] counters = {
            "VS.NbrSuccessAttachRequests",
            "VS.AttAttachRequests",
            "VS.NbrAttachReqAbortBefore",
            "VS.NbrAttachReqAbortAfter",
            "VS.NbrFailedAttachRequests_PLMNnotAllowed",
            "VS.NbrFailedAttachRequests_EPSandNonEPSnotAllowed",
            "VS.NbrFailedAttachRequests_CannotDeriveUEid",
            "VS.NbrFailedAttachRequests_NetworkFailure",
            "VS.NbrPageRespInLastSeenTA",
            "VS.NbrPageRespNotInLastSeenTA",
            "VS.NbrPagingFailures_Timeout",
            "VS.AttPaging_FirstAttempt",
            "VS.NbrSuccessTAU",
            "VS.TauInterMmeSucc",
            "VS.AttTAU",
            "VS.TauInterMmeAtt",
            "VS.memUsage",
            "VS.maxNEmemUsage",
            "VS.aveCpuUsage",
            "VS.peakCpuUsage",
            "VS.UECapacityUsage",
            "VS.AveNumOfDefaultBearers",
            "VS.MaxNumOfDefaultBearers",
            "VS.AveNumOfDedicatedBearers",
            "VS.MaxNumOfDedicatedBearers",
            "VS.AveNbrOfRegisteredUE",
            "VS.MaxNbrOfRegisteredUE",
            "VS.AveNbrOfIdleUE",
            "VS.MaxNbrOfIdleUE",
            "VS.AveNbrOfConnectedUE",
            "VS.MaxNbrOfConnectedUE",

    };

    public static String[] getCounters() {
        return counters;
    }
}
