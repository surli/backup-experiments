package org.knowm.xchange.cexio.dto.trade;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.knowm.xchange.currency.Currency;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
  {
    "id": "1",
    "type": "sell",
    "time": "2017-09-09T10:56:30.612Z",
    "lastTxTime": "2017-09-09T10:58:44.773Z",
    "lastTx": "4307893035",
    "pos": null,
    "status": "d",
    "symbol1": "BTC",
    "symbol2": "GBP",
    "amount": "0.05000000",
    "price": "3435.0004",
    "fa:GBP": "0.00",
    "ta:GBP": "171.75",
    "remains": "0.00000000",
    "a:BTC:cds": "0.05000000",
    "a:GBP:cds": "171.75",
    "f:GBP:cds": "0.00",
    "tradingFeeMaker": "0",
    "tradingFeeTaker": "0.17",
    "tradingFeeUserVolumeAmount": "6457460231",
    "orderId": "1"
  },
  {
    "id": "2",
    "type": "sell",
    "time": "2017-09-09T10:50:27.028Z",
    "lastTxTime": "2017-09-09T10:50:27.028Z",
    "lastTx": "4307823094",
    "pos": null,
    "status": "d",
    "symbol1": "BTC",
    "symbol2": "GBP",
    "amount": "0.06330000",
    "price": "3421.1501",
    "tfacf": "1",
    "remains": "0.00000000",
    "tfa:GBP": "0.37",
    "tta:GBP": "216.55",
    "a:BTC:cds": "0.06330000",
    "a:GBP:cds": "216.55",
    "f:GBP:cds": "0.37",
    "tradingFeeMaker": "0",
    "tradingFeeTaker": "0.17",
    "tradingFeeUserVolumeAmount": "6451130231",
    "orderId": "2"
  }

  status - "d" — done (fully executed), "c" — canceled (not executed), "cd" — cancel-done (partially executed)
  ta:USD/tta:USD – total amount in current currency (Maker/Taker)
  fa:USD/tfa:USD – fee amount in current currency (Maker/Taker)
  a:BTC:cds – credit, debit and saldo merged amount in current currency
  tradingFeeMaker,tradingFeeTaker – fee % value of Maker/Taker transactions

  what is tfacf??
*/

@JsonDeserialize(using = CexIOArchivedOrder.Deserializer.class)
public class CexIOArchivedOrder {
  public final String id;
  public final String type;
  public final String time;
  public final String lastTxTime;
  public final String lastTx;
  public final String pos;
  public final String status;
  public final String symbol1;
  public final String symbol2;
  public final String amount;
  public final String price;
  public final String remains;
  public final String tradingFeeMaker;
  public final String tradingFeeTaker;
  public final String tradingFeeUserVolumeAmount;
  public final String orderId;
  public final String feeValue;
  public final String feeCcy;

  public CexIOArchivedOrder(String id, String type, String time, String lastTxTime,
                            String lastTx, String pos, String status, String symbol1,
                            String symbol2, String amount, String price, String remains,
                            String tradingFeeMaker, String tradingFeeTaker, String tradingFeeUserVolumeAmount,
                            String orderId, String feeValue, String feeCcy) {
    this.id = id;
    this.type = type;
    this.time = time;
    this.lastTxTime = lastTxTime;
    this.lastTx = lastTx;
    this.pos = pos;
    this.status = status;
    this.symbol1 = symbol1;
    this.symbol2 = symbol2;
    this.amount = amount;
    this.price = price;
    this.remains = remains;
    this.tradingFeeMaker = tradingFeeMaker;
    this.tradingFeeTaker = tradingFeeTaker;
    this.tradingFeeUserVolumeAmount = tradingFeeUserVolumeAmount;
    this.orderId = orderId;
    this.feeValue = feeValue;
    this.feeCcy = feeCcy;
  }

  static class Deserializer extends JsonDeserializer<CexIOArchivedOrder> {

    @Override
    public CexIOArchivedOrder deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
      Map<String, String> map = new HashMap<>();

      JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
      Iterator<Map.Entry<String, JsonNode>> tradesResultIterator = jsonNode.fields();
      while (tradesResultIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = tradesResultIterator.next();
        map.put(entry.getKey(), entry.getValue().asText());
      }

      String feeValue = null;
      String feeCcy = null;
      for (String key : map.keySet()) {
        if (key.startsWith("tfa:") || key.startsWith("fa:")) {
          feeValue = map.get(key);
          feeCcy = key.split(":")[1];
        }
      }

      return new CexIOArchivedOrder(
          map.get("id"),
          map.get("type"),
          map.get("time"),
          map.get("lastTxTime"),
          map.get("lastTx"),
          map.get("pos"),
          map.get("status"),
          map.get("symbol1"),
          map.get("symbol2"),
          map.get("amount"),
          map.get("price"),
          map.get("remains"),
          map.get("tradingFeeMaker"),
          map.get("tradingFeeTaker"),
          map.get("tradingFeeUserVolumeAmount"),
          map.get("orderId"),
          feeValue,
          feeCcy
      );
    }
  }

  public static class Fee {
    public final Currency currency;
    public final BigDecimal amount;

    public Fee(Currency currency, BigDecimal amount) {
      this.currency = currency;
      this.amount = amount;
    }
  }

}
