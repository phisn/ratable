terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0.0"
    }
  }
  required_version = ">= 0.14.9"
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-localrating"
  location = "westeurope"
}

/*
resource "azurerm_service_plan" "plan" {
  name                = "plan-localrating"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  os_type             = "Linux"
  sku_name            = "F1"
}
*/

// https://docs.microsoft.com/en-us/azure/static-web-apps/overview
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/static_site
resource "azurerm_static_site" "staticsite" {
  name                = "stapp-localrating"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}

resource "azurerm_web_pubsub" "web_pubsub" {
  name                = "wps-localrating"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  sku = "Free_F1"
}

// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/function_app

output "api_key" {
  value = azurerm_static_site.staticsite.api_key
}

output "hostname" {
  value = azurerm_static_site.staticsite.default_host_name
}

/*
resource "azurerm_storage_account" "st" {
  name                = "stlocalrating"
  resource_group_name = azurerm_resource_group.rg.name
 
  location                 = azurerm_resource_group.rg.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
}

resource "azurerm_storage_blob" "static-web-demo-storage-blob" {
  name                   = "index.html"
  storage_account_name   = azurerm_storage_account.st
  storage_container_name = "$web"
  type                   = "Block"
  content_type           = "text/html"
  source_content         = "<h1>This is static content coming from the Terraform</h1>"
}
*/
