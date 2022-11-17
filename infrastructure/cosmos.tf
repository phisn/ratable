resource "azurerm_cosmosdb_account" "core" {
  name                = "cosacc-ratable"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name

  capabilities {
    name = "EnableServerless"
  }

  geo_location {
    location          = azurerm_resource_group.core.location
    failover_priority = 0
  }

  // Currently not caring about consistency
  // https://learn.microsoft.com/en-us/azure/cosmos-db/consistency-levels
  consistency_policy {
    consistency_level = "Eventual"
  }

  offer_type = "Standard"
}

/*
resource "azurerm_cosmosdb_sql_database" "core" {
  name                = "core"
  resource_group_name = azurerm_cosmosdb_account.core.resource_group_name
  account_name        = azurerm_cosmosdb_account.core.name
}

resource "azurerm_cosmosdb_sql_container" "core_ratables" {
  name                  = "ratables"
  resource_group_name   = azurerm_cosmosdb_account.core.resource_group_name
  account_name          = azurerm_cosmosdb_account.core.name
  database_name         = azurerm_cosmosdb_sql_database.core.name
  partition_key_path    = "/id"
}
*/

// Not using the same account for test and prod because connection strings are grained
// by account and not by database. This makes it hard to use the same account for test and prod.
resource "azurerm_cosmosdb_account" "test" {
  name                = "cosacc-ratable-test"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name

  capabilities {
    name = "EnableServerless"
  }

  geo_location {
    location          = azurerm_resource_group.core.location
    failover_priority = 0
  }

  // Currently not caring about consistency
  // https://learn.microsoft.com/en-us/azure/cosmos-db/consistency-levels
  consistency_policy {
    consistency_level = "Eventual"
  }

  offer_type = "Standard"
}

/*
resource "azurerm_cosmosdb_sql_database" "test" {
  name                = "core"
  resource_group_name = azurerm_cosmosdb_account.test.resource_group_name
  account_name        = azurerm_cosmosdb_account.test.name
}

resource "azurerm_cosmosdb_sql_container" "test_ratables" {
  name                  = "ratables"
  resource_group_name   = azurerm_cosmosdb_account.test.resource_group_name
  account_name          = azurerm_cosmosdb_account.test.name
  database_name         = azurerm_cosmosdb_sql_database.test.name
  partition_key_path    = "/id"
}
*/

data "azurerm_cosmosdb_account" "core" {
  name                = azurerm_cosmosdb_account.core.name
  resource_group_name = azurerm_cosmosdb_account.core.resource_group_name
}

data "azurerm_cosmosdb_account" "test" {
  name                = azurerm_cosmosdb_account.test.name
  resource_group_name = azurerm_cosmosdb_account.test.resource_group_name
}
