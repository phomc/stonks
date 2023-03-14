# Stonks (Fabric Port)
_nahkd porting Stonks as Fabric server mod._

## Links
- [Original Stonks](https://github.com/MangoPlex/Stonks)

## Using database service backend
By default, Stonks (this Fabric port ofc) will use memory-based service, which means all the market data will be cleared when you stop the server. To keep the data, you can configure a database service backend using envvar:

```sh
STONKS_DATABASE="type=mongodb;host=localhost:27017;database=veryCoolDatabaseName;collection=marketData" java -jar server.jar
```

### MongoDB service backend
Stonks currently only support [MongoDB](https://www.mongodb.com/). Here are fields that you can use in envvar:

- ``type=mongodb``: Use MongoDB as service backend
- ``host=<host>[:port]``, default is ``localhost``: Host (and port) of the database. You can use connection string in here, but symbols like ``=`` might confuse Stonks when parsing the string.
- ``database=<name>``: Name of the database
- ``collection=<name>``, default is ``stonks_offers``: The collection that will stores all offers from players.
