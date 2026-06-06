package com.example.individual_arif;

/**
 * Simple POJO / data class representing one row in the bills table.
 */
public class BillRecord {

    private long   id;
    private String month;
    private double units;
    private double rebate;
    private double totalCharges;
    private double finalCost;

    public BillRecord(long id, String month, double units, double rebate,
                      double totalCharges, double finalCost) {
        this.id           = id;
        this.month        = month;
        this.units        = units;
        this.rebate       = rebate;
        this.totalCharges = totalCharges;
        this.finalCost    = finalCost;
    }

    // ── getters ──
    public long   getId()           { return id; }
    public String getMonth()        { return month; }
    public double getUnits()        { return units; }
    public double getRebate()       { return rebate; }
    public double getTotalCharges() { return totalCharges; }
    public double getFinalCost()    { return finalCost; }

    // ── setters ──
    public void setMonth(String month)               { this.month = month; }
    public void setUnits(double units)               { this.units = units; }
    public void setRebate(double rebate)             { this.rebate = rebate; }
    public void setTotalCharges(double totalCharges) { this.totalCharges = totalCharges; }
    public void setFinalCost(double finalCost)       { this.finalCost = finalCost; }
}
