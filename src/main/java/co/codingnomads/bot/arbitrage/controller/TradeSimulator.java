package co.codingnomads.bot.arbitrage.controller;

import co.codingnomads.bot.arbitrage.action.arbitrage.ArbitrageTradingAction;
import co.codingnomads.bot.arbitrage.exception.EmailLimitException;
import co.codingnomads.bot.arbitrage.exception.ExchangeDataException;
import co.codingnomads.bot.arbitrage.exchange.*;
import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import co.codingnomads.bot.arbitrage.model.exchange.ActivatedExchange;
import co.codingnomads.bot.arbitrage.model.rest.TradeRequest;
import co.codingnomads.bot.arbitrage.model.rest.TradeResponse;
import co.codingnomads.bot.arbitrage.model.trading.TradingData;
import co.codingnomads.bot.arbitrage.service.arbitrage.Arbitrage;
import co.codingnomads.bot.arbitrage.service.arbitrage.ArbitrageMode;
import co.codingnomads.bot.arbitrage.service.general.ExchangeGetter;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.Balance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TradeSimulator {

    @Autowired
    private Arbitrage arbitrage;

    @Autowired
    @Qualifier("simulatedTrade")
    private ArbitrageTradingAction arbitrageTradingAction;

    @Autowired
    private ExchangeGetter exchangeGetter;

    @Autowired
    private SimulatedWallet simulatedWallet;

    @MessageMapping("/simulatetrade")
    @SendTo("/topic/traderesults")
    public TradeResponse simulate(TradeRequest tradeRequest) throws InterruptedException, ExchangeDataException, EmailLimitException {

        List<ExchangeSpecs> exchangeList =
                ExchangeSpecsListFactory.createFrom(tradeRequest.getExchanges(), true);

        arbitrage.setLoopIterations(tradeRequest.getLoopIterations());
        arbitrage.setTimeIntervalRepeater(tradeRequest.getTimeInterval());
        arbitrageTradingAction.setArbitrageMargin(tradeRequest.getArbMargin());
        arbitrageTradingAction.setTradeValueBase(tradeRequest.getValueBase());

        //create a new array list of Activated Exchanges and sets it equal to the selected exchanges set in the controller
        List<ActivatedExchange> activatedExchanges =
                exchangeGetter.getAllSelectedExchangeServices(
                        exchangeList,
                        true);

        //TODO: Get this info from request
        activatedExchanges.forEach($ -> {
            simulatedWallet.putBalance($.getExchange(), Currency.ETH, new Balance(Currency.ETH, new BigDecimal(1000)));
            simulatedWallet.putBalance($.getExchange(), Currency.USD, new Balance(Currency.USD, new BigDecimal(100000)));
            simulatedWallet.putBalance($.getExchange(), Currency.BTC, new Balance(Currency.BTC, new BigDecimal(100)));
        });

        arbitrage.doArbitrage(
                CurrencyPair.ETH_BTC,
                activatedExchanges,
                ArbitrageMode.TRADING,
                true);

        return new TradeResponse(true, "Arbitrage ended");
    }
}
