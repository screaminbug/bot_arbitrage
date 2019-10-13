package co.codingnomads.bot.arbitrage.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {
    private boolean finished;
    private String message;
}
