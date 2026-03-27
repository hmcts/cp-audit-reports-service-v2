#!/bin/sh

az storage table create --name reportrequests

az group create --location uksouth --name downloads-group

az container create --resource-group downloads-group --name downloads-container
