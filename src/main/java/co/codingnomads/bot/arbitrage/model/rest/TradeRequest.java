package co.codingnomads.bot.arbitrage.model.rest;

import co.codingnomads.bot.arbitrage.exchange.ExchangeName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.List;

@Data
public class TradeRequest {
    private List<ExchangeName> exchanges;
    private List<CurrencyPair> currencyPairs;
    private Integer loopIterations;
    private Integer timeInterval;
    private Double arbMargin;
    private Double valueBase;
}
