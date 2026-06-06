package com.example.individual_arif;

/**
 * Stateless utility class that implements the TNB Domestic Tariff block-rate
 * calculation and rebate deduction.
 *
 * Tariff blocks (sen/kWh):
 *   Block 1 :   1 – 200  kWh  →  21.8 sen/kWh
 *   Block 2 : 201 – 300  kWh  →  33.4 sen/kWh
 *   Block 3 : 301 – 600  kWh  →  51.6 sen/kWh
 *   Block 4 : 601 – 1000 kWh  →  54.6 sen/kWh
 */
public class BillCalculator {

    // sen / kWh for each block
    private static final double RATE_BLOCK1 = 0.218;   // RM/kWh (21.8 sen)
    private static final double RATE_BLOCK2 = 0.334;   // RM/kWh (33.4 sen)
    private static final double RATE_BLOCK3 = 0.516;   // RM/kWh (51.6 sen)
    private static final double RATE_BLOCK4 = 0.546;   // RM/kWh (54.6 sen)

    // Block boundaries
    private static final double BLOCK1_LIMIT = 200.0;
    private static final double BLOCK2_LIMIT = 300.0;
    private static final double BLOCK3_LIMIT = 600.0;

    /**
     * Compute total charges (before rebate) using tiered block rates.
     *
     * @param units  kWh consumed (1–1000)
     * @return total charges in RM
     */
    public static double calculateTotalCharges(double units) {
        double total = 0.0;

        if (units <= BLOCK1_LIMIT) {
            total = units * RATE_BLOCK1;
        } else if (units <= BLOCK2_LIMIT) {
            total  = BLOCK1_LIMIT * RATE_BLOCK1;
            total += (units - BLOCK1_LIMIT) * RATE_BLOCK2;
        } else if (units <= BLOCK3_LIMIT) {
            total  = BLOCK1_LIMIT * RATE_BLOCK1;
            total += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            total += (units - BLOCK2_LIMIT) * RATE_BLOCK3;
        } else {
            total  = BLOCK1_LIMIT * RATE_BLOCK1;
            total += (BLOCK2_LIMIT - BLOCK1_LIMIT) * RATE_BLOCK2;
            total += (BLOCK3_LIMIT - BLOCK2_LIMIT) * RATE_BLOCK3;
            total += (units - BLOCK3_LIMIT) * RATE_BLOCK4;
        }

        return total;
    }

    /**
     * Deduct rebate from total charges.
     *   Final cost = totalCharges - (totalCharges × rebatePercent / 100)
     *
     * @param totalCharges  result from {@link #calculateTotalCharges(double)}
     * @param rebatePercent rebate percentage (0–5)
     * @return final cost after rebate, in RM
     */
    public static double applyRebate(double totalCharges, double rebatePercent) {
        return totalCharges - (totalCharges * rebatePercent / 100.0);
    }

    /**
     * Convenience method: compute both values and return as a double[2].
     * result[0] = totalCharges, result[1] = finalCost
     */
    public static double[] calculate(double units, double rebatePercent) {
        double totalCharges = calculateTotalCharges(units);
        double finalCost    = applyRebate(totalCharges, rebatePercent);
        return new double[]{totalCharges, finalCost};
    }
}
