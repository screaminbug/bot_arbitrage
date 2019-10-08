package co.codingnomads.bot.arbitrage.model.ticker;

import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedExchange;
import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import org.knowm.xchange.Exchange;

/**
 * Created by Thomas Leruth on 12/11/17
 *
 * POJO class to get the bid/ask and the exchangeName as well as the currency pair
 */

public class TickerDataSimulated extends TickerDataTrading {

    private SimulatedWallet simulatedWallet;

    public TickerDataSimulated(TickerDataTrading tickerData, SimulatedWallet simulatedWallet) {
        super(tickerData, tickerData.getBaseFund(), tickerData.getCounterFund());
        this.simulatedWallet = simulatedWallet;
    }

    public Exchange getExchange() {
        return new SimulatedExchange(exchange, simulatedWallet);
    }

}
