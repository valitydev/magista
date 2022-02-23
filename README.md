## Magista query DSL

Для формирования запросов к данным сервис предоставляет DSL в JSON формате, который основан на Elasticsearch [Query DSL](https://www.elastic.co/guide/en/elasticsearch/reference/current/_introducing_the_query_language.html). 

Общий формат запроса выглядит следующим образом:

```json
{
  "query": {
    "<query_type>": {
      "<param>": "<val>"
    },
    "<query_param>": "<val>"
  }
}
```

`<query_type>` - тип запроса, который требуется выполнить. `<query_param>` зависит от типа запроса.

`<query_param>` - параметр запроса, может включать:

1. Для запросов на выборку по моделям:

    1. `from` - (0-based) определяет, с какой записи результирующей выборки следует начать.
    2. `size` - определяет, сколько максимум записей следует вернуть, начиная с `from`.

### Типы запросов
 Вся статистика считается за период времени и на данный момент может включать следующие типы запросов: 

1. `payments_turnover` - статистика по обороту.
2. `payments_geo_stat` - статистика по географии платежей.
3. `payments_pmt_cards_stat` - статистика по платежным системам (по картам).
4. `payments_conversion_stat` - статистика по конверсии.
5. `customers_rate_stat` - статистика по количеству плательщиков.

Также возможны запросы на выборку по моделям:

1. `payments` - выборка по платежам.
2. `invoices` - выборка по инвойсам.



### Параметры запросов статистики

Все запросы возвращают упорядоченный по `offset` набор ассоциативных массивов, состоящий из агрегированной информации по интервалам разбиения. Порядок вывода определяется значением  `offset` (смещение текущего агрегата относительно начала запрошенного временного диапазона), которое присутстует в каждом агрегате. В случае, если одному значению интервала соответствует более одного агрегата, они снабжаются дополнительными ключами для возможности их классификации(группировки).

Все типы запросов статистики должны содержать следующие параметры:

1. `merchant_id`- id мерчанта, для которого производится выборка.
1. `shop_id`- id магазина, для которого производится выборка.
2. `from_time` - начало временного интервала выборки(inclusive).
3. `to_time` - конец временного интервала выборки(exclusive).
4. `split_interval` - интервал разбиения выборки, по которому проводится агрегация, указывается в секундах.

Аргументы, содержащие время, должны быть представлены в формате ISO 8601 UTC.

##### `payments_turnover` 
Возвращает статистику по обороту (сумма успешных платежей за вычетом комиссий) в виде набора, сгруппированного по представленным в выборке валютам:

- `currency_symbolic_code` - символьный код валюты(ключ)
- `amount_with_fee` - сумма с вычетом комиссий
- `amount_without_fee` - сумма без вычета комиссий

##### `payments_geo_stat`
Возвращает статистику по географии платежей (агрегация сумм успешных платежей по городам) в виде набора, сгруппированного по представленным в выборке городам и валютам:

- `city_id` - ID города, который определился для данной совокупности платежей(ключ)
- `country_id` - ID страны, которая определилась для данной совокупности платежей(ключ)
- `currency_symbolic_code` - символьный код валюты(ключ)
- `amount_with_fee` - сумма с вычетом комиссий
- `amount_without_fee` - сумма без вычета комиссий

##### `payments_pmt_cards_stat`
Возвращает статистику по платежным системам (агрегация сумм успешных платежей по типам карт) в виде набора, сгруппированного по представленным в выборке платежным системам:

- `total_count` - общее количество платежей в наборе
- `payment_system` - платежная система (тип карты)
- `amount_with_fee` - сумма с вычетом комиссий
- `amount_without_fee` - сумма без вычета комиссий

##### `payments_conversion_stat` 
Возвращает статистику по конверсии (отношение количества успешных платежей к общему количеству завершенных платежей):

- `successful_count` - количество успешных платежей в наборе
- `total_count` - общее количество платежей в наборе
- `conversion` - конверсия (0 <= conversion <= 1)

##### `customers_rate_stat`
Возвращает статистику по количеству плательщиков (агрегация по уникальным плательщикам):

- `unic_count` - количество уникальных плательщиков

#### Пример
Запросим статистику для мерчанта с id=1 по гео-данным за час с интервалом разбиения в 5 минут:

```json
{
	"query": {
		"payments_geo_stat": {
			"merchant_id": "1",
			"shop_id": "2",
			"from_time": "2016-03-22T00:12:00Z",
			"to_time": "2016-03-22T01:12:00Z",
			"split_interval": "300"
		}
	}
}
```

В ответ получим структуру:

```json
[
	{
		"offset": "0",
		"city_id": "524901",
		"country_id": "2017370",
		"currency_symbolic_code": "RUB",
		"amount_with_fee": "9000",
		"amount_without_fee": "10000"
	},
	{
		"offset": "0",
		"city_id": "7536080",
		"country_id": "2017370",
		"currency_symbolic_code": "RUB",
		"amount_with_fee": "900",
		"amount_without_fee": "1000"
	},
	{
		"offset": "300",
		"city_id": "524901",
		"country_id": "2017370",
		"currency_symbolic_code": "RUB",
		"amount_with_fee": "18000",
		"amount_without_fee": "19000"
	},
	{
		"offset": "300",
		"city_id": "7536080",
		"country_id": "2017370",
		"currency_symbolic_code": "RUB",
		"amount_with_fee": "5000",
		"amount_without_fee": "5100"
	}
]
```

### Параметры запросов моделей
Все запросы возвращают упорядоченный набор данных, отсортированных по времени создания.

##### `payouts` 
Выборка по выплатам. Параметры:

- `merchant_id`
- `shop_id`
- `payout_id`
- `from_time`
- `to_time`
- `payout_status` - статус выплаты, имена берем из thrift-интерфейса **payout-manager-proto.PayoutStatus**, к примеру: `confirmed`
- `payout_statuses` - список статусов выплаты, имена берем из thrift-интерфейса **payout_processing.PayoutStatus**, к примеру: `["confirmed", "paid"]`
- `payout_type` - тип выплаты, имена берем из thrift-интерфейса **domain.PayoutToolInfo**, к примеру: `russian_bank_account`

##### `payments` 
Выборка по платежам. Параметры:

- `merchant_id`
- `shop_id`
- `payment_id`
- `invoice_id`
- `from_time`
- `to_time`
- `payment_email` - почта, полностью или частично
- `payment_flow` - flow платежа, имена берем из thrift-интерфейса **domain.InvoicePaymentFlow**, к примеру: `instant`
- `payment_method` - метод оплаты, имена берем из thrift-интерфейса **domain.PaymentTool**, к примеру: `payment_terminal`
- `payment_terminal_provider` - провайдер платежного терминалаб имена берем из thrift-интерфейса **domain.TerminalPaymentProvider**, к примеру: `euroset`
- `payment_ip` - ip адрес в виде строки
- `payment_fingerprint` - отпечаток браузера в виде строки
- `payment_pan_mask` - маска в формате [\d\*]+ где все символы * трактуются как любой символ
- `payment_customer_id` - идентификатор покупателя по подпискам
- `payment_amount` - сумма без комисси в минорных единицах 
- `payment_status` - статус платежа, имена берем из thrift-интерфейса **domain.InvoicePaymentStatus**, к примеру: `succeeded`

##### `invoices` 
Выборка по инвойсам. Параметры:

- `merchant_id`
- `shop_id`
- `payment_id`
- `invoice_id`
- `from_time`
- `to_time`
- `payment_email` - почта, полностью или частично 
- `payment_flow` - flow платежа, имена берем из thrift-интерфейса **domain.InvoicePaymentFlow**, к примеру: `instant`
- `payment_method` - метод оплаты, имена берем из thrift-интерфейса **domain.PaymentTool**, к примеру: `payment_terminal`
- `payment_terminal_provider` - провайдер платежного терминалаб имена берем из thrift-интерфейса **domain.TerminalPaymentProvider**, к примеру: `euroset`
- `payment_ip` - ip адрес в виде строки
- `payment_fingerprint` - отпечаток браузера в виде строки
- `payment_pan_mask` - маска в формате [\d\*]+ где все символы * трактуются как любой символ
- `payment_customer_id` - идентификатор покупателя по подпискам
- `payment_amount` - сумма платежа без комисси в минорных единицах 
- `invoice_amount` - сумма инвойса без комисси в минорных единицах 
- `payment_status` - статус платежа, имена берем из thrift-интерфейса **domain.InvoicePaymentStatus**, к примеру: `succeeded`
- `invoice_status` - статус инвойса, имена берем из thrift-интерфейса **domain.InvoiceStatus**, к примеру: `paid`

##### `chargebacks` 
Выборка по чарджбекам. Параметры:

- `merchant_id`
- `shop_ids`
- `invoice_id`
- `payment_id`
- `chargeback_id`
- `from_time`
- `to_time`
- `chargeback_statuses` - список интересующих статусов (pending, accepted, rejected, cancelled)
- `chargeback_categories` - список интересующих категорий (fraud, dispute, authorisation, processing_error)
- `chargeback_stages` - список интересующих этапов (chargeback, pre_arbitration, arbitration)

### Параметры запросов отчетов

##### `shop_accounting_report`

Отчет по магазинам. Представляет из себя данные по магазинам мерчантов в разрезе за определенный период. Более подробная
информация есть reporter/blob/master/docs/accounting_report.md  
Параметры:
- `from_time` - начало временного интервала выборки(inclusive).
- `to_time` - конец временного интервала выборки(exclusive).
- `without_shop_category_ids` - список исключающих категорий

#### Пример:

Запросим данный отчет за период:

```json
{
   "query":{
      "shop_accounting_report":{
         "from_time":"2016-08-11T00:12:00Z",
         "to_time":"2016-08-11T17:12:00Z"
      }
   }
}
```
В ответ получим структуру:

```json
[
  {
    "merchant_id": "74480e4f-1a36-4edd-8175-7a9e984313b0",
    "shop_id": "1",
    "currency_code": "RUB",
    "funds_acquired": "444000",
    "fee_charged": "19980",
    "opening_balance": "2259530",
    "closing_balance": "2683550"
  },
  {
    "merchant_id": "74480e4f-1a36-4edd-8175-7a9e984313b0",
    "shop_id": "2",
    "currency_code": "RUB",
    "funds_acquired": "3631200",
    "fee_charged": "163403",
    "opening_balance": "0",
    "closing_balance": "3467797"
  },
  {
    "merchant_id": "74480e4f-1a36-4edd-8175-7a9e984313b0",
    "shop_id": "3",
    "currency_code": "RUB",
    "funds_acquired": "450000",
    "fee_charged": "20250",
    "opening_balance": "0",
    "closing_balance": "429750"
  }
]
```
