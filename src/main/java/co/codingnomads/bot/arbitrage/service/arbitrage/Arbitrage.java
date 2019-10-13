package co.codingnomads.bot.arbitrage.service.arbitrage;

import co.codingnomads.bot.arbitrage.action.arbitrage.ArbitrageEmailAction;
import co.codingnomads.bot.arbitrage.action.arbitrage.ArbitragePrintAction;
import co.codingnomads.bot.arbitrage.action.arbitrage.ArbitrageTradingAction;
import co.codingnomads.bot.arbitrage.action.arbitrage.SimulatedArbitrageTradingAction;
import co.codingnomads.bot.arbitrage.action.arbitrage.selection.ArbitrageActionSelection;
import co.codingnomads.bot.arbitrage.exception.EmailLimitException;
import co.codingnomads.bot.arbitrage.exception.ExchangeDataException;
import co.codingnomads.bot.arbitrage.exchange.ExchangeSpecs;
import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import co.codingnomads.bot.arbitrage.model.exchange.ActivatedExchange;
import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import co.codingnomads.bot.arbitrage.service.general.BalanceCalc;
import co.codingnomads.bot.arbitrage.service.general.DataUtil;
import co.codingnomads.bot.arbitrage.service.general.ExchangeDataGetter;
import co.codingnomads.bot.arbitrage.service.general.ExchangeGetter;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static co.codingnomads.bot.arbitrage.service.arbitrage.ArbitrageMode.TRADING;

/**
 * Created by Thomas Leruth on 12/14/17
 * Modified by Tomislav Strelar on 2019-10-13
 * <p>
 * the arbitrage bot class
 */


@Service
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Arbitrage {

    public static final int MAX_REPEAT_INTERVAL = 5000;
    @Autowired
    private SimulatedWallet simulatedWallet;

    @Autowired
    private ExchangeGetter exchangeGetter;

    @Autowired
    @Qualifier("trading")
    private ArbitrageTradingAction tradingAction;

    @Autowired
    @Qualifier("simulatedTrade")
    private SimulatedArbitrageTradingAction simulatedTradingAction;

    @Autowired
    @Qualifier("email")
    private ArbitrageEmailAction emailAction;

    @Autowired
    @Qualifier("print")
    private ArbitragePrintAction printAction;


    private BalanceCalc balanceCalc = new BalanceCalc();
    private ExchangeDataGetter exchangeDataGetter = new ExchangeDataGetter();
    private DataUtil dataUtil = new DataUtil();
    private int timeIntervalRepeater;
    private int loopIterations;

    public void setTimeIntervalRepeater(int timeIntervalRepeater) {
        this.timeIntervalRepeater = timeIntervalRepeater;
    }
    public void setLoopIterations(int loopIterations) {
        this.loopIterations = loopIterations;
    }

    /**
     *
     *
     * @param currencyPair             the pair selected
     * @param selectedExchanges        a List of Exchange specs that will be looked in
     * @throws IOException
     */
    public void run(CurrencyPair currencyPair,
                    List<ExchangeSpecs> selectedExchanges,
                    ArbitrageMode arbitrageMode) throws IOException, InterruptedException, EmailLimitException, ExchangeDataException {

        if (arbitrageMode == TRADING) {
            for (ExchangeSpecs exchange : selectedExchanges) {
                if (exchange.getSetupExchange().getApiKey() == null
                        || exchange.getSetupExchange().getSecretKey() == null) {
                    throw new ExchangeDataException(
                            "You must enter correct exchange specs for "
                                    + exchange.getSetupExchange().getExchangeName());
                }
            }
            // prints balance out for the selectedExchanges
            balanceCalc.Balance(selectedExchanges, currencyPair);
        }

        //create a new array list of Activated Exchanges and sets it equal to the selected exchanges set in the controller
        List<ActivatedExchange> activatedExchanges =
                exchangeGetter.getAllSelectedExchangeServices(
                        selectedExchanges,
                        arbitrageMode == TRADING);

        doArbitrage(currencyPair, activatedExchanges, arbitrageMode, false);
    }

    /**
     * Arbitrage bot with multiple arbitrage action
     *
     * @param currencyPair
     * @param activatedExchanges
     * @param arbitrageMode
     * @throws ExchangeDataException
     * @throws EmailLimitException
     * @throws InterruptedException
     */
    public void doArbitrage(CurrencyPair currencyPair,
                             List<ActivatedExchange> activatedExchanges,
                             ArbitrageMode arbitrageMode,
                             boolean isSimulated) throws ExchangeDataException, EmailLimitException, InterruptedException {

        double tradeValueBase = -1;

        if (arbitrageMode == TRADING) {
            if (isSimulated) {
                tradeValueBase = simulatedTradingAction.getTradeValueBase();
            } else {
                tradeValueBase = tradingAction.getTradeValueBase();
            }
        }

        BigDecimal valueOfTradeValueBase = BigDecimal.valueOf(tradeValueBase);

        while (loopIterations >= 0) {
            List<TickerData> listTickerData = exchangeDataGetter.getAllTickerData(
                    activatedExchanges,
                    currencyPair,
                    valueOfTradeValueBase,
                    simulatedWallet);
            if (arbitrageMode == TRADING && listTickerData.size() == 0) {
                throw new ExchangeDataException("Unable to pull exchange data, either the pair " + currencyPair + " is not supported on the exchange/s selected" +
                        " or you do not have a wallet with the needed trade base of " + tradeValueBase + currencyPair.base);
            }

            TickerData highBid = dataUtil.highBidFinder(listTickerData);
            TickerData lowAsk = dataUtil.lowAskFinder(listTickerData);

            switch (arbitrageMode) {
                case PRINT:
                    printAction.print(lowAsk, highBid);
                    break;
                case EMAIL:
                    emailAction.email(lowAsk, highBid);
                    break;
                case TRADING:
                    ArbitrageTradingAction selectedTradingAction;
                    if (isSimulated) {  selectedTradingAction = simulatedTradingAction; }
                    else             {  selectedTradingAction = tradingAction;          }

                    if (selectedTradingAction.canTrade(lowAsk, highBid)) {
                        selectedTradingAction.makeTrade(lowAsk, highBid);
                    }
                    break;
            }
            Thread.sleep(Math.max(timeIntervalRepeater, MAX_REPEAT_INTERVAL));
            loopIterations--;
        }


    }

}


