package co.codingnomads.bot.arbitrage.action.arbitrage.selection;

/**
 * Created by Thomas Leruth on 12/14/17
 *
 * abstract method to be extended for each arbitrage action
 */

public abstract class ArbitrageActionSelection {

    private double arbitrageMargin;


    protected ArbitrageActionSelection() {
    }

    public void setArbitrageMargin(double arbitrageMargin) {
        this.arbitrageMargin = arbitrageMargin;
    }

    protected double getArbitrageMargin() {
        return arbitrageMargin;
    }

}
