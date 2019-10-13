package co.codingnomads.bot.arbitrage.action.arbitrage;

import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import co.codingnomads.bot.arbitrage.model.ticker.TickerDataSimulated;
import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import co.codingnomads.bot.arbitrage.model.ticker.TickerDataTrading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by Thomas Leruth on 12/17/17
 * <p>
 * class for the information needed to use the trading action as behavior action
 */
@Component("simulatedTrade")
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimulatedArbitrageTradingAction extends ArbitrageTradingAction {

    @Autowired
    private SimulatedWallet simulatedWallet;

    /**
     * Make trade method that executes trades on the low ask exchange and the high bid exchange
     * @param lowAskReal
     * @param highBidReal
     */
    public void makeTrade(TickerData lowAskReal,
                          TickerData highBidReal) {

        TickerData lowAsk = lowAskReal;
        TickerData highBid = highBidReal;

        if (lowAskReal instanceof TickerDataTrading && highBidReal instanceof TickerDataTrading) {
            lowAsk = new TickerDataSimulated((TickerDataTrading) lowAskReal, simulatedWallet);
            highBid = new TickerDataSimulated((TickerDataTrading) highBidReal, simulatedWallet);
        }

        super.makeTrade(lowAsk, highBid);
    }
}


