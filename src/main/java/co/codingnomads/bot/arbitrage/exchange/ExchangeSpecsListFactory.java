package co.codingnomads.bot.arbitrage.exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeSpecsListFactory {

    public static List<ExchangeSpecs> createFrom(List<ExchangeName> exchangeNames, boolean isSimulated) {
        List<ExchangeSpecs> ret = new ArrayList<>();

        exchangeNames.forEach($ -> create($, isSimulated).ifPresent(ret::add));

        return ret;

    }

    private static Optional<ExchangeSpecs> create(ExchangeName name, boolean isSimulated) {
        switch (name) {
            case KRAKEN:   return Optional.of(new KrakenSpecs(isSimulated));
            case GDAX:     return Optional.of(new GDAXSpecs(isSimulated));
            case BITTREX:  return Optional.of(new BittrexSpecs(isSimulated));
            case BINANCE:  return Optional.of(new BinanceSpecs(isSimulated));
            case POLONIEX: return Optional.of(new PoloniexSpecs(isSimulated));
            case GEMINI:   return Optional.of(new GeminiSpecs(isSimulated));
            default:       return Optional.empty();
        }
    }
}
