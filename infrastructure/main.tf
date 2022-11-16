terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.30.0"
    }
  }
  required_version = ">= 0.14.9"
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "core" {
  name     = "rg-ratable"
  location = "westeurope"
}

output "api_key" {
  value = azurerm_static_site.core.api_key
}

output "hostname" {
  value = azurerm_static_site.core.default_host_name
}

output "api_hostname" {
  value = azurerm_windows_function_app.core.default_hostname
}

output "webpubsub_test_connection_string" {
  sensitive = true
  value = azurerm_web_pubsub.test.primary_connection_string
}

output "cosmos_test_connection_string" {
  sensitive = true
  value = azurerm_cosmosdb_account.test.connection_strings
}
