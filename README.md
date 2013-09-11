sdg : StockDataGrabber
===

A convinient loader for Yahoo financial data which works also as a local cache for time series data.

It handles a local file which just contains the stock symbol, the request URL and the responses from Yahoo.
This file can be parsed to extract individual rows for individual values, collected from Yahoo, e.g. (close, volume, high, ...).


TODO:
===
- connect to HBase cluster and to TSCache


